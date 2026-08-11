package com.exempal.shiftcounter.regression;

import com.exempal.shiftcounter.features.comment.application.CommentsReadUseCase;
import com.exempal.shiftcounter.features.comment.application.LossExplanationUseCase;
import com.exempal.shiftcounter.features.comment.application.StoppageRepository;
import com.exempal.shiftcounter.features.comment.domain.LossCategory;
import com.exempal.shiftcounter.features.report.application.ReportQueryUseCase;
import com.exempal.shiftcounter.features.sensor.domain.SensorCatalog;
import com.exempal.shiftcounter.features.sensor.domain.SensorId;
import com.exempal.shiftcounter.features.settings.application.SettingsGroupService;
import com.exempal.shiftcounter.features.settings.application.UpdateSettingsCommand;
import com.exempal.shiftcounter.features.settings.domain.SettingsRow;
import com.exempal.shiftcounter.features.settings.domain.SettingsSnapshot;
import com.exempal.shiftcounter.features.shift.application.ActualDataPort;
import com.exempal.shiftcounter.features.shift.application.ProductionDayService;
import com.exempal.shiftcounter.features.shift.application.projection.ShiftProjectionUseCase;
import com.exempal.shiftcounter.features.shift.application.projection.ShiftSlice;
import com.exempal.shiftcounter.features.shift.domain.Shift;
import com.exempal.shiftcounter.features.signal.application.SignalInputPort;
import com.exempal.shiftcounter.features.signal.application.SignalStoragePort;
import com.exempal.shiftcounter.features.signal.domain.RegisterSignalCommand;
import com.exempal.shiftcounter.features.signal.domain.Signal;
import com.exempal.shiftcounter.features.signal.domain.SignalSource;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Tag("e2e")
class Pr4SixSensorRegressionIT {
    @Autowired SignalInputPort registration;
    @Autowired SignalStoragePort signals;
    @Autowired ActualDataPort shifts;
    @Autowired StoppageRepository stoppages;
    @Autowired LossExplanationUseCase explanations;
    @Autowired CommentsReadUseCase comments;
    @Autowired SettingsGroupService settings;
    @Autowired ProductionDayService productionDays;
    @Autowired ShiftProjectionUseCase projection;
    @Autowired ReportQueryUseCase reports;

    @Test
    void allSixSensorsKeepIndependentSignalActualAndStoppageOwnership() {
        LocalDate date = LocalDate.of(2026, 8, 10);
        LocalDateTime occurredAt = date.atTime(8, 15);

        for (int number = 1; number <= 6; number++) {
            String sensorId = "sensor-" + number;
            assertThat(registration.register(command(sensorId, occurredAt, "pr4-isolation-" + number)).accepted())
                    .as(sensorId).isTrue();
        }

        for (int number = 1; number <= 6; number++) {
            String sensorId = "sensor-" + number;
            assertThat(signals.findBySensorAndRange(sensorId, date.atStartOfDay(), date.plusDays(1).atStartOfDay()))
                    .singleElement().satisfies(signal -> assertThat(signal.sensorId().value()).isEqualTo(sensorId));
            assertThat(shifts.findByDateAndSensorId(date, sensorId).orElseThrow().getActual())
                    .as(sensorId).isEqualTo(1);
        }

        for (String sensorId : List.of("sensor-1", "sensor-2", "sensor-3", "sensor-4", "sensor-6")) {
            assertThat(stoppages.findByShiftDateAndSensorId(date, sensorId))
                    .as(sensorId).isNotEmpty().allMatch(row -> row.sensorKey().equals(sensorId));
        }
        assertThat(stoppages.findByShiftDateAndSensorId(date, SensorCatalog.SENSOR_5)).isEmpty();

        var sensor1Loss = stoppages.findByShiftDateAndSensorId(date, "sensor-1").getFirst();
        var sensor2Loss = stoppages.findByShiftDateAndSensorId(date, "sensor-2").getFirst();
        var sensor6Loss = stoppages.findByShiftDateAndSensorId(date, "sensor-6").getFirst();
        explanations.create(sensor1Loss.id(), LossCategory.MATERIAL, "First source row", 1);
        explanations.create(sensor1Loss.id(), LossCategory.QUALITY, "Second source row", 1);
        explanations.create(sensor2Loss.id(), LossCategory.ORGANIZATION, "Sensor two row", 1);
        explanations.create(sensor6Loss.id(), LossCategory.BREAKDOWN, "Independent six", 1);

        var aggregated = comments.read(date, SensorCatalog.SENSOR_5);
        assertThat(aggregated.rows()).isEmpty();
        assertThat(aggregated.sourceComments()).flatExtracting(CommentsReadUseCase.SourceComments::rows)
                .extracting(CommentsReadUseCase.ExplanationRow::sourceSensorId,
                        CommentsReadUseCase.ExplanationRow::comment)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple("sensor-1", "First source row"),
                        org.assertj.core.groups.Tuple.tuple("sensor-1", "Second source row"),
                        org.assertj.core.groups.Tuple.tuple("sensor-2", "Sensor two row"));
        assertThat(comments.read(date, "sensor-6").rows())
                .flatExtracting(row -> row.explanations())
                .extracting(value -> value.comment())
                .containsExactly("Independent six");
    }

    @Test
    void commonHourTailIsReversibleAndEveningContinuesAfterTwentyThree() {
        LocalDate date = productionDays.current().date();
        SettingsSnapshot original = settings.getSnapshot(SensorCatalog.SHARED_SETTINGS_GROUP);
        for (int number = 1; number <= 6; number++) {
            String sensorId = "sensor-" + number;
            shifts.save(shift(date, sensorId, original, 1));
            signals.save(new Signal(UUID.randomUUID(), SensorId.of(sensorId), date.atTime(8, 15), date,
                    SignalSource.RECOVERY, "pr4-settings-" + sensorId));
        }

        SettingsSnapshot extended = original.addHour().addHour();
        assertThat(extended.rows().get(15)).isEqualTo(new SettingsRow(
                java.time.LocalTime.of(22, 30), 600, 1920));
        assertThat(extended.rows().get(16)).isEqualTo(new SettingsRow(
                java.time.LocalTime.of(23, 30), 600, 1920));
        assertThat(extended.rows().get(17)).isEqualTo(new SettingsRow(
                java.time.LocalTime.of(0, 30), 300, 960));

        update(extended);
        for (int number = 1; number <= 6; number++) {
            String sensorId = "sensor-" + number;
            Shift current = shifts.findByDateAndSensorId(date, sensorId).orElseThrow();
            assertThat(current.getHourlyLabels()).hasSize(18).endsWith("23:30", "00:30");
            assertThat(current.getActual()).as(sensorId).isEqualTo(1);
        }
        assertThat(shifts.findByDateAndSensorId(date, "sensor-5").orElseThrow().getHourlyPlanValues())
                .containsExactlyElementsOf(extended.rows().stream().map(SettingsRow::sensor5Plan).toList());
        assertThat(shifts.findByDateAndSensorId(date, "sensor-6").orElseThrow().getHourlyPlanValues())
                .containsExactlyElementsOf(extended.rows().stream().map(SettingsRow::sensor6Plan).toList());

        registration.register(command("sensor-6", date.atTime(23, 45), "pr4-evening-2345"));
        registration.register(command("sensor-6", date.plusDays(1).atTime(0, 45), "pr4-evening-0045"));
        var evening = projection.buildView(date, "sensor-6", ShiftSlice.EVENING);
        assertThat(evening.hours()).endsWith("23:30", "00:30");
        assertThat(evening.actual().get(evening.hours().indexOf("23:30"))).isEqualTo(1);
        assertThat(evening.actual().get(evening.hours().indexOf("00:30"))).isEqualTo(1);

        SettingsSnapshot restored = extended.deleteLastExtension().deleteLastExtension();
        assertThat(restored).isEqualTo(original);
        update(restored);
        for (int number = 1; number <= 6; number++) {
            String sensorId = "sensor-" + number;
            Shift current = shifts.findByDateAndSensorId(date, sensorId).orElseThrow();
            assertThat(current.getHourlyLabels()).hasSize(SettingsSnapshot.STANDARD_ROW_COUNT);
            assertThat(current.getActual()).as(sensorId).isEqualTo(1);
        }
    }

    @Test
    void sensorFiveReportAggregatesFourSourcesAcrossProductionDateRange() {
        LocalDate from = LocalDate.of(2026, 8, 9);
        LocalDate to = LocalDate.of(2026, 8, 10);
        for (int number = 1; number <= 4; number++) {
            String sensorId = "sensor-" + number;
            registration.register(command(sensorId, from.atTime(8, 15), "pr4-report-first-" + number));
            var loss = stoppages.findByShiftDateAndSensorId(from, sensorId).getFirst();
            explanations.create(loss.id(), LossCategory.MATERIAL, "Range " + number, 1);
            registration.register(command(sensorId, to.atTime(23, 45), "pr4-report-late-" + number));
        }

        var report = reports.query(Map.of("from", from.toString(), "to", to.toString(),
                "sensorId", SensorCatalog.SENSOR_5));

        assertThat(report.rows()).extracting(row -> row.source())
                .containsExactly("sensor-1", "sensor-2", "sensor-3", "sensor-4");
        assertThat(report.rows()).extracting(row -> row.reason())
                .containsExactly("Range 1", "Range 2", "Range 3", "Range 4");
        assertThat(report.signalTotals()).extracting(value -> value.sensorId(), value -> value.total())
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple("sensor-1", 2L),
                        org.assertj.core.groups.Tuple.tuple("sensor-2", 2L),
                        org.assertj.core.groups.Tuple.tuple("sensor-3", 2L),
                        org.assertj.core.groups.Tuple.tuple("sensor-4", 2L));
    }

    private RegisterSignalCommand command(String sensorId, LocalDateTime occurredAt, String identity) {
        return new RegisterSignalCommand(SensorId.of(sensorId), occurredAt, SignalSource.RECOVERY, identity);
    }

    private Shift shift(LocalDate date, String sensorId, SettingsSnapshot snapshot, int actual) {
        List<Integer> plans = sensorId.equals("sensor-6")
                ? snapshot.rows().stream().map(SettingsRow::sensor6Plan).toList()
                : sensorId.equals("sensor-5")
                ? snapshot.rows().stream().map(SettingsRow::sensor5Plan).toList()
                : snapshot.rows().stream().map(SettingsRow::sharedPlan).toList();
        List<Integer> hourlyActual = new ArrayList<>(Collections.nCopies(snapshot.rows().size(), 0));
        hourlyActual.set(1, actual);
        return new Shift(null, date, sensorId, plans, actual, hourlyActual,
                snapshot.rows().stream().map(row -> row.hour().toString()).toList());
    }

    private void update(SettingsSnapshot snapshot) {
        settings.update(new UpdateSettingsCommand(SensorCatalog.SHARED_SETTINGS_GROUP,
                snapshot.rows().stream().map(row -> row.hour().toString()).toList(),
                snapshot.rows().stream().map(SettingsRow::sharedPlan).toList(),
                snapshot.rows().stream().map(SettingsRow::sensor6Plan).toList()));
    }
}

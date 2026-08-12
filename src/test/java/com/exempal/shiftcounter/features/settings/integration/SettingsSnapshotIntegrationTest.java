package com.exempal.shiftcounter.features.settings.integration;

import com.exempal.shiftcounter.features.comment.application.StoppageReconcilesService;
import com.exempal.shiftcounter.features.sensor.domain.SensorId;
import com.exempal.shiftcounter.features.settings.application.SettingsGroupService;
import com.exempal.shiftcounter.features.settings.application.SettingsRepository;
import com.exempal.shiftcounter.features.settings.application.UpdateSettingsCommand;
import com.exempal.shiftcounter.features.settings.domain.SettingUpdatedEvent;
import com.exempal.shiftcounter.features.settings.domain.SettingsRow;
import com.exempal.shiftcounter.features.shift.application.ActualDataPort;
import com.exempal.shiftcounter.features.shift.application.ProductionDayService;
import com.exempal.shiftcounter.features.shift.domain.Shift;
import com.exempal.shiftcounter.features.signal.application.SignalStoragePort;
import com.exempal.shiftcounter.features.signal.domain.Signal;
import com.exempal.shiftcounter.features.signal.domain.SignalSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@SpringBootTest
@RecordApplicationEvents
class SettingsSnapshotIntegrationTest {
    private static final List<String> HOURS = List.of("07:00", "08:00", "09:00", "10:00",
            "11:30", "12:30", "13:30", "14:30", "15:00", "16:00", "17:00", "18:00",
            "19:00", "20:30", "21:30", "22:30");
    private static final List<Integer> SHARED = List.of(450, 600, 500, 600, 600, 600, 500, 600,
            300, 600, 600, 500, 600, 600, 500, 300);
    private static final List<Integer> SENSOR_6 = List.of(1600, 1920, 1600, 1920, 1920, 1920, 1600,
            960, 1920, 1920, 1600, 1920, 1920, 1920, 1600, 960);

    @Autowired SettingsGroupService service;
    @Autowired SettingsRepository settings;
    @Autowired ActualDataPort shifts;
    @Autowired ProductionDayService productionDays;
    @Autowired SignalStoragePort signals;
    @SpyBean StoppageReconcilesService reconcile;
    @Autowired ApplicationEvents applicationEvents;

    @Test
    void readsApprovedCompositeSnapshotAndTotals() {
        var snapshot = service.getSnapshot("settings-group-1");
        assertThat(snapshot.rows()).hasSize(16);
        assertThat(snapshot.rows()).extracting(SettingsRow::sharedPlan).containsExactlyElementsOf(SHARED);
        assertThat(snapshot.sharedTotal()).isEqualTo(8450);
        assertThat(snapshot.sensor5Total()).isEqualTo(33800);
        assertThat(snapshot.sensor6Total()).isEqualTo(27200);
    }

    @Test
    void sharedPlanOnlyUpdatesSensorsOneToFiveAndPreservesActualAndHistory() {
        var date = productionDays.resolve(productionDays.now()).date();
        shifts.save(shift(date, "sensor-1", 3));
        shifts.save(shift(date, "sensor-5", 5));
        shifts.save(shift(date, "sensor-6", 6));
        shifts.save(shift(date.minusDays(1), "sensor-1", 7));
        List<Integer> changed = SHARED.stream().map(value -> value + 1).toList();

        service.update(new UpdateSettingsCommand("settings-group-1", HOURS, changed, SENSOR_6));

        assertThat(current(date, "sensor-1").getHourlyPlanValues()).isEqualTo(changed);
        assertThat(current(date, "sensor-1").getActual()).isEqualTo(3);
        assertThat(current(date, "sensor-5").getHourlyPlanValues())
                .isEqualTo(changed.stream().map(value -> value * 4).toList());
        assertThat(current(date, "sensor-5").getActual()).isEqualTo(5);
        assertThat(current(date, "sensor-6").getHourlyPlanValues()).isEqualTo(SENSOR_6);
        assertThat(current(date.minusDays(1), "sensor-1").getHourlyPlanValues()).isEqualTo(SHARED);
        verify(reconcile, never()).reconcile(any(), eq("sensor-5"), anyInt(), any());
        verify(reconcile, never()).reconcile(any(), eq("sensor-6"), anyInt(), any());
        assertThat(applicationEvents.stream(SettingUpdatedEvent.class)).hasSize(1);
    }

    @Test
    void independentPlanOnlyUpdatesSensorSix() {
        var date = productionDays.resolve(productionDays.now()).date();
        shifts.save(shift(date, "sensor-1", 1));
        shifts.save(shift(date, "sensor-5", 2));
        shifts.save(shift(date, "sensor-6", 3));
        List<Integer> changed = SENSOR_6.stream().map(value -> value + 10).toList();

        service.update(new UpdateSettingsCommand("settings-group-2", HOURS, SHARED, changed));

        assertThat(current(date, "sensor-1").getHourlyPlanValues()).isEqualTo(SHARED);
        assertThat(current(date, "sensor-5").getHourlyPlanValues())
                .isEqualTo(SHARED.stream().map(value -> value * 4).toList());
        assertThat(current(date, "sensor-6").getHourlyPlanValues()).isEqualTo(changed);
        assertThat(current(date, "sensor-6").getActual()).isEqualTo(3);
        verify(reconcile, never()).reconcile(any(), eq("sensor-1"), anyInt(), any());
        verify(reconcile, never()).reconcile(any(), eq("sensor-5"), anyInt(), any());
    }

    @Test
    void timeChangeRedistributesSignalsForAllSensors() {
        var date = productionDays.resolve(productionDays.now()).date();
        for (String sensor : List.of("sensor-1", "sensor-5", "sensor-6")) {
            shifts.save(shift(date, sensor, 2));
            signals.save(new Signal(UUID.randomUUID(), SensorId.of(sensor), date.atTime(7, 45), date,
                    SignalSource.RECOVERY, "settings-" + sensor + "-0745"));
            signals.save(new Signal(UUID.randomUUID(), SensorId.of(sensor), date.atTime(8, 45), date,
                    SignalSource.RECOVERY, "settings-" + sensor + "-0845"));
        }
        List<String> changedHours = new ArrayList<>(HOURS);
        changedHours.set(0, "07:30");

        service.update(new UpdateSettingsCommand("settings-group-1", changedHours, SHARED, SENSOR_6));

        for (String sensor : List.of("sensor-1", "sensor-5", "sensor-6")) {
            assertThat(current(date, sensor).getHourlyLabels()).isEqualTo(changedHours);
            assertThat(current(date, sensor).getHourlyActualValues()).startsWith(1, 1);
            assertThat(current(date, sensor).getActual()).isEqualTo(2);
        }
        verify(reconcile, never()).reconcile(any(), eq("sensor-5"), anyInt(), any());
    }

    @Test
    void failureOnOneAffectedSensorRollsBackBothSettingsGroupsAndEveryShift() {
        var date = productionDays.resolve(productionDays.now()).date();
        shifts.save(shift(date, "sensor-1", 1));
        shifts.save(shift(date, "sensor-6", 1));
        var sharedBefore = settings.findById("settings-group-1");
        var independentBefore = settings.findById("settings-group-2");
        doAnswer(invocation -> {
            if ("sensor-6".equals(invocation.getArgument(1, String.class))) {
                throw new IllegalStateException("forced snapshot failure");
            }
            return null;
        }).when(reconcile).reconcile(any(), anyString(), anyInt(), any());
        List<String> changedHours = new ArrayList<>(HOURS);
        changedHours.set(0, "07:30");

        assertThatThrownBy(() -> service.update(new UpdateSettingsCommand("settings-group-1",
                changedHours, SHARED, SENSOR_6))).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("forced snapshot failure");

        assertThat(settings.findById("settings-group-1")).isEqualTo(sharedBefore);
        assertThat(settings.findById("settings-group-2")).isEqualTo(independentBefore);
        assertThat(current(date, "sensor-1").getHourlyLabels()).isEqualTo(HOURS);
        assertThat(current(date, "sensor-6").getHourlyLabels()).isEqualTo(HOURS);
        assertThat(applicationEvents.stream(SettingUpdatedEvent.class)).isEmpty();
    }

    private Shift current(java.time.LocalDate date, String sensor) {
        return shifts.findByDateAndSensorId(date, sensor).orElseThrow();
    }

    private Shift shift(java.time.LocalDate date, String sensor, int actual) {
        List<Integer> plans = sensor.equals("sensor-6") ? SENSOR_6
                : sensor.equals("sensor-5") ? SHARED.stream().map(value -> value * 4).toList() : SHARED;
        var actuals = new ArrayList<>(Collections.nCopies(HOURS.size(), 0));
        actuals.set(0, actual);
        return new Shift(null, date, sensor, plans, actual, actuals, HOURS);
    }
}

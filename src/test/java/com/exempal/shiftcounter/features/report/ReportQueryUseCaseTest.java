package com.exempal.shiftcounter.features.report.application;

import com.exempal.shiftcounter.features.comment.application.StoppageRepository;
import com.exempal.shiftcounter.features.comment.domain.DetectionType;
import com.exempal.shiftcounter.features.comment.domain.LossCategory;
import com.exempal.shiftcounter.features.comment.domain.LossExplanation;
import com.exempal.shiftcounter.features.comment.domain.Stoppage;
import com.exempal.shiftcounter.features.comment.domain.StoppageState;
import com.exempal.shiftcounter.features.shift.application.ProductionDayService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ReportQueryUseCaseTest {
    private StoppageRepository stoppages;
    private ReportQueryUseCase reports;

    @BeforeEach
    void setUp() {
        stoppages = mock(StoppageRepository.class);
        reports = new ReportQueryUseCase(stoppages, new ProductionDayService(
                Clock.fixed(Instant.parse("2026-08-11T12:00:00Z"), ZoneOffset.UTC)));
    }

    @Test
    void queriesInclusiveProductionDates() {
        reports.query(Map.of("from", "2026-08-08", "to", "2026-08-10", "sensorId", "sensor-2"));

        verify(stoppages).findByShiftDateBetweenAndSensorId(
                LocalDate.of(2026, 8, 8), LocalDate.of(2026, 8, 10), "sensor-2");
    }

    @ParameterizedTest
    @ValueSource(strings = {"sensor-1", "sensor-2", "sensor-3", "sensor-4", "sensor-6"})
    void ownSensorsReturnOnlyTheirOwnExplanationsAndLostCans(String sensorId) {
        when(stoppages.findByShiftDateBetweenAndSensorId(any(), any(), eq(sensorId)))
                .thenReturn(List.of(stoppage(10, sensorId, 7, 21, "own")));

        ReportView view = reports.query(Map.of("sensorId", sensorId));

        assertThat(view.rows()).extracting(ReportRow::source, ReportRow::type, ReportRow::minutes,
                        ReportRow::cans, ReportRow::reason, ReportRow::productionDate)
                .containsExactly(org.assertj.core.groups.Tuple.tuple(sensorId, LossCategory.BREAKDOWN,
                        7, 21, "own", LocalDate.of(2026, 8, 10)));
        assertThat(view.lossTotals()).containsExactly(new ReportLossTotal(sensorId, 21));
    }

    @Test
    void sensorFiveAggregatesExplanationsAndFourSourceLostCanTotals() {
        for (int sensor = 1; sensor <= 4; sensor++) {
            String source = "sensor-" + sensor;
            when(stoppages.findByShiftDateBetweenAndSensorId(any(), any(), eq(source)))
                    .thenReturn(List.of(stoppage(sensor, source, sensor, sensor * 10, "reason-" + sensor)));
        }

        ReportView view = reports.query(Map.of(
                "from", "2026-08-09", "to", "2026-08-10", "sensorId", "sensor-5"));

        assertThat(view.rows()).extracting(ReportRow::source)
                .containsExactly("sensor-4", "sensor-3", "sensor-2", "sensor-1");
        assertThat(view.totalMinutes()).isEqualTo(10);
        assertThat(view.totalCans()).isEqualTo(100);
        assertThat(view.lossTotals()).containsExactly(
                new ReportLossTotal("sensor-1", 10),
                new ReportLossTotal("sensor-2", 20),
                new ReportLossTotal("sensor-3", 30),
                new ReportLossTotal("sensor-4", 40));
        verify(stoppages, never()).findByShiftDateBetweenAndSensorId(any(), any(), eq("sensor-5"));
    }

    @Test
    void resolvedStoppagesDoNotContributeRowsOrTotals() {
        Stoppage resolved = stoppage(20, "sensor-6", 8, 16, "resolved").resolve();
        when(stoppages.findByShiftDateBetweenAndSensorId(any(), any(), eq("sensor-6")))
                .thenReturn(List.of(resolved));

        ReportView view = reports.query(Map.of("sensorId", "sensor-6"));

        assertThat(view.rows()).isEmpty();
        assertThat(view.totalMinutes()).isZero();
        assertThat(view.totalCans()).isZero();
    }

    @ParameterizedTest
    @CsvSource({"2026-08-01,2026-08-07,daily,7", "2026-08-01,2026-08-08,weekly,2",
            "2026-08-01,2026-08-31,weekly,5", "2026-08-01,2026-09-01,monthly,2"})
    void choosesGroupingAtSevenEightThirtyOneAndThirtyTwoDays(String from, String to,
                                                               String grouping, int buckets) {
        ReportView view = reports.query(Map.of("from", from, "to", to, "sensorId", "sensor-1"));
        assertThat(view.timeGrouping()).isEqualTo(grouping);
        assertThat(view.timeTotals()).hasSize(buckets);
    }

    @Test
    void sortsByCansThenMinutesAndKeepsRepositoryOrderForTies() {
        when(stoppages.findByShiftDateBetweenAndSensorId(any(), any(), eq("sensor-1"))).thenReturn(List.of(
                stoppage(1, "sensor-1", 9, 10, "low"), stoppage(2, "sensor-1", 2, 20, "short"),
                stoppage(3, "sensor-1", 7, 20, "first-tie"), stoppage(4, "sensor-1", 7, 20, "second-tie")));
        ReportView view = reports.query(Map.of("sensorId", "sensor-1"));
        assertThat(view.rows()).extracting(ReportRow::reason)
                .containsExactly("first-tie", "second-tie", "short", "low");
    }

    @Test
    void exposesExplanationAuthor() {
        Stoppage value = new Stoppage(30L, UUID.randomUUID(), 30L, "sensor-1", 0,
                LocalDateTime.of(2026, 8, 10, 12, 0), Duration.ofMinutes(4), 4, 8,
                DetectionType.FIXED, StoppageState.ACTIVE, List.of(new LossExplanation(30L, 30L,
                LossCategory.QUALITY, "check", 4, 8, UUID.randomUUID(), "Maria Ivanova",
                Instant.EPOCH, Instant.EPOCH, null, 0L)), 0L);
        when(stoppages.findByShiftDateBetweenAndSensorId(any(), any(), eq("sensor-1"))).thenReturn(List.of(value));
        assertThat(reports.query(Map.of("sensorId", "sensor-1")).rows())
                .extracting(ReportRow::author).containsExactly("Maria Ivanova");
    }

    private Stoppage stoppage(long id, String sensorId, int minutes, int cans, String reason) {
        return new Stoppage(id, UUID.randomUUID(), id, sensorId, 0,
                LocalDateTime.of(2026, 8, 10, 23, 30), Duration.ofMinutes(minutes), minutes, cans,
                DetectionType.FIXED, StoppageState.ACTIVE,
                List.of(new LossExplanation(id, id, LossCategory.BREAKDOWN, reason, minutes, cans)), 0L);
    }
}

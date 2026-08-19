package com.exempal.shiftcounter.features.report.application;

import com.exempal.shiftcounter.features.comment.application.StoppageRepository;
import com.exempal.shiftcounter.features.comment.domain.DetectionType;
import com.exempal.shiftcounter.features.comment.domain.LossCategory;
import com.exempal.shiftcounter.features.comment.domain.LossExplanation;
import com.exempal.shiftcounter.features.comment.domain.Stoppage;
import com.exempal.shiftcounter.features.comment.domain.StoppageState;
import com.exempal.shiftcounter.features.shift.application.ProductionDayService;
import com.exempal.shiftcounter.features.shift.application.ShiftIntervalService;
import com.exempal.shiftcounter.features.shift.application.ActualDataPort;
import com.exempal.shiftcounter.features.shift.domain.Shift;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ReportQueryUseCaseTest {
    private StoppageRepository stoppages;
    private ReportQueryUseCase reports;
    private ActualDataPort shifts;

    @BeforeEach
    void setUp() {
        stoppages = mock(StoppageRepository.class);
        shifts = mock(ActualDataPort.class);
        reports = new ReportQueryUseCase(stoppages, new ProductionDayService(
                Clock.fixed(Instant.parse("2026-08-11T12:00:00Z"), ZoneOffset.UTC)), shifts,
                new ShiftIntervalService());
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
                .containsExactly(org.assertj.core.groups.Tuple.tuple(sensorId, "BREAKDOWN",
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
    void resolvedStoppagesKeepSavedExplanationsInReport() {
        Stoppage resolved = stoppage(20, "sensor-6", 8, 16, "resolved").resolve();
        when(stoppages.findByShiftDateBetweenAndSensorId(any(), any(), eq("sensor-6")))
                .thenReturn(List.of(resolved));

        ReportView view = reports.query(Map.of("sensorId", "sensor-6"));

        assertThat(view.rows()).extracting(ReportRow::reason).containsExactly("resolved");
        assertThat(view.totalMinutes()).isEqualTo(8);
        assertThat(view.totalCans()).isEqualTo(16);
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

    @Test
    void filtersRowsAndLossTotalsBySourceTypeReasonAndAuthor() {
        when(stoppages.findByShiftDateBetweenAndSensorId(any(), any(), eq("sensor-2")))
                .thenReturn(List.of(authoredStoppage(41, "sensor-2", LossCategory.MATERIAL,
                        "Kiwi wrapper jam", "Anton Nowak", 12, 120)));
        when(stoppages.findByShiftDateBetweenAndSensorId(any(), any(), eq("sensor-3")))
                .thenReturn(List.of(authoredStoppage(42, "sensor-3", LossCategory.MATERIAL,
                        "Kiwi wrapper jam", "Anton Nowak", 20, 200)));

        ReportView view = reports.query(Map.of("sensorId", "sensor-5", "source", "sensor-2",
                "type", "MATERIAL", "reason", "WRAPPER", "author", "anton"));

        assertThat(view.rows()).extracting(ReportRow::source, ReportRow::reason, ReportRow::author)
                .containsExactly(org.assertj.core.groups.Tuple.tuple(
                        "sensor-2", "Kiwi wrapper jam", "Anton Nowak"));
        assertThat(view.totalMinutes()).isEqualTo(12);
        assertThat(view.totalCans()).isEqualTo(120);
    }

    @Test
    void oneDayProductionAndUnexplainedPlanAreGroupedByIntervals() {
        LocalDate date = LocalDate.of(2026, 8, 10);
        Shift shift = new Shift(50L, date, "sensor-2", List.of(100, 120), 0,
                List.of(80, 130), List.of("07:00", "08:00"));
        when(shifts.findByDateAndSensorId(date, "sensor-2")).thenReturn(Optional.of(shift));

        ReportView view = reports.query(Map.of("from", date.toString(), "to", date.toString(),
                "sensorId", "sensor-2"));

        assertThat(view.productionTotals()).containsExactly(
                new ReportChartPoint("07:00", 80), new ReportChartPoint("08:00", 130));
        assertThat(view.totalProduction()).isEqualTo(210);
        assertThat(view.unexplainedPlanTotals()).containsExactly(
                new ReportChartPoint("07:00", 20), new ReportChartPoint("08:00", 0));
    }

    @Test
    void unexplainedPlanSubtractsSavedExplanationsAndZerosUnfinishedIntervals() {
        LocalDate date = LocalDate.of(2026, 8, 11);
        Shift shift = new Shift(51L, date, "sensor-5", List.of(1800, 2400, 2000, 2400), 0,
                List.of(0, 373, 0, 0), List.of("07:00", "08:00", "11:30", "12:30"));
        when(shifts.findByDateAndSensorId(date, "sensor-5")).thenReturn(Optional.of(shift));
        when(stoppages.findByShiftDateBetweenAndSensorId(any(), any(), eq("sensor-2")))
                .thenReturn(List.of(
                        stoppageAtInterval(52, "sensor-2", 0, 1800),
                        stoppageAtInterval(53, "sensor-2", 1, 500)));

        ReportView view = reports.query(Map.of("from", date.toString(), "to", date.toString(),
                "sensorId", "sensor-5"));

        assertThat(view.unexplainedPlanTotals()).containsExactly(
                new ReportChartPoint("07:00", 0),
                new ReportChartPoint("08:00", 1527),
                new ReportChartPoint("11:30", 0),
                new ReportChartPoint("12:30", 0));
    }

    @Test
    void unexplainedPlanKeepsInternalStopsAndZerosIntervalsAfterProductionStopped() {
        LocalDate date = LocalDate.of(2026, 8, 10);
        Shift shift = new Shift(54L, date, "sensor-5", List.of(100, 100, 100, 100, 100), 0,
                List.of(80, 0, 70, 0, 0), List.of("07:00", "08:00", "09:00", "10:00", "11:00"));
        when(shifts.findByDateAndSensorId(date, "sensor-5")).thenReturn(Optional.of(shift));

        ReportView view = reports.query(Map.of("from", date.toString(), "to", date.toString(),
                "sensorId", "sensor-5"));

        assertThat(view.unexplainedPlanTotals()).containsExactly(
                new ReportChartPoint("07:00", 20),
                new ReportChartPoint("08:00", 100),
                new ReportChartPoint("09:00", 30),
                new ReportChartPoint("10:00", 0),
                new ReportChartPoint("11:00", 0));
    }

    @Test
    void productionUsesWeeklyBucketsThroughThirtyOneDaysAndMonthlyBucketsAfterThat() {
        LocalDate first = LocalDate.of(2026, 7, 1);
        LocalDate eighth = first.plusDays(7);
        when(shifts.findByDateAndSensorId(first, "sensor-1")).thenReturn(Optional.of(
                new Shift(61L, first, "sensor-1", List.of(10), 0, List.of(4), List.of("07:00"))));
        when(shifts.findByDateAndSensorId(eighth, "sensor-1")).thenReturn(Optional.of(
                new Shift(62L, eighth, "sensor-1", List.of(10), 0, List.of(6), List.of("07:00"))));

        ReportView weekly = reports.query(Map.of("from", first.toString(), "to", first.plusDays(30).toString(),
                "sensorId", "sensor-1"));
        ReportView monthly = reports.query(Map.of("from", first.toString(), "to", first.plusDays(31).toString(),
                "sensorId", "sensor-1"));

        assertThat(weekly.productionTotals()).extracting(ReportChartPoint::label, ReportChartPoint::value)
                .startsWith(org.assertj.core.groups.Tuple.tuple("2026-07-01–2026-07-07", 4),
                        org.assertj.core.groups.Tuple.tuple("2026-07-08–2026-07-14", 6));
        assertThat(monthly.productionTotals()).containsExactly(
                new ReportChartPoint("2026-07", 10), new ReportChartPoint("2026-08", 0));
    }

    @Test
    void defaultsToSensorFive() {
        ReportView report = reports.query(Map.of());

        assertThat(report.sensorId()).isEqualTo("sensor-5");
        assertThat(report.from()).isEqualTo(LocalDate.of(2026, 8, 11));
        assertThat(report.to()).isEqualTo(LocalDate.of(2026, 8, 11));
    }

    private Stoppage authoredStoppage(long id, String sensorId, LossCategory category,
                                      String reason, String author, int minutes, int cans) {
        return new Stoppage(id, UUID.randomUUID(), id, sensorId, 0,
                LocalDateTime.of(2026, 8, 10, 12, 0), Duration.ofMinutes(minutes), minutes, cans,
                DetectionType.FIXED, StoppageState.ACTIVE, List.of(new LossExplanation(id, id,
                category, reason, minutes, cans, UUID.randomUUID(), author,
                Instant.EPOCH, Instant.EPOCH, null, 0L)), 0L);
    }

    private Stoppage stoppage(long id, String sensorId, int minutes, int cans, String reason) {
        return new Stoppage(id, UUID.randomUUID(), id, sensorId, 0,
                LocalDateTime.of(2026, 8, 10, 23, 30), Duration.ofMinutes(minutes), minutes, cans,
                DetectionType.FIXED, StoppageState.ACTIVE,
                List.of(new LossExplanation(id, id, LossCategory.BREAKDOWN, reason, minutes, cans)), 0L);
    }

    private Stoppage stoppageAtInterval(long id, String sensorId, int intervalIndex, int cans) {
        return new Stoppage(id, UUID.randomUUID(), id, sensorId, intervalIndex,
                LocalDateTime.of(2026, 8, 11, 7, 0), Duration.ofMinutes(60), 60, cans,
                DetectionType.FIXED, StoppageState.RESOLVED,
                List.of(new LossExplanation(id, id, LossCategory.ORGANIZATION,
                        "explained", 60, cans)), 0L);
    }
}

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
    private ReportSignalQueryPort signals;
    private ReportQueryUseCase reports;

    @BeforeEach
    void setUp() {
        stoppages = mock(StoppageRepository.class);
        signals = mock(ReportSignalQueryPort.class);
        reports = new ReportQueryUseCase(stoppages, new ProductionDayService(
                Clock.fixed(Instant.parse("2026-08-11T12:00:00Z"), ZoneOffset.UTC)), signals);
    }

    @Test
    void queriesInclusiveProductionDatesAndHalfOpenSevenAmSignalRange() {
        reports.query(Map.of("from", "2026-08-08", "to", "2026-08-10", "sensorId", "sensor-2"));

        verify(stoppages).findByShiftDateBetweenAndSensorId(
                LocalDate.of(2026, 8, 8), LocalDate.of(2026, 8, 10), "sensor-2");
        verify(signals).count("sensor-2",
                LocalDateTime.of(2026, 8, 8, 7, 0),
                LocalDateTime.of(2026, 8, 11, 7, 0));
    }

    @ParameterizedTest
    @ValueSource(strings = {"sensor-1", "sensor-2", "sensor-3", "sensor-4", "sensor-6"})
    void ownSensorsReturnOnlyTheirOwnExplanationsAndSignals(String sensorId) {
        when(stoppages.findByShiftDateBetweenAndSensorId(any(), any(), eq(sensorId)))
                .thenReturn(List.of(stoppage(10, sensorId, 7, 21, "own")));
        when(signals.count(eq(sensorId), any(), any())).thenReturn(9L);

        ReportView view = reports.query(Map.of("sensorId", sensorId));

        assertThat(view.rows()).containsExactly(
                new ReportRow(sensorId, LossCategory.BREAKDOWN, 7, 21, "own"));
        assertThat(view.signalTotals()).containsExactly(new ReportSignalTotal(sensorId, 9));
        verify(signals).count(eq(sensorId), any(), any());
        verifyNoMoreInteractions(signals);
    }

    @Test
    void sensorFiveAggregatesExplanationsAndFourSourceSignalTotals() {
        for (int sensor = 1; sensor <= 4; sensor++) {
            String source = "sensor-" + sensor;
            when(stoppages.findByShiftDateBetweenAndSensorId(any(), any(), eq(source)))
                    .thenReturn(List.of(stoppage(sensor, source, sensor, sensor * 10, "reason-" + sensor)));
            when(signals.count(eq(source), any(), any())).thenReturn((long) sensor * 100);
        }

        ReportView view = reports.query(Map.of(
                "from", "2026-08-09", "to", "2026-08-10", "sensorId", "sensor-5"));

        assertThat(view.rows()).extracting(ReportRow::source)
                .containsExactly("sensor-1", "sensor-2", "sensor-3", "sensor-4");
        assertThat(view.totalMinutes()).isEqualTo(10);
        assertThat(view.totalCans()).isEqualTo(100);
        assertThat(view.signalTotals()).containsExactly(
                new ReportSignalTotal("sensor-1", 100),
                new ReportSignalTotal("sensor-2", 200),
                new ReportSignalTotal("sensor-3", 300),
                new ReportSignalTotal("sensor-4", 400));
        verify(stoppages, never()).findByShiftDateBetweenAndSensorId(any(), any(), eq("sensor-5"));
        verify(signals, never()).count(eq("sensor-5"), any(), any());
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

    private Stoppage stoppage(long id, String sensorId, int minutes, int cans, String reason) {
        return new Stoppage(id, UUID.randomUUID(), id, sensorId, 0,
                LocalDateTime.of(2026, 8, 10, 23, 30), Duration.ofMinutes(minutes), minutes, cans,
                DetectionType.FIXED, StoppageState.ACTIVE,
                List.of(new LossExplanation(id, id, LossCategory.BREAKDOWN, reason, minutes, cans)), 0L);
    }
}

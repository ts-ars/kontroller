package com.exempal.shiftcounter.features.signal;

import com.exempal.shiftcounter.features.signal.application.*;
import com.exempal.shiftcounter.features.sensor.domain.SensorId;
import com.exempal.shiftcounter.features.signal.domain.*;
import com.exempal.shiftcounter.features.signal.adapter.persistence.CounterStateJpaRepository;
import com.exempal.shiftcounter.features.signal.adapter.persistence.SignalJpaRepository;
import com.exempal.shiftcounter.features.shift.application.ActualDataPort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class Stage7CounterRecoveryIntegrationTest {
    @Autowired CounterInputPort counters;
    @Autowired CounterStateJpaRepository states;
    @Autowired SignalJpaRepository signals;
    @Autowired ActualDataPort shifts;

    @Test
    void restartUsesPersistedBaselineAndAppliesOnlyAccumulatedDelta() {
        SensorId sensor = SensorId.of("sensor-2");
        LocalDateTime firstRead = LocalDateTime.of(2026, 8, 10, 8, 0);

        assertThat(counters.process(new CounterReadingCommand(sensor, 100, firstRead)).status())
                .isEqualTo(CounterProcessingStatus.BASELINE_ESTABLISHED);
        assertThat(counters.process(new CounterReadingCommand(sensor, 103, firstRead.plusMinutes(1))).delta())
                .isEqualTo(3);
        assertThat(counters.process(new CounterReadingCommand(sensor, 105, firstRead.plusMinutes(5))).delta())
                .isEqualTo(2);

        assertThat(signals.count()).isEqualTo(5);
        assertThat(shifts.findByDateAndSensorId(LocalDate.of(2026, 8, 10), sensor.value()).orElseThrow()
                .getActual()).isEqualTo(5);
        assertThat(states.findById(sensor.value()).orElseThrow().getLastCounterValue()).isEqualTo(105);
    }

    @Test
    void boundaryPollAssignsWholeDeltaToOldDayAndStartsNewBaseline() {
        SensorId sensor = SensorId.of("sensor-3");
        LocalDateTime beforeBoundary = LocalDateTime.of(2026, 8, 11, 6, 59);
        LocalDateTime afterBoundary = LocalDateTime.of(2026, 8, 11, 7, 1);
        counters.process(new CounterReadingCommand(sensor, 200, beforeBoundary));

        CounterProcessingResult result = counters.process(new CounterReadingCommand(sensor, 202, afterBoundary));

        assertThat(result.attributedProductionDate()).isEqualTo(LocalDate.of(2026, 8, 10));
        assertThat(shifts.findByDateAndSensorId(LocalDate.of(2026, 8, 10), sensor.value()).orElseThrow()
                .getActual()).isEqualTo(2);
        assertThat(shifts.findByDateAndSensorId(LocalDate.of(2026, 8, 11), sensor.value())).isEmpty();
        var state = states.findById(sensor.value()).orElseThrow();
        assertThat(state.getProductionDate()).isEqualTo(LocalDate.of(2026, 8, 11));
        assertThat(state.getLastCounterValue()).isEqualTo(202);
    }

    @Test
    void discontinuityPersistsDiagnosticWithoutInventingDeltaOrReplacingBaseline() {
        SensorId sensor = SensorId.of("sensor-4");
        LocalDateTime firstRead = LocalDateTime.of(2026, 8, 10, 9, 0);
        counters.process(new CounterReadingCommand(sensor, 500, firstRead));

        CounterProcessingResult result = counters.process(
                new CounterReadingCommand(sensor, 12, firstRead.plusMinutes(1)));

        assertThat(result.status()).isEqualTo(CounterProcessingStatus.COUNTER_DISCONTINUITY);
        assertThat(signals.count()).isZero();
        var state = states.findById(sensor.value()).orElseThrow();
        assertThat(state.getLastCounterValue()).isEqualTo(500);
        assertThat(state.getContinuity()).isEqualTo(CounterContinuity.COUNTER_DISCONTINUITY);
    }
}

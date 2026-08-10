package com.exempal.shiftcounter.features.signal;

import com.exempal.shiftcounter.features.comment.calculator.StoppageCalculator;
import com.exempal.shiftcounter.features.sensor.domain.SensorId;
import com.exempal.shiftcounter.features.signal.domain.*;
import com.exempal.shiftcounter.features.signal.infrastructure.SignalJpaRepository;
import com.exempal.shiftcounter.features.shift.domain.ActualDataPort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
class Stage7SignalRollbackIntegrationTest {
    @Autowired SignalInputPort registration;
    @Autowired ActualDataPort shifts;
    @Autowired SignalJpaRepository signals;
    @MockBean StoppageCalculator calculator;

    @Test
    void reconcileFailureRollsBackSignalAndActualTogether() {
        when(calculator.calculate(any())).thenThrow(new IllegalStateException("forced reconcile failure"));
        RegisterSignalCommand command = new RegisterSignalCommand(SensorId.of("sensor-6"),
                LocalDateTime.of(2026, 8, 10, 8, 15), SignalSource.RECOVERY, "rollback-signal");

        assertThatThrownBy(() -> registration.register(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("forced reconcile failure");

        assertThat(signals.count()).isZero();
        assertThat(shifts.findByDateAndSensorId(LocalDate.of(2026, 8, 10), "sensor-6")).isEmpty();
    }
}

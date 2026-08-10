package com.exempal.shiftcounter.features.signal;

import com.exempal.shiftcounter.features.signal.application.*;
import com.exempal.shiftcounter.features.sensor.domain.SensorId;
import com.exempal.shiftcounter.features.signal.domain.*;
import com.exempal.shiftcounter.features.signal.adapter.persistence.SignalJpaRepository;
import com.exempal.shiftcounter.features.shift.application.ActualDataPort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class Stage6SignalRegistrationIntegrationTest {
    @Autowired SignalInputPort registration;
    @Autowired ActualDataPort shifts;
    @Autowired SignalJpaRepository signals;

    @Test
    void duplicateIsNoOpAndTwoSensorsKeepIndependentFacts() {
        LocalDateTime occurredAt = LocalDateTime.of(2026, 8, 10, 8, 15);
        RegisterSignalCommand sensor2 = new RegisterSignalCommand(SensorId.of("sensor-2"), occurredAt,
                SignalSource.RECOVERY, "recovery-event-17");
        RegisterSignalCommand sensor3 = new RegisterSignalCommand(SensorId.of("sensor-3"), occurredAt,
                SignalSource.RECOVERY, "recovery-event-17");

        assertThat(registration.register(sensor2).accepted()).isTrue();
        assertThat(registration.register(sensor2).accepted()).isFalse();
        assertThat(registration.register(sensor3).accepted()).isTrue();

        assertThat(signals.count()).isEqualTo(2);
        assertThat(shifts.findByDateAndSensorId(LocalDate.of(2026, 8, 10), "sensor-2").orElseThrow()
                .getHourlyActualValues()).startsWith(1);
        assertThat(shifts.findByDateAndSensorId(LocalDate.of(2026, 8, 10), "sensor-3").orElseThrow()
                .getHourlyActualValues()).startsWith(1);
        assertThat(shifts.findByDateAndSensorId(LocalDate.of(2026, 8, 10), "sensor-1")).isEmpty();
    }
}

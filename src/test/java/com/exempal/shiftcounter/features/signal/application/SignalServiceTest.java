package com.exempal.shiftcounter.features.signal.application;

import com.exempal.shiftcounter.features.sensor.domain.SensorId;
import com.exempal.shiftcounter.features.signal.domain.*;
import com.exempal.shiftcounter.features.shift.application.ProductionDayService;
import com.exempal.shiftcounter.shared.event.DomainEventPublisher;
import com.exempal.shiftcounter.shared.event.ProductDetectedEvent;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

class SignalServiceTest {
    @Test
    void duplicateSourceIdentityPublishesNoSecondIncrementTrigger() {
        SignalStoragePort storage = mock(SignalStoragePort.class);
        DomainEventPublisher events = mock(DomainEventPublisher.class);
        when(storage.saveIfAbsent(any())).thenReturn(true, false);
        SignalService service = new SignalService(events, storage,
                new ProductionDayService(Clock.system(ZoneOffset.UTC)));
        RegisterSignalCommand command = new RegisterSignalCommand(SensorId.of("sensor-3"),
                LocalDateTime.of(2026, 8, 10, 9, 15), SignalSource.RECOVERY, "source-event-42");

        assertThat(service.register(command).accepted()).isTrue();
        assertThat(service.register(command).accepted()).isFalse();

        verify(storage, times(2)).saveIfAbsent(argThat(signal ->
                signal.sensorId().value().equals("sensor-3")
                        && signal.productionDate().toString().equals("2026-08-10")
                        && signal.sourceIdentity().equals("source-event-42")));
        verify(events, times(1)).publish(isA(ProductDetectedEvent.class));
    }

    @Test
    void recordsPreviousProductionDateBeforeSeven() {
        SignalStoragePort storage = mock(SignalStoragePort.class);
        when(storage.saveIfAbsent(any())).thenReturn(true);
        SignalService service = new SignalService(mock(DomainEventPublisher.class), storage,
                new ProductionDayService(Clock.system(ZoneOffset.UTC)));

        service.register(new RegisterSignalCommand(SensorId.of("sensor-6"),
                LocalDateTime.of(2026, 8, 10, 0, 15), SignalSource.BATCH, "batch-1"));

        verify(storage).saveIfAbsent(argThat(signal ->
                signal.productionDate().toString().equals("2026-08-09")));
    }
}

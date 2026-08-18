package com.exempal.shiftcounter.features.signal.application;

import com.exempal.shiftcounter.features.sensor.domain.SensorId;
import com.exempal.shiftcounter.features.shift.application.ProductionDayService;
import com.exempal.shiftcounter.features.signal.domain.*;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CounterInputServiceTest {
    private static final SensorId SENSOR = SensorId.of("sensor-2");
    private final CounterStateStoragePort states = mock(CounterStateStoragePort.class);
    private final SignalInputPort signals = mock(SignalInputPort.class);
    private final CounterInputService service = new CounterInputService(states, signals,
            new ProductionDayService(Clock.system(ZoneOffset.UTC)));

    @Test
    void firstReadingEstablishesPersistedBaselineWithoutInventingProduction() {
        LocalDateTime readAt = LocalDateTime.of(2026, 8, 10, 8, 0);
        CounterState baseline = state(40, readAt, LocalDate.of(2026, 8, 10));
        when(states.getOrInitializeForUpdate(SENSOR, 40, readAt, baseline.productionDate()))
                .thenReturn(new CounterStateLoad(baseline, true));

        CounterProcessingResult result = service.process(new CounterReadingCommand(SENSOR, 40, readAt));

        assertThat(result.status()).isEqualTo(CounterProcessingStatus.BASELINE_ESTABLISHED);
        verifyNoInteractions(signals);
    }

    @Test
    void appliesCounterDeltaWithStableSourceIdentities() {
        LocalDateTime previousRead = LocalDateTime.of(2026, 8, 10, 8, 0);
        LocalDateTime currentRead = previousRead.plusMinutes(1);
        when(states.getOrInitializeForUpdate(any(), anyLong(), any(), any()))
                .thenReturn(new CounterStateLoad(state(40, previousRead, LocalDate.of(2026, 8, 10)), false));
        when(signals.register(any())).thenAnswer(invocation -> {
            RegisterSignalCommand command = invocation.getArgument(0);
            return new SignalRegistrationResult(java.util.UUID.randomUUID(), command.sensorId(), true);
        });

        CounterProcessingResult result = service.process(new CounterReadingCommand(SENSOR, 43, currentRead));

        ArgumentCaptor<RegisterSignalCommand> commands = ArgumentCaptor.forClass(RegisterSignalCommand.class);
        verify(signals, times(3)).register(commands.capture());
        assertThat(commands.getAllValues()).extracting(RegisterSignalCommand::sourceIdentity)
                .containsExactly("counter-2026-08-10-41", "counter-2026-08-10-42", "counter-2026-08-10-43");
        assertThat(commands.getAllValues()).extracting(RegisterSignalCommand::occurredAt)
                .containsOnly(currentRead);
        assertThat(result.delta()).isEqualTo(3);
        assertThat(result.acceptedSignals()).isEqualTo(3);
    }

    @Test
    void pollCrossingSevenAttributesWholeDeltaToOldProductionDayAndSetsNewBaseline() {
        LocalDateTime previousRead = LocalDateTime.of(2026, 8, 11, 6, 59);
        LocalDateTime currentRead = LocalDateTime.of(2026, 8, 11, 7, 1);
        when(states.getOrInitializeForUpdate(any(), anyLong(), any(), any()))
                .thenReturn(new CounterStateLoad(state(100, previousRead, LocalDate.of(2026, 8, 10)), false));
        when(signals.register(any())).thenReturn(new SignalRegistrationResult(java.util.UUID.randomUUID(), SENSOR, true));

        CounterProcessingResult result = service.process(new CounterReadingCommand(SENSOR, 102, currentRead));

        ArgumentCaptor<RegisterSignalCommand> commands = ArgumentCaptor.forClass(RegisterSignalCommand.class);
        verify(signals, times(2)).register(commands.capture());
        assertThat(commands.getAllValues()).extracting(RegisterSignalCommand::occurredAt)
                .containsOnly(previousRead);
        assertThat(commands.getAllValues()).extracting(RegisterSignalCommand::sourceIdentity)
                .containsExactly("counter-2026-08-10-101", "counter-2026-08-10-102");
        assertThat(result.attributedProductionDate()).isEqualTo(LocalDate.of(2026, 8, 10));
        ArgumentCaptor<CounterState> saved = ArgumentCaptor.forClass(CounterState.class);
        verify(states).save(saved.capture());
        assertThat(saved.getValue().productionDate()).isEqualTo(LocalDate.of(2026, 8, 11));
        assertThat(saved.getValue().lastCounterValue()).isEqualTo(102);
    }

    @Test
    void lowerCounterEstablishesNewBaselineWithoutApplyingDelta() {
        LocalDateTime previousRead = LocalDateTime.of(2026, 8, 10, 9, 0);
        LocalDateTime currentRead = previousRead.plusMinutes(1);
        when(states.getOrInitializeForUpdate(any(), anyLong(), any(), any()))
                .thenReturn(new CounterStateLoad(state(200, previousRead, LocalDate.of(2026, 8, 10)), false));

        CounterProcessingResult result = service.process(new CounterReadingCommand(SENSOR, 12, currentRead));

        assertThat(result.status()).isEqualTo(CounterProcessingStatus.BASELINE_ESTABLISHED);
        verifyNoInteractions(signals);
        ArgumentCaptor<CounterState> saved = ArgumentCaptor.forClass(CounterState.class);
        verify(states).save(saved.capture());
        assertThat(saved.getValue().lastCounterValue()).isEqualTo(12);
        assertThat(saved.getValue().continuity()).isEqualTo(CounterContinuity.CONTINUOUS);
    }

    @Test
    void persistedStateDrivesRestartRecoveryInsteadOfReplacingBaseline() {
        LocalDateTime previousRead = LocalDateTime.of(2026, 8, 10, 10, 0);
        LocalDateTime restartRead = previousRead.plusMinutes(5);
        when(states.getOrInitializeForUpdate(any(), anyLong(), any(), any()))
                .thenReturn(new CounterStateLoad(state(500, previousRead, LocalDate.of(2026, 8, 10)), false));
        when(signals.register(any())).thenReturn(new SignalRegistrationResult(java.util.UUID.randomUUID(), SENSOR, true));

        CounterProcessingResult result = service.process(new CounterReadingCommand(SENSOR, 502, restartRead));

        assertThat(result.delta()).isEqualTo(2);
        verify(signals, times(2)).register(any());
    }

    private CounterState state(long value, LocalDateTime readAt, LocalDate productionDate) {
        return new CounterState(SENSOR, value, readAt, productionDate, CounterContinuity.CONTINUOUS);
    }
}

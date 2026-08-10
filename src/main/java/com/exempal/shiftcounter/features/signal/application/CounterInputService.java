package com.exempal.shiftcounter.features.signal.application;

import com.exempal.shiftcounter.features.shift.application.ProductionDayService;
import com.exempal.shiftcounter.features.signal.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CounterInputService implements CounterInputPort {
    private final CounterStateStoragePort states;
    private final SignalInputPort signals;
    private final ProductionDayService productionDays;

    @Override
    @Transactional
    public CounterProcessingResult process(CounterReadingCommand command) {
        var currentProductionDate = productionDays.resolve(command.readAt()).date();
        CounterStateLoad loaded = states.getOrInitializeForUpdate(command.sensorId(), command.currentCounter(),
                command.readAt(), currentProductionDate);
        CounterState previous = loaded.state();
        if (loaded.initialized()) {
            return new CounterProcessingResult(CounterProcessingStatus.BASELINE_ESTABLISHED, 0, 0,
                    currentProductionDate);
        }
        if (previous.continuity() == CounterContinuity.COUNTER_DISCONTINUITY
                || command.currentCounter() < previous.lastCounterValue()) {
            states.save(new CounterState(previous.sensorId(), previous.lastCounterValue(), command.readAt(),
                    previous.productionDate(), CounterContinuity.COUNTER_DISCONTINUITY));
            return new CounterProcessingResult(CounterProcessingStatus.COUNTER_DISCONTINUITY, 0, 0,
                    previous.productionDate());
        }

        long delta = command.currentCounter() - previous.lastCounterValue();
        boolean crossedProductionDay = !currentProductionDate.equals(previous.productionDate());
        LocalDateTime occurredAt = crossedProductionDay ? previous.lastReadAt() : command.readAt();
        long accepted = 0;
        for (long counterValue = previous.lastCounterValue() + 1;
             counterValue <= command.currentCounter(); counterValue++) {
            SignalRegistrationResult result = signals.register(new RegisterSignalCommand(command.sensorId(),
                    occurredAt, SignalSource.ADAM, "counter-" + counterValue));
            if (result.accepted()) accepted++;
        }
        states.save(new CounterState(command.sensorId(), command.currentCounter(), command.readAt(),
                currentProductionDate, CounterContinuity.CONTINUOUS));
        CounterProcessingStatus status = delta == 0
                ? CounterProcessingStatus.NO_CHANGE : CounterProcessingStatus.APPLIED;
        return new CounterProcessingResult(status, delta, accepted,
                crossedProductionDay ? previous.productionDate() : currentProductionDate);
    }
}

package com.exempal.shiftcounter.features.signal.application;

import com.exempal.shiftcounter.features.signal.domain.*;
import com.exempal.shiftcounter.features.shift.application.ProductionDayService;
import com.exempal.shiftcounter.features.shift.application.ProductRegistrationUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SignalService implements SignalInputPort {
    private final ProductRegistrationUseCase productRegistration;
    private final SignalStoragePort signalStorage;
    private final ProductionDayService productionDays;
    private final SignalRegistrationLock registrationLock;

    @Override
    @Transactional
    public SignalRegistrationResult register(RegisterSignalCommand command) {
        UUID id = UUID.randomUUID();
        var productionDate = productionDays.resolve(command.occurredAt()).date();
        registrationLock.acquire(productionDate, command.sensorId().value());
        Signal signal = new Signal(id, command.sensorId(), command.occurredAt(), productionDate,
                command.source(), command.sourceIdentity());
        if (!signalStorage.saveIfAbsent(signal)) return SignalRegistrationResult.duplicate(command.sensorId());
        productRegistration.registerProduct(command.sensorId().value(), command.occurredAt());
        return new SignalRegistrationResult(id, command.sensorId(), true);
    }
}

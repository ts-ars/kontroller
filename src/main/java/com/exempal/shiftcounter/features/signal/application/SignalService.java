package com.exempal.shiftcounter.features.signal.application;

import com.exempal.shiftcounter.features.signal.domain.Signal;
import com.exempal.shiftcounter.features.signal.domain.SignalInputPort;
import com.exempal.shiftcounter.features.signal.domain.SignalStoragePort;
import com.exempal.shiftcounter.shared.event.DomainEventPublisher;
import com.exempal.shiftcounter.shared.event.ProductDetectedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SignalService implements SignalInputPort {

    private final DomainEventPublisher eventPublisher;
    private final SignalStoragePort signalStorage;

    @Override
    public void onProductSensorTriggered() {
        Instant now = Instant.now();
        LocalDateTime localDateTime = now.atZone(ZoneId.systemDefault()).toLocalDateTime();
        signalStorage.save(new Signal(localDateTime));
        eventPublisher.publish(new ProductDetectedEvent(now));
    }

    // ✅ Этот метод вызывает существующий порт
    public List<Signal> getSignalsBetween(LocalDateTime start, LocalDateTime end) {
        return signalStorage.findByRange(start, end); // ✅ всё корректно
    }
}
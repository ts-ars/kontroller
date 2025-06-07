package com.exempal.shiftcounter.features.signal.application;

import com.exempal.shiftcounter.features.signal.domain.SignalInputPort;
import com.exempal.shiftcounter.shared.event.DomainEventPublisher;
import com.exempal.shiftcounter.shared.event.ProductDetectedEvent;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Реализация порта SignalInputPort.
 * Принимает сигналы от адаптеров и публикует бизнес-события.
 */
@Service
public class SignalService implements SignalInputPort {

    private final DomainEventPublisher eventPublisher;

    public SignalService(DomainEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void onProductSensorTriggered() {
        // Публикуем доменное событие
        eventPublisher.publish(new ProductDetectedEvent(Instant.now()));
    }
}

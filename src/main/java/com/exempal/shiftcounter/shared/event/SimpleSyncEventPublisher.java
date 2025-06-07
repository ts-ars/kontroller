package com.exempal.shiftcounter.shared.event;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class SimpleSyncEventPublisher implements DomainEventPublisher {

    private final ApplicationEventPublisher springPublisher;

    public SimpleSyncEventPublisher(ApplicationEventPublisher springPublisher) {
        this.springPublisher = springPublisher;
    }

    @Override
    public void publish(Object event) {
        springPublisher.publishEvent(event);
    }
}

package com.exempal.shiftcounter.shared.event;

public interface DomainEventPublisher {
    void publish(Object event);
}

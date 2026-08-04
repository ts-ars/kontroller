package com.exempal.shiftcounter.common.adapter;

import com.exempal.shiftcounter.common.domain.EventPublisherPort;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class SpringEventPublisherAdapter implements EventPublisherPort {

    private final ApplicationEventPublisher applicationEventPublisher;

    public SpringEventPublisherAdapter(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void publish(Object event) {
        applicationEventPublisher.publishEvent(event);
    }
}

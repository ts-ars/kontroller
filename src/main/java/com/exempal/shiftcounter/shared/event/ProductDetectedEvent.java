package com.exempal.shiftcounter.shared.event;

import lombok.Getter;

import java.time.Instant;

@Getter
public class ProductDetectedEvent implements DomainEvent {
    private final Instant detectedAt;

    public ProductDetectedEvent(Instant detectedAt) {
        this.detectedAt = detectedAt;
    }
}
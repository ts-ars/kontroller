package com.exempal.shiftcounter.shared.event;

import java.time.Instant;

public class ProductDetectedEvent implements DomainEvent {
    private final Instant detectedAt;

    public ProductDetectedEvent(Instant detectedAt) {
        this.detectedAt = detectedAt;
    }

    public Instant getDetectedAt() {
        return detectedAt;
    }
}

package com.exempal.shiftcounter.features.signal.adapter.event;

import com.exempal.shiftcounter.features.shift.application.ShiftProductRegistrar;
import com.exempal.shiftcounter.shared.event.ProductDetectedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductDetectedListener {
    private final ShiftProductRegistrar shiftProductRegistrar;

    @EventListener
    public void onProductDetected(ProductDetectedEvent event) {
        shiftProductRegistrar.registerProduct(event.sensorId().value(), event.occurredAt());
    }
}

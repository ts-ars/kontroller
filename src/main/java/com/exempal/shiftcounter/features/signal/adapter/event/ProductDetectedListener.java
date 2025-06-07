package com.exempal.shiftcounter.features.signal.adapter.event;

import com.exempal.shiftcounter.features.shift.application.ShiftPlannerUseCase;
import com.exempal.shiftcounter.shared.event.ProductDetectedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.LocalDateTime;

@Slf4j
@Component
public class ProductDetectedListener {

    private final ShiftPlannerUseCase shiftPlanner;

    public ProductDetectedListener(ShiftPlannerUseCase shiftPlanner) {
        this.shiftPlanner = shiftPlanner;
    }

    @EventListener
    public void onProductDetected(ProductDetectedEvent event) {
        LocalDateTime timestamp = event.getDetectedAt()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        log.info("[EVENT] Product detected at {}", timestamp);

        shiftPlanner.registerProduct(timestamp);
    }
}

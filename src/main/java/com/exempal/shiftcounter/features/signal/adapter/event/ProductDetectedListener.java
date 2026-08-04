package com.exempal.shiftcounter.features.signal.adapter.event;

import com.exempal.shiftcounter.features.shift.application.ShiftProductRegistrar;
import com.exempal.shiftcounter.shared.event.ProductDetectedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Slf4j
@Component
public class ProductDetectedListener {

    private final ShiftProductRegistrar shiftProductRegistrar;

    public ProductDetectedListener(ShiftProductRegistrar shiftProductRegistrar) {
        this.shiftProductRegistrar = shiftProductRegistrar;
    }

    @EventListener
    public void onProductDetected(ProductDetectedEvent event) {
        LocalDateTime timestamp = event.getDetectedAt()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        log.info("[EVENT] Product detected at {}", timestamp);

        // ✅ Делегируем всю логику в единый сервис
        shiftProductRegistrar.registerProduct(timestamp);
    }
}

package com.exempal.shiftcounter.features.signal.adapter.event;

import com.exempal.shiftcounter.features.signal.adapter.adam.AdamModbusAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.exempal.shiftcounter.shared.event.ProductDetectedEvent;


import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdamEventEmitter {

    private final AdamModbusAdapter modbusAdapter;
    private final ApplicationEventPublisher publisher;

    private boolean previousState = false;

    @Scheduled(fixedDelay = 100)
    public void pollAdam() {
        try {
            boolean currentState = modbusAdapter.readDigitalInput(0);

            if (currentState && !previousState) {
                publisher.publishEvent(new ProductDetectedEvent(Instant.now()));
                log.info("[MODBUS] Product detected — event published");
            }

            previousState = currentState;
        } catch (Exception e) {
            log.warn("[MODBUS] Polling failed: {}", e.getMessage());
        }
    }
}

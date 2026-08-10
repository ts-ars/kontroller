package com.exempal.shiftcounter.features.signal.adapter.event;

import com.exempal.shiftcounter.features.signal.adapter.adam.AdamModbusAdapter;
import com.exempal.shiftcounter.features.signal.domain.SignalInputPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("prod")
@RequiredArgsConstructor
public class AdamEventEmitter {

    private final AdamModbusAdapter modbusAdapter;
    private final SignalInputPort signals;

    private boolean previousState = false;

    @Scheduled(fixedDelay = 100)
    public void pollAdam() {
        try {
            boolean currentState = modbusAdapter.readDigitalInput(0);

            if (currentState && !previousState) {
                signals.onProductSensorTriggered();
                log.info("[MODBUS] Product detected — event published");
            }

            previousState = currentState;
        } catch (Exception e) {
            log.warn("[MODBUS] Polling failed: {}", e.getMessage());
        }
    }
}

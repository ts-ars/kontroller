package com.exempal.shiftcounter.features.signal.adapter.event;

import com.exempal.shiftcounter.features.sensor.domain.SensorId;
import com.exempal.shiftcounter.features.signal.adapter.adam.AdamModbusAdapter;
import com.exempal.shiftcounter.features.signal.domain.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Component
@Profile("prod")
public class AdamEventEmitter {
    private final AdamModbusAdapter modbusAdapter;
    private final SignalInputPort signals;
    private final Clock clock;
    private final SensorId sensorId;
    private boolean previousState;

    public AdamEventEmitter(AdamModbusAdapter modbusAdapter, SignalInputPort signals, Clock clock,
                            @Value("${adam.sensor-id:sensor-1}") String sensorId) {
        this.modbusAdapter = modbusAdapter;
        this.signals = signals;
        this.clock = clock;
        this.sensorId = SensorId.of(sensorId);
    }

    public AdamEventEmitter(AdamModbusAdapter modbusAdapter, SignalInputPort signals) {
        this(modbusAdapter, signals, Clock.systemDefaultZone(), "sensor-1");
    }

    @Scheduled(fixedDelay = 100)
    public void pollAdam() {
        try {
            boolean currentState = modbusAdapter.readDigitalInput(0);
            if (currentState && !previousState) {
                LocalDateTime occurredAt = LocalDateTime.ofInstant(clock.instant(), clock.getZone());
                signals.register(new RegisterSignalCommand(sensorId, occurredAt, SignalSource.ADAM,
                        UUID.randomUUID().toString()));
            }
            previousState = currentState;
        } catch (Exception exception) {
            log.warn("[MODBUS] Polling failed: {}", exception.getMessage());
        }
    }
}

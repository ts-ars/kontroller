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

@Slf4j
@Component
@Profile("prod")
public class AdamEventEmitter {
    private final AdamModbusAdapter modbusAdapter;
    private final CounterInputPort counters;
    private final Clock clock;
    private final SensorId sensorId;
    private final int counterChannel;

    public AdamEventEmitter(AdamModbusAdapter modbusAdapter, CounterInputPort counters, Clock clock,
                            @Value("${adam.sensor-id:sensor-1}") String sensorId,
                            @Value("${adam.counter-channel:0}") int counterChannel) {
        this.modbusAdapter = modbusAdapter;
        this.counters = counters;
        this.clock = clock;
        this.sensorId = SensorId.of(sensorId);
        this.counterChannel = counterChannel;
    }

    public AdamEventEmitter(AdamModbusAdapter modbusAdapter, CounterInputPort counters) {
        this(modbusAdapter, counters, Clock.systemDefaultZone(), "sensor-1", 0);
    }

    @Scheduled(fixedDelay = 100)
    public void pollAdam() {
        try {
            long currentCounter = modbusAdapter.readCounter(counterChannel);
            LocalDateTime readAt = LocalDateTime.ofInstant(clock.instant(), clock.getZone());
            counters.process(new CounterReadingCommand(sensorId, currentCounter, readAt));
        } catch (Exception exception) {
            log.warn("[MODBUS] Polling failed: {}", exception.getMessage());
        }
    }
}

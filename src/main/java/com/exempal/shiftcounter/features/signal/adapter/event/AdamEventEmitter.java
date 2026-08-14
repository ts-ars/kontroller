package com.exempal.shiftcounter.features.signal.adapter.event;

import com.exempal.shiftcounter.features.signal.application.CounterInputPort;
import com.exempal.shiftcounter.features.sensor.domain.SensorId;
import com.exempal.shiftcounter.features.signal.adapter.adam.AdamModbusAdapter;
import com.exempal.shiftcounter.features.signal.adapter.adam.AdamProperties;
import com.exempal.shiftcounter.features.signal.domain.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;

@Slf4j
@Component
@Profile({"prod", "test-adam"})
public class AdamEventEmitter {
    private final AdamModbusAdapter modbusAdapter;
    private final CounterInputPort counters;
    private final Clock clock;
    private final AdamProperties properties;

    public AdamEventEmitter(AdamModbusAdapter modbusAdapter, CounterInputPort counters, Clock clock,
                            AdamProperties properties) {
        this.modbusAdapter = modbusAdapter;
        this.counters = counters;
        this.clock = clock;
        this.properties = properties;
    }

    @Scheduled(fixedDelayString = "${adam.poll-delay}")
    public void pollAdam() {
        if (!properties.enabled()) return;
        properties.devices().forEach(this::poll);
    }

    private void poll(AdamProperties.Device device) {
        try {
            long currentCounter = modbusAdapter.readCounter(device);
            LocalDateTime readAt = LocalDateTime.ofInstant(clock.instant(), clock.getZone());
            CounterProcessingResult result = counters.process(new CounterReadingCommand(
                    SensorId.of(device.sensorId()), currentCounter, readAt));
            log.info("sensor={} counter={} delta={} acceptedSignals={} productionDate={} result={}",
                    device.sensorId(), currentCounter, result.delta(), result.acceptedSignals(),
                    result.attributedProductionDate(), result.status());
        } catch (Exception exception) {
            log.warn("sensor={} result=poll-failed reason={}", device.sensorId(), exception.getMessage());
        }
    }
}

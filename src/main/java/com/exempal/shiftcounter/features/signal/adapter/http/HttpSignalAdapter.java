package com.exempal.shiftcounter.features.signal.adapter.http;

import com.exempal.shiftcounter.features.signal.application.SignalInputPort;
import com.exempal.shiftcounter.features.sensor.domain.SensorId;
import com.exempal.shiftcounter.features.signal.domain.*;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@Profile("test")
@RequestMapping("/api/signal")
public class HttpSignalAdapter {
    private final SignalInputPort signalInput;
    private final Clock clock;

    public HttpSignalAdapter(SignalInputPort signalInput, Clock clock) {
        this.signalInput = signalInput;
        this.clock = clock;
    }

    @PostMapping("/product/{sensorId}")
    public SignalRegistrationResult productSignal(@PathVariable String sensorId) {
        LocalDateTime occurredAt = LocalDateTime.ofInstant(clock.instant(), clock.getZone());
        return signalInput.register(new RegisterSignalCommand(SensorId.of(sensorId), occurredAt,
                SignalSource.HTTP_SIMULATION, UUID.randomUUID().toString()));
    }
}

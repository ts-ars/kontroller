package com.exempal.shiftcounter.features.signal.adapter.web;

import com.exempal.shiftcounter.features.signal.domain.SignalInputPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Обрабатывает сигнал от сенсора и публикует событие в систему.
 */
@RestController
@Profile("test")
@RequestMapping("/api/signal")
public class SignalController {

    private static final Logger log = LoggerFactory.getLogger(SignalController.class);

    private final SignalInputPort signalInputPort;

    public SignalController(SignalInputPort signalInputPort) {
        this.signalInputPort = signalInputPort;
    }

    @PostMapping
    public ResponseEntity<Void> triggerSignal(
            @RequestParam String shiftDate,
            @RequestParam String sensor
    ) {
        log.info("📡 Сигнал получен: сенсор = {}, дата смены = {}", sensor, shiftDate);
        signalInputPort.onProductSensorTriggered();
        return ResponseEntity.ok().build();
    }
}

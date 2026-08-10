package com.exempal.shiftcounter.features.shift.adapter.event;

import com.exempal.shiftcounter.features.shift.domain.ShiftUpdatedEvent;
import com.exempal.shiftcounter.features.shift.application.projection.ShiftProjectionUseCase;
import com.exempal.shiftcounter.features.shift.application.projection.ShiftView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class ShiftUpdatedListener {

    private static final Logger log = LoggerFactory.getLogger(ShiftUpdatedListener.class);

    private final SimpMessagingTemplate messaging;
    private final ShiftProjectionUseCase projection;

    public ShiftUpdatedListener(SimpMessagingTemplate messaging, ShiftProjectionUseCase projection) {
        this.messaging = messaging;
        this.projection = projection;
    }

    @EventListener
    public void handle(ShiftUpdatedEvent event) {
        ShiftView view = projection.buildView(event.date(), event.sensorId());
        messaging.convertAndSend("/topic/shift-updates/" + event.sensorId(), view);
        if (event.sensorId().equals("sensor-1")) messaging.convertAndSend("/topic/shift-updates", view);
        log.info("📤 Отправлен ShiftView в /topic/shift-updates: {}", view);
    }
}

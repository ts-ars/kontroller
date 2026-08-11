package com.exempal.shiftcounter.features.comment.adapter.event;


import com.exempal.shiftcounter.features.comment.application.event.CommentsUpdatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CommentsUpdatedListener {

    private final SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void onCommentsUpdated(CommentsUpdatedEvent event) {
        Map<String, String> payload = Map.of(
                "date", event.date().toString(),
                "sensorId", event.sensorId());
        messagingTemplate.convertAndSend("/topic/comments/" + event.sensorId(), payload);
        if (List.of("sensor-1", "sensor-2", "sensor-3", "sensor-4").contains(event.sensorId())) {
            messagingTemplate.convertAndSend("/topic/comments/sensor-5", payload);
        }
    }
}

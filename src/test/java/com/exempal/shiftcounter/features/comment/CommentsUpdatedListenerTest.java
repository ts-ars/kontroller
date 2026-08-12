package com.exempal.shiftcounter.features.comment.adapter.event;

import com.exempal.shiftcounter.features.comment.application.event.CommentsUpdatedEvent;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDate;
import java.util.Map;

import static org.mockito.Mockito.*;

class CommentsUpdatedListenerTest {
    @Test
    void sourceSensorRefreshesItselfAndSensorFiveAggregation() {
        SimpMessagingTemplate messaging = mock(SimpMessagingTemplate.class);
        var listener = new CommentsUpdatedListener(messaging);
        var event = new CommentsUpdatedEvent(LocalDate.of(2026, 8, 7), "sensor-3");

        listener.onCommentsUpdated(event);

        Map<String, String> payload = Map.of("date", "2026-08-07", "sensorId", "sensor-3");
        verify(messaging).convertAndSend("/topic/comments/sensor-3", payload);
        verify(messaging).convertAndSend("/topic/comments/sensor-5", payload);
    }

    @Test
    void sensorSixRefreshesOnlyItsOwnTopic() {
        SimpMessagingTemplate messaging = mock(SimpMessagingTemplate.class);
        var listener = new CommentsUpdatedListener(messaging);

        listener.onCommentsUpdated(new CommentsUpdatedEvent(LocalDate.of(2026, 8, 7), "sensor-6"));

        verify(messaging).convertAndSend(eq("/topic/comments/sensor-6"), anyMap());
        verify(messaging, never()).convertAndSend(eq("/topic/comments/sensor-5"), anyMap());
    }
}

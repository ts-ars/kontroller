package com.exempal.shiftcounter.features.comment.adapter.event;


import com.exempal.shiftcounter.features.comment.application.event.CommentsUpdatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class CommentsUpdatedListener {

    private final SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void onCommentsUpdated(CommentsUpdatedEvent event) {
        messagingTemplate.convertAndSend("/topic/shift-comments", Map.of(
                "date", event.date().toString(),
                "comments", event.comments()
        ));
    }
}
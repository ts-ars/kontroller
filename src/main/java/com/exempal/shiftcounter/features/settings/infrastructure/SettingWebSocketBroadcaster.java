package com.exempal.shiftcounter.features.settings.infrastructure;

import com.exempal.shiftcounter.features.settings.domain.SettingUpdatedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class SettingWebSocketBroadcaster {

    private final SimpMessagingTemplate messaging;

    public SettingWebSocketBroadcaster(SimpMessagingTemplate messaging) {
        this.messaging = messaging;
    }

    @EventListener
    public void onSettingUpdated(SettingUpdatedEvent event) {
        messaging.convertAndSend("/topic/settings", "updated");
    }
}

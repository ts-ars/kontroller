package com.exempal.shiftcounter.features.shift.adapter.event;

import com.exempal.shiftcounter.features.shift.domain.ShiftUpdatedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ShiftUpdatedListener {

    @EventListener
    public void handle(ShiftUpdatedEvent event) {
        System.out.println("🎯 Обновлена смена: " + event.shift());

        // TODO: Здесь можно:
        // - Сохранить обновлённую смену в БД (если потребуется),
        // - Отправить уведомление по WebSocket,
        // - Обновить кеш или отправить метрику
    }
}

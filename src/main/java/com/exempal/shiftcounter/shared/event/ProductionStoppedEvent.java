package com.exempal.shiftcounter.shared.event;

import java.time.LocalDateTime;

/**
 * 📦 Доменное событие, сигнализирующее об остановке производства.
 *
 * Это событие публикуется при получении сигнала остановки с устройства (например, ADAM-6050)
 * и используется подписчиком (ProductionStoppedListener) для автоматического создания
 * черновика строки остановки — StoppageEntry — в базе данных.
 *
 * ⚙️ Пример использования:
 *   eventPublisher.publishEvent(new ProductionStoppedEvent(...));
 */
public class ProductionStoppedEvent implements DomainEvent {

    /**
     * Время начала остановки (с точностью до секунд).
     */
    private final LocalDateTime time;

    /**
     * Длительность остановки в минутах (например, 2.5 = 2 мин 30 сек).
     */
    private final double minutes;

    public ProductionStoppedEvent(LocalDateTime time, double minutes) {
        this.time = LocalDateTime.from(time);
        this.minutes = minutes;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public double getMinutes() {
        return minutes;
    }
}


package com.exempal.shiftcounter.features.signal.domain;

/**
 * Входной порт для получения сигналов с железа.
 * Вызывается, когда срабатывает внешний сенсор (например, ADAM-6050 или ESP32).
 */
public interface SignalInputPort {
    void onProductSensorTriggered();  // можно расширить на другие типы сигналов
}

package com.exempal.shiftcounter.features.signal.domain;

/**
 * Порт для взаимодействия с устройством ADAM-6050 по Modbus TCP.
 * Позволяет считывать состояние входов и управлять выходами.
 */
public interface AdamIoPort {

    /**
     * Считывает состояние цифрового входа (DI).
     * @param channel номер канала (например, 0–11)
     * @return true, если сигнал есть
     */
    boolean readInput(int channel);

    /**
     * Устанавливает состояние цифрового выхода (DO).
     * @param channel номер канала (например, 0–5)
     * @param value значение (true = HIGH, false = LOW)
     */
    void writeOutput(int channel, boolean value);
}

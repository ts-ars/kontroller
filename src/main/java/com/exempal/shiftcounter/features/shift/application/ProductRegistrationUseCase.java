package com.exempal.shiftcounter.features.shift.application;

import java.time.LocalDateTime;

public interface ProductRegistrationUseCase {
    void registerProduct(String sensorId, LocalDateTime timestamp);
}

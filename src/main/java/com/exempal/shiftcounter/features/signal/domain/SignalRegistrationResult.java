package com.exempal.shiftcounter.features.signal.domain;

import java.util.UUID;
import com.exempal.shiftcounter.features.sensor.domain.SensorId;

public record SignalRegistrationResult(UUID signalId, SensorId sensorId, boolean accepted) {
    public static SignalRegistrationResult duplicate(SensorId sensorId) {
        return new SignalRegistrationResult(null, sensorId, false);
    }
}

package com.exempal.shiftcounter.features.shift.application;

public interface ShiftSettingsPort {
    ShiftSettings getForSensor(String sensorId);
}

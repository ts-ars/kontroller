package com.exempal.shiftcounter.features.signal.application;

import java.time.LocalDate;

public interface SignalRegistrationLock {
    void acquire(LocalDate productionDate, String sensorId);
}

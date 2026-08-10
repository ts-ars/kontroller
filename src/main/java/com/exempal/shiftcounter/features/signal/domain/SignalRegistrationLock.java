package com.exempal.shiftcounter.features.signal.domain;

import java.time.LocalDate;

public interface SignalRegistrationLock {
    void acquire(LocalDate productionDate, String sensorId);
}

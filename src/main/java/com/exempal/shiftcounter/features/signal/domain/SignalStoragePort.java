package com.exempal.shiftcounter.features.signal.domain;

import java.time.LocalDateTime;
import java.util.List;

public interface SignalStoragePort {
    boolean saveIfAbsent(Signal signal);

    default void save(Signal signal) {
        saveIfAbsent(signal);
    }
    List<Signal> findBySensorAndRange(String sensorId, LocalDateTime from, LocalDateTime to);

    default List<Signal> findByRange(LocalDateTime from, LocalDateTime to) {
        return findBySensorAndRange("sensor-1", from, to);
    }
}

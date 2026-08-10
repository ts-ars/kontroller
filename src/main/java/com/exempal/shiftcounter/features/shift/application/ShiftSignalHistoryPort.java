package com.exempal.shiftcounter.features.shift.application;

import java.time.LocalDateTime;
import java.util.List;

public interface ShiftSignalHistoryPort {
    List<LocalDateTime> findTimestamps(String sensorId, LocalDateTime from, LocalDateTime to);
}

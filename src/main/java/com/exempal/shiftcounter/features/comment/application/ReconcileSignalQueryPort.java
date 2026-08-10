package com.exempal.shiftcounter.features.comment.application;

import java.time.LocalDateTime;
import java.util.List;

public interface ReconcileSignalQueryPort {
    List<LocalDateTime> findTimestamps(String sensorId, LocalDateTime from, LocalDateTime to);
}

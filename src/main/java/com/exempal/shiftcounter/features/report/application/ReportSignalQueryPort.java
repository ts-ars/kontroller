package com.exempal.shiftcounter.features.report.application;

import java.time.LocalDateTime;

public interface ReportSignalQueryPort {
    long count(String sensorId, LocalDateTime fromInclusive, LocalDateTime toExclusive);
}

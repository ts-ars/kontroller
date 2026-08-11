package com.exempal.shiftcounter.features.shift.application.projection;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface ShiftExplanationPort {
    Map<Integer, List<IntervalExplanationView>> findByInterval(LocalDate date, String sensorId);
}

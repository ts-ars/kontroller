package com.exempal.shiftcounter.features.comment.application;

import com.exempal.shiftcounter.features.comment.domain.Stoppage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface StoppageRepository {
    Optional<Stoppage> findById(long id);
    Optional<Stoppage> findForUpdateById(long id);
    List<Stoppage> findByShiftDateAndSensorId(LocalDate date, String sensorId);
    List<Stoppage> findByShiftDateBetweenAndSensorId(LocalDate from, LocalDate to, String sensorId);
    List<Stoppage> findActiveByShiftAndInterval(long shiftId, int intervalIndex);
    List<Stoppage> findActiveByShiftSensorAndIntervalRange(long shiftId, String sensorKey,
                                                            int fromInterval, int toInterval);
    Stoppage save(Stoppage stoppage);
    List<Stoppage> saveAll(List<Stoppage> stoppages);
}

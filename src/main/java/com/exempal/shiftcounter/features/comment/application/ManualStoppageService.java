package com.exempal.shiftcounter.features.comment.application;

import com.exempal.shiftcounter.features.comment.domain.*;
import com.exempal.shiftcounter.features.sensor.domain.SensorCatalog;
import com.exempal.shiftcounter.features.shift.application.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ManualStoppageService {
    private final ActualDataPort shifts;
    private final ShiftIntervalService intervals;
    private final StoppageRepository stoppages;
    private final CurrentCommentActor actors;
    private final ProductionDayService productionDays;
    private final ShiftPlannerUseCase shiftPlanner;

    @Transactional
    public Stoppage create(LocalDate date, String sensorId, int intervalIndex, LossCategory category,
                           String comment, int minutes, int cans) {
        SensorCatalog.require(sensorId);
        if (SensorCatalog.SENSOR_5.equals(sensorId)) throw new IllegalArgumentException("Sensor 5 is read only");
        if (minutes <= 0) throw new IllegalArgumentException("Minutes must be positive");
        var shift = shifts.findByDateAndSensorId(date, sensorId)
                .orElseGet(() -> shiftPlanner.getOrCreateShift(date, sensorId));
        if (shift.getId() == null) {
            shift = shifts.findByDateAndSensorId(date, sensorId)
                    .orElseThrow(() -> new IllegalStateException("Created shift was not persisted"));
        }
        var timeline = intervals.resolve(date, shift.getHourlyLabels(), shift.getHourlyPlanValues().size());
        if (intervalIndex < 0 || intervalIndex >= timeline.size()) throw new IllegalArgumentException("Invalid interval");
        var interval = timeline.get(intervalIndex);
        if (date.equals(productionDays.current().date()) && interval.start().isAfter(productionDays.now())) {
            throw new IllegalArgumentException("Future interval is not available");
        }
        if (minutes > interval.duration().toMinutes()) throw new IllegalArgumentException("Minutes exceed interval duration");
        int planned = shift.getHourlyPlanValues().get(intervalIndex);
        int calculatedCans = (int) Math.round((double) planned * minutes / 60.0);
        if (calculatedCans <= 0) throw new IllegalArgumentException("Calculated cans must be positive");
        var saved = stoppages.save(Stoppage.detected(UUID.randomUUID(), shift.getId(), sensorId, intervalIndex,
                interval.start(), Duration.ofMinutes(minutes), calculatedCans, DetectionType.MANUAL));
        if (comment != null && !comment.isBlank()) {
            var actor = actors.require();
            saved = stoppages.save(saved.addExplanation(category, comment, minutes, actor.userId(),
                    actor.displayName(), productionDays.now().atZone(java.time.ZoneId.systemDefault()).toInstant()));
        }
        return saved;
    }
}

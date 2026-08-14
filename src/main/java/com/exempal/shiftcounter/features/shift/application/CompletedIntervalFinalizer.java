package com.exempal.shiftcounter.features.shift.application;

import com.exempal.shiftcounter.features.sensor.domain.SensorCatalog;
import com.exempal.shiftcounter.features.shift.domain.Shift;
import com.exempal.shiftcounter.features.shift.domain.ShiftInterval;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class CompletedIntervalFinalizer {
    private final ProductionDayService productionDays;
    private final ActualDataPort shifts;
    private final ShiftIntervalService intervals;
    private final ShiftReconcilePort reconciles;

    @Scheduled(fixedDelayString = "${stoppages.finalize-delay:30000}")
    public void finalizeCompletedIntervals() {
        LocalDateTime now = productionDays.now();
        var productionDate = productionDays.resolve(now).date();
        for (var sensor : SensorCatalog.all()) {
            String sensorId = sensor.id().value();
            if (SensorCatalog.SENSOR_5.equals(sensorId)) continue;
            shifts.findByDateAndSensorId(productionDate, sensorId)
                    .ifPresent(shift -> finalizeShift(shift, now));
        }
    }

    private void finalizeShift(Shift shift, LocalDateTime now) {
        for (ShiftInterval interval : intervals.resolve(shift.getDate(), shift.getHourlyLabels(),
                shift.getHourlyPlanValues().size())) {
            if (interval.planSupplied() && !interval.end().isAfter(now)) {
                reconciles.reconcile(shift.getDate(), shift.getSensorId(), interval.index(), now);
            }
        }
    }
}

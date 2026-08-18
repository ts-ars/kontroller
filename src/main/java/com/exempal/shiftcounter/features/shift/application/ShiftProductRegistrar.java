package com.exempal.shiftcounter.features.shift.application;

import com.exempal.shiftcounter.features.sensor.domain.SensorCatalog;
import com.exempal.shiftcounter.features.shift.domain.Shift;
import com.exempal.shiftcounter.features.shift.domain.ShiftInterval;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShiftProductRegistrar implements ProductRegistrationUseCase {

    private final ShiftPlannerUseCase shiftPlanner;
    private final ShiftExtenderService extender;
    private final ProductionDayService productionDays;
    private final ShiftIntervalService intervals;
    // Kept as a constructor dependency for compatibility; reconciliation is now
    // performed only by CompletedIntervalFinalizer after an interval has ended.
    private final ShiftReconcilePort reconciles;

    @Transactional
    public void registerProduct(LocalDateTime timestamp) {
        registerProduct(SensorCatalog.SENSOR_1, timestamp);
    }

    @Override
    @Transactional
    public void registerProduct(String sensorId, LocalDateTime timestamp) {
        SensorCatalog.require(sensorId);
        var productionDay = productionDays.resolve(timestamp);
        Shift current = shiftPlanner.getOrCreateShift(productionDay.date(), sensorId);
        Shift extended = extender.extendIfNeeded(timestamp, current);
        ShiftInterval interval = intervals.find(productionDay.date(), extended.getHourlyLabels(),
                        extended.getHourlyPlanValues().size(), timestamp)
                .orElse(null);
        if (interval == null) {
            log.warn("Signal outside active shift window: ts={}, labels={}, productionDate={}",
                    timestamp, extended.getHourlyLabels(), productionDay.date());
            return;
        }

        Shift updated = extended.withIncrementedHourlyActualValue(interval.index());
        shiftPlanner.updateShift(updated);
        log.info("sensor={} productionDate={} interval={} result=actual-incremented",
                sensorId, productionDay.date(), interval.index());
    }
}

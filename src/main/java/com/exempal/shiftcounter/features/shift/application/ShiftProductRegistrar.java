package com.exempal.shiftcounter.features.shift.application;

import com.exempal.shiftcounter.features.comment.application.ReconcileStoppagesCommand;
import com.exempal.shiftcounter.features.comment.application.ReconcileStoppagesUseCase;
import com.exempal.shiftcounter.features.comment.domain.Stoppage;
import com.exempal.shiftcounter.features.settings.infrastructure.ShiftSettingsApplier;
import com.exempal.shiftcounter.features.shift.domain.Shift;
import com.exempal.shiftcounter.features.shift.domain.ShiftInterval;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShiftProductRegistrar {

    private final ShiftPlannerUseCase shiftPlanner;
    private final ShiftExtenderService extender;
    private final ProductionDayService productionDays;
    private final ShiftIntervalService intervals;
    private final ShiftSettingsApplier shiftSettingsApplier;
    private final ReconcileStoppagesUseCase reconciles;

    public void registerProduct(LocalDateTime timestamp) {
        var productionDay = productionDays.resolve(timestamp);
        Shift current = shiftSettingsApplier.applyIfChanged(
                shiftPlanner.getOrCreateShift(productionDay.date()), timestamp);
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
        if (interval.planSupplied()) {
            reconciles.reconcile(new ReconcileStoppagesCommand(productionDay.date(),
                    Stoppage.PRIMARY_SENSOR, interval.index(), timestamp));
        }
    }
}

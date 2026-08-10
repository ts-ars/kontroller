package com.exempal.shiftcounter.features.shift.application;

import com.exempal.shiftcounter.features.comment.application.ReconcileStoppagesCommand;
import com.exempal.shiftcounter.features.comment.application.ReconcileStoppagesUseCase;
import com.exempal.shiftcounter.features.comment.domain.Stoppage;
import com.exempal.shiftcounter.features.settings.infrastructure.ShiftSettingsApplier;
import com.exempal.shiftcounter.features.shift.domain.Shift;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShiftProductRegistrar {

    private final ShiftPlannerUseCase shiftPlanner;
    private final ShiftExtenderService extender;
    private final ShiftTimeHelper timeHelper;
    private final ShiftSettingsApplier shiftSettingsApplier;
    private final ReconcileStoppagesUseCase reconciles;

    public void registerProduct(LocalDateTime timestamp) {
        LocalDate today = timestamp.toLocalDate();

        // 1) СЕГОДНЯ
        Shift todayShift = shiftSettingsApplier.applyIfChanged(shiftPlanner.getOrCreateShift(today));
        Shift todayExt   = extender.extendIfNeeded(timestamp, todayShift);

        if (timeHelper.contains(today, todayExt.getHourlyLabels(), timestamp)) {
            int idx = timeHelper.resolveHourIndex(today, todayExt.getHourlyLabels(), timestamp);
            if (idx >= 0) {
                Shift updated = todayExt.withIncrementedHourlyActualValue(idx);
                shiftPlanner.applySettingsAndUpdate(updated);
                reconciles.reconcile(new ReconcileStoppagesCommand(today, Stoppage.PRIMARY_SENSOR, idx, timestamp));
                return;
            }
        }

        // 2) ВЧЕРА — только если ts до первого сегодняшнего лейбла
        LocalTime firstToday = LocalTime.parse(todayExt.getHourlyLabels().get(0));
        if (timestamp.toLocalTime().isBefore(firstToday)) {
            LocalDate y = today.minusDays(1);

            // если нельзя создавать задним числом — замени на порт: actualDataPort.findByDate(y)
            Shift yShift = shiftPlanner.getOrCreateShift(y);
            Shift yExt   = extender.extendIfNeeded(timestamp, yShift);

            if (timeHelper.contains(y, yExt.getHourlyLabels(), timestamp)) {
                int idx = timeHelper.resolveHourIndex(y, yExt.getHourlyLabels(), timestamp);
                if (idx >= 0) {
                    Shift updated = yExt.withIncrementedHourlyActualValue(idx);
                    shiftPlanner.applySettingsAndUpdate(updated);
                    reconciles.reconcile(new ReconcileStoppagesCommand(y, Stoppage.PRIMARY_SENSOR, idx, timestamp));
                    return;
                }
            }
        }

        // 3) ВНЕ ОКОН — не валим поток
        log.warn("🔸 Signal outside shift windows: ts={}, todayLabels={}, date={}",
                timestamp, todayExt.getHourlyLabels(), today);
    }
}

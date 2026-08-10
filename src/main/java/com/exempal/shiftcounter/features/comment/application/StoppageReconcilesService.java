package com.exempal.shiftcounter.features.comment.application;

import com.exempal.shiftcounter.features.comment.calculator.StoppageCalculator;
import com.exempal.shiftcounter.features.comment.domain.Stoppage;
import com.exempal.shiftcounter.features.settings.infrastructure.ShiftSettingsApplier;
import com.exempal.shiftcounter.features.shift.application.ShiftPlannerUseCase;
import com.exempal.shiftcounter.features.shift.application.ShiftTimeHelper;
import com.exempal.shiftcounter.features.shift.domain.Shift;
import com.exempal.shiftcounter.features.shift.domain.ShiftMetricsCalculator;
import com.exempal.shiftcounter.features.shift.infrastructure.ShiftEntity;
import com.exempal.shiftcounter.features.shift.infrastructure.ShiftJpaRepository;
import com.exempal.shiftcounter.features.signal.application.SignalService;
import com.exempal.shiftcounter.features.signal.domain.Signal;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StoppageReconcilesService {

    private final ShiftPlannerUseCase shiftPlanner;
    private final ShiftSettingsApplier shiftSettingsApplier;
    private final ShiftTimeHelper timeHelper;
    private final SignalService signalService;
    private final ShiftMetricsCalculator metricsCalculator;
    private final StoppageCalculator stoppageCalculator;
    private final ShiftJpaRepository shiftJpaRepository;
    private final StoppageRepository stoppageRepository;

    /**
     * Пересчитать FIXED/TEMPO для конкретного часа (hourIndex) конкретной даты.
     * <p>
     * Контракт по времени:
     * - intervalStartInclusive — включительно
     * - intervalEndExclusive — исключая правую границу
     * - SignalService.getSignalsBetween(start, end) сейчас принимает ИСКЛЮЧИТЕЛЬНО конец,
     * поэтому мы передаём endExclusive.minusNanos(1), если ваш сервис ожидает включительный конец.
     * <p>
     * Идемпотентность:
     * - перед сохранением удаляем прежние авто-строки (FIXED/TEMPO) за этот час.
     * - вызов можно повторять без побочных эффектов.
     */
    @Transactional
    public void reconcileHour(LocalDate shiftDate, int hourIndex, LocalDateTime now) {
        // 1) Блокируем смену на дату, чтобы избежать гонок с инкрементом факта
        ShiftEntity shiftEntity = shiftJpaRepository.findForUpdateByDate(shiftDate)
                .orElseThrow(() -> new IllegalStateException(STR."ShiftEntity not found for date: \{shiftDate}"));

        // 2) Получаем доменную смену с актуальными настройками
        Shift shift = shiftSettingsApplier.applyIfChanged(shiftPlanner.getOrCreateShift(shiftDate));

        // 2.1) Пересчитываем индекс по фактическим labels и текущему ts
        List<String> labels = shift.getHourlyLabels();
        int idx = timeHelper.resolveHourIndex(shiftDate, labels, now);
        if (idx < 0 || idx >= labels.size()) {
            log.warn("Skip reconcile: ts={}, computedIdx={}, labelsSize={}, date={}", now, idx, labels.size(), shiftDate);
            return;
        }

        // 3) Границы интервала по пересчитанному индексу
        LocalDateTime intervalStartInclusive = timeHelper.resolveStartTime(labels.get(idx), shiftDate);
        LocalDateTime intervalEndExclusive   = timeHelper.resolveEndTime(labels, idx, shiftDate);

        // 4) Сигналы строго внутри [start, end)
        List<Signal> signals = signalService.getSignalsBetween(
                intervalStartInclusive, intervalEndExclusive.minusNanos(1));

        // 5) Метрики считаем по фактическим labels смены
        var metrics = metricsCalculator.calculateFor(
                shiftSettingsApplier.getCurrentSettings(),
                labels
        );

        // 6) Расчёт FIXED/TEMPO
        List<Stoppage> newAutoStoppages = stoppageCalculator.recalculate(
                shift, idx, signals, metrics, now);

        // Stage 3 compatibility: retain history. Stable matching/update-in-place belongs to Stage 4.
        List<Stoppage> previous = stoppageRepository.findActiveByShiftAndInterval(shiftEntity.getId(), idx);
        stoppageRepository.saveAll(previous.stream().map(Stoppage::resolve).toList());
        stoppageRepository.saveAll(newAutoStoppages);

        log.info("Reconciled date={}, hourIndex={} -> saved {} auto-stoppage(s)",
                shiftDate, idx, newAutoStoppages.size());
    }
}

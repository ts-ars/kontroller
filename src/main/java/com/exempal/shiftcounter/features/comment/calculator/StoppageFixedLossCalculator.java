package com.exempal.shiftcounter.features.comment.calculator;

import com.exempal.shiftcounter.features.comment.application.StoppageDetector;
import com.exempal.shiftcounter.features.comment.domain.StoppageEntry;
import com.exempal.shiftcounter.features.shift.infrastructure.ShiftEntity;
import com.exempal.shiftcounter.features.signal.domain.Signal;

import java.time.Duration;
import java.util.List;

/**
 * Вычисляет FIXED-потери на основе пропущенных сигналов.
 * Теперь использует StoppageDetector + minGap.
 */
public class StoppageFixedLossCalculator {

    private final StoppageDetector detector;
    private final Duration minGap;

    public StoppageFixedLossCalculator(StoppageDetector detector, Duration minGap) {
        if (minGap == null || minGap.isZero() || minGap.isNegative()) {
            throw new IllegalArgumentException("minGap must be positive");
        }
        this.detector = detector;
        this.minGap = minGap;
    }

    /**
     * Возвращает список FIXED-остановок с вычисленными банками.
     */
    public List<StoppageEntry> calculateFixed(
            ShiftEntity shift,
            int hourIndex,
            List<Signal> signals,
            double cansPerMinute
    ) {
        // детектируем FIXED с учётом границ часа и порога
        List<StoppageEntry> fixed = detector.detectFixedLosses(shift, hourIndex, signals, minGap);

        // банки выставляем по CPM
        fixed.forEach(e -> e.setCans((int) Math.round(e.getMinutes() * cansPerMinute)));
        return fixed;
    }
}
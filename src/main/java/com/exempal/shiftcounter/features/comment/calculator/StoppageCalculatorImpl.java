package com.exempal.shiftcounter.features.comment.calculator;

import com.exempal.shiftcounter.features.shift.domain.Shift;
import com.exempal.shiftcounter.features.shift.domain.ShiftMetrics;
import com.exempal.shiftcounter.features.comment.domain.Stoppage;
import com.exempal.shiftcounter.features.signal.domain.Signal;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Главный калькулятор остановок.
 * Делегирует вычисление fixed/tempo потерь и обработку пользовательских комментариев.
 */
public class StoppageCalculatorImpl implements StoppageCalculator {

    private final StoppageFixedLossCalculator fixedLossCalculator;
    private final StoppageTempoLossCalculator tempoLossCalculator;

    public StoppageCalculatorImpl(
            StoppageFixedLossCalculator fixedLossCalculator,
            StoppageTempoLossCalculator tempoLossCalculator
    ) {
        this.fixedLossCalculator = fixedLossCalculator;
        this.tempoLossCalculator = tempoLossCalculator;
    }

    @Override
    public List<Stoppage> recalculate(
            Shift shift,
            int hourIndex,
            List<Signal> signals,
            ShiftMetrics metrics,
            LocalDateTime now
    ) {
        double cansPerMinute = metrics.canPerMinute().get(hourIndex);
        int actual = shift.getHourlyActualValues().get(hourIndex);

        List<Stoppage> fixed = fixedLossCalculator.calculateFixed(
                shift, hourIndex, signals, cansPerMinute
        );
        int fixedCans = fixed.stream().mapToInt(Stoppage::lostCans).sum();

        List<Stoppage> result = new ArrayList<>(fixed);

        tempoLossCalculator.calculateTempo(
                shift, hourIndex, actual, fixedCans, cansPerMinute, now
        ).ifPresent(result::add);

        return result;
    }

}

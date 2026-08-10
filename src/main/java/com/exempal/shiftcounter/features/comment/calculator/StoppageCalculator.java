package com.exempal.shiftcounter.features.comment.calculator;

import com.exempal.shiftcounter.features.shift.domain.Shift;
import com.exempal.shiftcounter.features.shift.domain.ShiftMetrics;
import com.exempal.shiftcounter.features.comment.domain.Stoppage;
import com.exempal.shiftcounter.features.signal.domain.Signal;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Контракт калькулятора остановок по сигналам и пользовательским комментариям.
 */
public interface StoppageCalculator {

    List<Stoppage> recalculate(
            Shift shift,
            int hourIndex,
            List<Signal> signals,
            ShiftMetrics metrics,
            LocalDateTime now
    );

}

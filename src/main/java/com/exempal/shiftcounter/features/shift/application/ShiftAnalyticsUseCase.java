package com.exempal.shiftcounter.features.shift.application;

import org.springframework.stereotype.Component;

/**
 * Use-case для расчёта производственных метрик по сменам:
 * - потери (lost cans),
 * - план-факт отклонения,
 * - эффективность,
 * - и другие KPI в будущем.
 */

@Component
public class ShiftAnalyticsUseCase {

    /**
     * Расчёт потерь при остановке на основе длительности простоя и скорости линии.
     *
     * @param downtimeMinutes длительность простоя (в минутах)
     * @param cansPerMinute   скорость линии (банок в минуту)
     * @return количество потерянных банок
     */
    public int calculateLostCans(double downtimeMinutes, double cansPerMinute) {
        return (int) (downtimeMinutes * cansPerMinute);
    }

    /**
     * Расчёт эффективности работы (факт/план в процентах).
     *
     * @param actual фактически произведено
     * @param plan   плановое значение
     * @return эффективность (0–100%)
     */
    public int calculateEfficiency(int actual, int plan) {
        if (plan == 0) return 0;
        return (int) ((actual / (double) plan) * 100);
    }

    // сюда же потом можно добавить OEE, TE, отклонения, коэффициенты загрузки и т.д.
}

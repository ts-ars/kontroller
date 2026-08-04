package com.exempal.shiftcounter.features.comment.application;

import com.exempal.shiftcounter.features.shift.domain.ShiftMetrics;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class LossMetricsNormalizer {

    /**
     * Выравнивает CPM для расчёта потерь:
     * - базовая длительность = самая частая среди слотов с plan>0;
     * - referenceCpm = медиана CPM среди слотов базовой длительности;
     * - у слотов с другой длительностью CPM заменяется на referenceCpm.
     * Планы, длительности и метки НЕ меняются.
     */
    public ShiftMetrics normalize(ShiftMetrics raw) {
        int n = raw.labels().size();
        List<Integer> durations = raw.durations();
        List<Integer> plans     = raw.plans();
        List<Double>  cpms      = raw.canPerMinute();

        // 1) валидные индексы (есть план и ненулевой cpm)
        List<Integer> valid = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            boolean hasPlan = i < plans.size() && plans.get(i) > 0;
            boolean hasCpm  = i < cpms.size()  && cpms.get(i) > 0.0;
            if (hasPlan && hasCpm) valid.add(i);
        }
        if (valid.isEmpty()) return raw;

        // 2) базовая длительность = мода (при равенстве берём большую — детерминированно)
        Map<Integer,Integer> countByDur = new HashMap<>();
        for (int i : valid) countByDur.merge(durations.get(i), 1, Integer::sum);

        int baseDuration = -1, best = -1;
        for (var e : countByDur.entrySet()) {
            int d = e.getKey(), c = e.getValue();
            if (c > best || (c == best && d > baseDuration)) { best = c; baseDuration = d; }
        }

        // 3) reference CPM = медиана CPM по базовой длительности
        List<Double> baseCpms = new ArrayList<>();
        for (int i : valid) if (durations.get(i) == baseDuration) baseCpms.add(cpms.get(i));
        if (baseCpms.isEmpty()) return raw;
        Collections.sort(baseCpms);
        double ref = median(baseCpms);

        // 4) подменяем CPM у нестандартных длительностей
        List<Double> adjusted = new ArrayList<>(cpms);
        for (int i = 0; i < n; i++) {
            double c = (i < cpms.size()) ? cpms.get(i) : 0.0;
            if (i < durations.size() && durations.get(i) != baseDuration && c > 0.0) {
                adjusted.set(i, ref);
            }
        }

        // Возвращаем новый ShiftMetrics с теми же labels/plans/durations и новым списком CPM
        return new ShiftMetrics(raw.labels(), raw.plans(), raw.durations(), adjusted);
    }

    private static double median(List<Double> asc) {
        int m = asc.size();
        return (m % 2 == 1) ? asc.get(m/2) : (asc.get(m/2 - 1) + asc.get(m/2)) / 2.0;
    }
}
package com.exempal.shiftcounter.features.shift.projection;

import com.exempal.shiftcounter.features.settings.domain.SettingsPort;
import com.exempal.shiftcounter.features.shift.domain.ActualDataPort;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class ShiftProjectionUseCase {

    private final SettingsPort settings;
    private final ActualDataPort actual;

    public ShiftProjectionUseCase(SettingsPort settings, ActualDataPort actual) {
        this.settings = settings;
        this.actual = actual;
    }

    public ShiftView buildView(LocalDate date) {
        var loaded = settings.load();

        return actual.findByDate(date)
                .map(shift -> {
                    // Часы — из домена (фактической смены), порядок не трогаем
                    List<String> hours = shift.getHourlyLabels();
                    int expectedSize = hours.size();
                    List<Integer> plan = ensureSize(shift.getHourlyPlanValues(), expectedSize);
                    List<Integer> actualValues = ensureSize(shift.getHourlyActualValues(), expectedSize);
                    return new ShiftView(date, actualValues, plan, hours);
                })
                .orElseGet(() -> {
                    // Если смены ещё нет — берём дефолтные часы из настроек
                    List<String> hours = loaded.getHours()
                            .stream()
                            .map(h -> h.getStart().toString()) // ← через геттер
                            .toList();
                    int expectedSize = hours.size();
                    List<Integer> plan = ensureSize(loaded.getHourlyPlans(), expectedSize);
                    List<Integer> actualValues = Collections.nCopies(expectedSize, 0);
                    return new ShiftView(date, actualValues, plan, hours);
                });
    }

    private List<Integer> ensureSize(List<Integer> source, int size) {
        List<Integer> result = new ArrayList<>(source != null ? source : List.of());
        while (result.size() < size) result.add(0);
        return result.subList(0, size);
    }
}
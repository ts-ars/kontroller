package com.exempal.shiftcounter.features.shift.application;

import com.exempal.shiftcounter.features.shift.domain.Shift;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ShiftExtenderService {

    private final ShiftIntervalService intervals;

    public Shift extendIfNeeded(LocalDateTime signalTime, Shift current) {
        List<String> currentLabels = current.getHourlyLabels();
        // Продлеваем до signalTime, порядок НЕ меняем (никакой сортировки)
        List<String> extendedLabels = intervals.extendUntil(current.getDate(), currentLabels,
                current.getHourlyPlanValues().size(), signalTime);

        List<Integer> actuals = fillExtended(current.getHourlyActualValues(), extendedLabels.size());

        return current.withUpdatedStructure(extendedLabels, current.getHourlyPlanValues(), actuals);
    }

    private List<Integer> fillExtended(List<Integer> source, int size) {
        List<Integer> result = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            result.add(i < source.size() ? source.get(i) : 0);
        }
        return result;
    }
}

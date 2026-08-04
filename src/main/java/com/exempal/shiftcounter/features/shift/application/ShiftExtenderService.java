package com.exempal.shiftcounter.features.shift.application;

import com.exempal.shiftcounter.features.settings.domain.Settings;
import com.exempal.shiftcounter.features.settings.infrastructure.ShiftSettingsProvider;
import com.exempal.shiftcounter.features.shift.domain.Shift;
import com.exempal.shiftcounter.features.shift.domain.ShiftMetrics;
import com.exempal.shiftcounter.features.shift.domain.ShiftMetricsCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ShiftExtenderService {

    private final ShiftTimeHelper timeHelper;
    private final ShiftSettingsProvider settingsProvider;
    private final ShiftMetricsCalculator metricsCalculator;

    public Shift extendIfNeeded(LocalDateTime signalTime, Shift current) {
        List<String> currentLabels = current.getHourlyLabels();
        // Продлеваем до signalTime, порядок НЕ меняем (никакой сортировки)
        List<String> extendedLabels = timeHelper.extendUntil(current.getDate(), currentLabels, signalTime);

        Settings settings = settingsProvider.get();
        ShiftMetrics metrics = metricsCalculator.calculateFor(settings, extendedLabels);

        List<Integer> actuals = fillExtended(current.getHourlyActualValues(), extendedLabels.size());

        return current.withUpdatedStructure(metrics.labels(), metrics.plans(), actuals);
    }

    private List<Integer> fillExtended(List<Integer> source, int size) {
        List<Integer> result = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            result.add(i < source.size() ? source.get(i) : 0);
        }
        return result;
    }
}
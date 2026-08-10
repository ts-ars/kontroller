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
        return buildView(date, com.exempal.shiftcounter.features.sensor.domain.SensorCatalog.SENSOR_1);
    }

    public ShiftView buildView(LocalDate date, String sensorId) {
        var loaded = settings.load();

        return actual.findByDateAndSensorId(date, sensorId)
                .map(shift -> {
                    // Часы — из домена (фактической смены), порядок не трогаем
                    List<String> hours = shift.getHourlyLabels();
                    int expectedSize = hours.size();
                    int suppliedPlans = shift.getHourlyPlanValues().size();
                    List<Integer> plan = ensureSize(shift.getHourlyPlanValues(), expectedSize);
                    List<Integer> actualValues = ensureSize(shift.getHourlyActualValues(), expectedSize);
                    List<Boolean> planSupplied = java.util.stream.IntStream.range(0, expectedSize)
                            .mapToObj(index -> index < suppliedPlans).toList();
                    return new ShiftView(date, sensorId, actualValues, plan, hours, planSupplied);
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
                    return new ShiftView(date, sensorId, actualValues, plan, hours,
                            Collections.nCopies(expectedSize, true));
                });
    }

    private List<Integer> ensureSize(List<Integer> source, int size) {
        List<Integer> result = new ArrayList<>(source != null ? source : List.of());
        while (result.size() < size) result.add(0);
        return result.subList(0, size);
    }
}

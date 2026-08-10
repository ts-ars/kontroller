package com.exempal.shiftcounter.features.shift.domain;

import com.exempal.shiftcounter.features.settings.infrastructure.ShiftHourLabelMapper;
import com.exempal.shiftcounter.features.settings.domain.Settings;
import com.exempal.shiftcounter.features.settings.infrastructure.ShiftSettingsProvider;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class ShiftFactory {

    private final ShiftSettingsProvider settingsProvider;

    public ShiftFactory(ShiftSettingsProvider settingsProvider) {
        this.settingsProvider = settingsProvider;
    }

    public Shift createNewShift(LocalDate date, String sensorId) {
        Settings settings = settingsProvider.get();

        List<String> hourLabels = ShiftHourLabelMapper.toLabelsStartOnly(settings.getHours());

        List<Integer> hourlyPlan = new ArrayList<>(settings.getHourlyPlans());
        List<Integer> hourlyActual = new ArrayList<>(Collections.nCopies(hourLabels.size(), 0));

        return new Shift(
                null,
                date,
                sensorId,
                hourlyPlan,
                0,
                hourlyActual,
                hourLabels
        );
    }

    public Shift createNewShift(LocalDate date) {
        return createNewShift(date, com.exempal.shiftcounter.features.sensor.domain.SensorCatalog.SENSOR_1);
    }

    public Shift recalculateFrom(Shift existingShift) {
        Settings settings = settingsProvider.get();

        List<String> hourLabels = ShiftHourLabelMapper.toLabelsStartOnly(settings.getHours());

        List<Integer> hourlyPlan = new ArrayList<>(settings.getHourlyPlans());
        List<Integer> actual = padOrTrim(new ArrayList<>(existingShift.getHourlyActualValues()), hourLabels.size());
        hourlyPlan = padOrTrim(hourlyPlan, hourLabels.size());

        int actualSum = actual.stream().mapToInt(Integer::intValue).sum();

        return new Shift(
                existingShift.getId(),
                existingShift.getDate(),
                existingShift.getSensorId(),
                hourlyPlan,
                actualSum,
                actual,
                hourLabels
        );
    }

    private List<Integer> padOrTrim(List<Integer> list, int size) {
        while (list.size() < size) list.add(0);
        return list.subList(0, size);
    }
}

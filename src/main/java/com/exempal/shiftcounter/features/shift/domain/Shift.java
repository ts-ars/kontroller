package com.exempal.shiftcounter.features.shift.domain;

import com.exempal.shiftcounter.features.shift.infrastructure.ShiftEntity;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
public class Shift {

    private final Long id;
    private final LocalDate date;
    private final List<Integer> hourlyPlanValues;
    private final Integer actual;
    private final List<Integer> hourlyActualValues;
    private final List<String> hourlyLabels;

    public Shift(
            Long id,
            LocalDate date,
            List<Integer> hourlyPlanValues,
            Integer actual,
            List<Integer> hourlyActualValues,
            List<String> hourlyLabels
    ) {
        this.id = id;
        this.date = date;
        this.hourlyPlanValues = new ArrayList<>(hourlyPlanValues);
        this.actual = hourlyActualValues.stream().mapToInt(Integer::intValue).sum();
        this.hourlyActualValues = new ArrayList<>(hourlyActualValues);
        this.hourlyLabels = new ArrayList<>(hourlyLabels);
    }

    public Shift(
            LocalDate date,
            List<Integer> hourlyPlanValues,
            Integer actual,
            List<Integer> hourlyActualValues,
            List<String> hourlyLabels
    ) {
        this(null, date, hourlyPlanValues, actual, hourlyActualValues, hourlyLabels);
    }

    @Getter
    @Setter
    private ShiftEntity entity;


    /**
     * Увеличивает значение actual по индексу на 1 и возвращает новый Shift
     */
    public Shift withIncrementedHourlyActualValue(int hourIndex) {
        List<Integer> updatedActual = new ArrayList<>(this.hourlyActualValues);
        updatedActual.set(hourIndex, updatedActual.get(hourIndex) + 1);
        return new Shift(
                this.id,
                this.date,
                this.hourlyPlanValues,
                this.actual != null ? this.actual + 1 : 1,
                updatedActual,
                this.hourlyLabels
        );
    }

    /**
     * Возвращает новый Shift с обновлённой структурой (часы, план, actual)
     * durations игнорируются (по требованию)
     */
    public Shift withUpdatedStructure(
            List<String> newHourlyLabels,
            List<Integer> newPlanValues,
            List<Integer> newActualValues
    ) {
        int recalculatedActual = newActualValues.stream().mapToInt(Integer::intValue).sum(); // ✅ пересчёт
        return new Shift(
                this.id,
                this.date,
                newPlanValues,
                recalculatedActual, // ✅ правильное значение actual
                newActualValues,
                newHourlyLabels
        );
    }
}

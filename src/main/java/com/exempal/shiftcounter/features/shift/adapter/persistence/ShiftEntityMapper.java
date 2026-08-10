package com.exempal.shiftcounter.features.shift.adapter.persistence;

import com.exempal.shiftcounter.features.shift.domain.Shift;

import java.util.ArrayList;

final class ShiftEntityMapper {
    private ShiftEntityMapper() {
    }

    static ShiftEntity fromDomain(Shift shift) {
        ShiftEntity entity = new ShiftEntity();
        replace(entity, shift);
        entity.setDate(shift.getDate());
        entity.setSensorId(shift.getSensorId());
        return entity;
    }

    static void replace(ShiftEntity target, Shift source) {
        target.setActual(source.getActual());
        target.setHourlyActualValues(new ArrayList<>(source.getHourlyActualValues()));
        target.setHourlyPlanValues(new ArrayList<>(source.getHourlyPlanValues()));
        target.setHourlyLabels(new ArrayList<>(source.getHourlyLabels()));
    }

    static Shift toDomain(ShiftEntity entity) {
        return new Shift(entity.getId(), entity.getDate(), entity.getSensorId(),
                entity.getHourlyPlanValues(), entity.getActual(), entity.getHourlyActualValues(),
                entity.getHourlyLabels());
    }
}

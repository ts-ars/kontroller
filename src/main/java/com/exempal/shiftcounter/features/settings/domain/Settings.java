package com.exempal.shiftcounter.features.settings.domain;

import lombok.Getter;

import java.util.List;

@Getter
public class Settings {

    private final List<ShiftHour> hours;
    private final List<Integer> hourlyPlans;

    public Settings(List<ShiftHour> hours, List<Integer> hourlyPlans) {
        this.hours = hours;
        this.hourlyPlans = hourlyPlans;
    }


    @Override
    public String toString() {
        return STR."Settings{hours=\{hours}, hourlyPlans=\{hourlyPlans}}";
    }
}

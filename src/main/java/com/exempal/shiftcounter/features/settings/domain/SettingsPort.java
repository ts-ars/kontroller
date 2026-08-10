package com.exempal.shiftcounter.features.settings.domain;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public interface SettingsPort {

    List<String> getHourlyPlans();    // например: ["337", "450", ..., "337"]
    List<String> getHours();          // например: ["08:00", "09:00", ..., "15:30"]

    void updateHourlyPlans(List<String> plans);
    void updateHours(List<String> hours);
    void update(String key, String value);

    default Settings load() {
        List<String> hourStrings = getHours();
        List<Integer> plans = getHourlyPlans().stream()
                .map(Integer::parseInt)
                .toList();

        if (hourStrings.size() != plans.size()) {
            throw new IllegalStateException("Число часов и планов должно совпадать");
        }

        List<ShiftHour> shiftHours = new ArrayList<>();
        for (int i = 0; i < hourStrings.size(); i++) {
            LocalTime start = LocalTime.parse(hourStrings.get(i));
            LocalTime end = (i + 1 < hourStrings.size())
                    ? LocalTime.parse(hourStrings.get(i + 1))
                    : (start.getMinute() == 30 ? start.plusMinutes(30) : start.plusHours(1));
            shiftHours.add(new ShiftHour(start, end));
        }

        return new Settings(shiftHours, plans);
    }

    default List<Integer> getPlan() {
        return getHourlyPlans().stream()
                .map(Integer::parseInt)
                .toList();
    }
}

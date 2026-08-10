package com.exempal.shiftcounter.features.settings.domain;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class SettingsLoader {

    private final SettingsPort port;

    public SettingsLoader(SettingsPort port) {
        this.port = port;
    }

    public Settings load() {
        List<String> hourStrings = port.getHours();
        List<String> planStrings = port.getHourlyPlans();

        List<ShiftHour> hours = new ArrayList<>();
        for (int i = 0; i < hourStrings.size(); i++) {
            LocalTime start = LocalTime.parse(hourStrings.get(i));
            LocalTime end = (i + 1 < hourStrings.size())
                    ? LocalTime.parse(hourStrings.get(i + 1))
                    : (start.getMinute() == 30 ? start.plusMinutes(30) : start.plusHours(1));
            hours.add(new ShiftHour(start, end));
        }

        List<Integer> plans = planStrings.stream()
                .map(Integer::parseInt)
                .toList();

        return new Settings(hours, plans);
    }
}


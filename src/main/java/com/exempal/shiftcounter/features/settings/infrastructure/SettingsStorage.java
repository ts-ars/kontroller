package com.exempal.shiftcounter.features.settings.infrastructure;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class SettingsStorage {

    private int ppm = 180;

    private final List<String> hours = new ArrayList<>(Arrays.asList(
            "08:00", "09:00", "10:00", "11:00", "12:30", "13:30", "14:30","15:30"
    ));

    private final List<Integer> hourlyPlans = new ArrayList<>(Arrays.asList(
            200, 200, 200, 200, 200, 200, 200, 200
    ));

    public int getPpm() {
        return ppm;
    }

    public void setPpm(int ppm) {
        this.ppm = ppm;
    }

    public List<String> getHours() {
        return List.copyOf(hours);
    }

    public void setHours(List<String> newHours) {
        hours.clear();
        hours.addAll(newHours);
    }

    public List<Integer> getHourlyPlans() {
        return List.copyOf(hourlyPlans);
    }

    public void setHourlyPlans(List<Integer> newPlans) {
        hourlyPlans.clear();
        hourlyPlans.addAll(newPlans);
    }
}

package com.exempal.shiftcounter.features.settings.domain;

import java.util.List;

public interface SettingsPort {
    int getPpm();
    List<Integer> getHourlyPlans();
    List<String> getHours();
}

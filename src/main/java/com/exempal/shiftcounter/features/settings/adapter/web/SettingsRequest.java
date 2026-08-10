package com.exempal.shiftcounter.features.settings.adapter.web;

import java.util.List;

public record SettingsRequest(
        String name,
        Boolean enabled,
        List<String> hours,
        List<String> hourlyPlans
) {
    public SettingsRequest(List<String> hours, List<String> hourlyPlans) {
        this(null, null, hours, hourlyPlans);
    }
}

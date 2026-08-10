package com.exempal.shiftcounter.features.settings.adapter.web;

import java.util.List;

public record SettingsResponse(
        String groupId,
        String name,
        boolean enabled,
        List<String> hours,
        List<String> hourlyPlans
) {}

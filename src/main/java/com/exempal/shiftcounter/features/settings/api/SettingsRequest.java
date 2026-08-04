package com.exempal.shiftcounter.features.settings.api;

import java.util.List;

public record SettingsRequest(
        List<String> hours,
        List<String> hourlyPlans
) {}

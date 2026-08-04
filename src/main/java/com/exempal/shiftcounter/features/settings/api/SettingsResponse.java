package com.exempal.shiftcounter.features.settings.api;

import java.util.List;

public record SettingsResponse(
        List<String> hours,
        List<String> hourlyPlans
) {}

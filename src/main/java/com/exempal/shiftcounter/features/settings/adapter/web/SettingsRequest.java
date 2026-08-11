package com.exempal.shiftcounter.features.settings.adapter.web;

import java.util.List;

public record SettingsRequest(List<String> hours, List<Integer> sensors1To4Plans,
                              List<Integer> sensor6Plans) {
}

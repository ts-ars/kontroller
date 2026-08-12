package com.exempal.shiftcounter.features.settings.application;

import java.util.List;

public record UpdateSettingsCommand(String groupId, List<String> hours,
                                    List<Integer> sharedPlans, List<Integer> sensor6Plans) {
}

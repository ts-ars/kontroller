package com.exempal.shiftcounter.features.settings.application;

import java.util.List;

public record UpdateSettingsGroupCommand(String groupId, String name, boolean enabled,
                                         List<String> hours, List<Integer> plans) {}

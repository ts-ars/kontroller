package com.exempal.shiftcounter.features.settings.adapter.web;

import com.exempal.shiftcounter.features.settings.domain.SettingsRow;
import com.exempal.shiftcounter.features.settings.domain.SettingsSnapshot;

import java.util.List;

public record SettingsResponse(String groupId, List<String> hours,
                               List<Integer> sensors1To4Plans, List<Integer> sensor5Plans,
                               List<Integer> sensor6Plans, int sensors1To4Total,
                               int sensor5Total, int sensor6Total) {
    public static SettingsResponse from(String groupId, SettingsSnapshot snapshot) {
        return new SettingsResponse(groupId,
                snapshot.rows().stream().map(row -> row.hour().toString()).toList(),
                snapshot.rows().stream().map(SettingsRow::sharedPlan).toList(),
                snapshot.rows().stream().map(SettingsRow::sensor5Plan).toList(),
                snapshot.rows().stream().map(SettingsRow::sensor6Plan).toList(),
                snapshot.sharedTotal(), snapshot.sensor5Total(), snapshot.sensor6Total());
    }
}

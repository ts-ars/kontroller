package com.exempal.shiftcounter.features.shift.application;

import java.util.List;

public record ShiftSettings(List<String> labels, List<Integer> plans) {
    public ShiftSettings {
        labels = List.copyOf(labels);
        plans = List.copyOf(plans);
    }
}

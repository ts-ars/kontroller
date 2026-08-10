package com.exempal.shiftcounter.features.comment.adapter.dto;

import com.exempal.shiftcounter.features.comment.application.ReconcileResult;

import java.util.List;

public record ReconcileResponse(String sensorId, int intervalIndex, int activeCount, int changedRows,
                                boolean persisted, List<String> diagnostics) {
    public static ReconcileResponse from(String sensorId, ReconcileResult result) {
        return new ReconcileResponse(sensorId, result.intervalIndex(), result.activeStoppages().size(),
                result.changedRows(), result.persisted(), result.diagnostics().stream()
                .map(value -> value.code() + ": " + value.detail()).toList());
    }
}

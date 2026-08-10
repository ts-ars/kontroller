package com.exempal.shiftcounter.features.comment.application;

import com.exempal.shiftcounter.features.comment.domain.Stoppage;

import java.util.List;

public record ReconcileResult(int intervalIndex, List<Stoppage> activeStoppages,
                              List<ReconcileDiagnostic> diagnostics, int changedRows,
                              boolean persisted) {
    public ReconcileResult {
        activeStoppages = List.copyOf(activeStoppages);
        diagnostics = List.copyOf(diagnostics);
    }

    public boolean hasFatalDiagnostic() {
        return diagnostics.stream().anyMatch(ReconcileDiagnostic::fatal);
    }
}

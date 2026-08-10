package com.exempal.shiftcounter.features.comment.calculator;

import com.exempal.shiftcounter.features.comment.application.ReconcileDiagnostic;

import java.util.List;

public record StoppageCalculation(List<StoppageCandidate> candidates,
                                  List<ReconcileDiagnostic> diagnostics) {
    public StoppageCalculation {
        candidates = List.copyOf(candidates);
        diagnostics = List.copyOf(diagnostics);
    }
}

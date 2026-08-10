package com.exempal.shiftcounter.features.comment.application;

public record ReconcileDiagnostic(ReconcileDiagnosticCode code, String detail, boolean fatal) {
    public static ReconcileDiagnostic warning(ReconcileDiagnosticCode code, String detail) {
        return new ReconcileDiagnostic(code, detail, false);
    }

    public static ReconcileDiagnostic fatal(ReconcileDiagnosticCode code, String detail) {
        return new ReconcileDiagnostic(code, detail, true);
    }
}

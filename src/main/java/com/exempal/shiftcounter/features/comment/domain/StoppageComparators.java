package com.exempal.shiftcounter.features.comment.domain;

import java.util.Comparator;

public final class StoppageComparators {
    private StoppageComparators() {}

    public static Comparator<Stoppage> chronological() {
        return Comparator.comparing(Stoppage::startedAt).thenComparing(Stoppage::detectionKey);
    }
}

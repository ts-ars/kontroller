package com.exempal.shiftcounter.features.comment.domain;

import java.util.Comparator;

public final class StoppageComparators {
    private StoppageComparators() {}

    /** Хронология внутри смены: час → смещение в минутах → тип (FIXED, TEMPO, затем пользовательские). */
    public static Comparator<StoppageEntry> chronological() {
        return Comparator
                .comparingInt(StoppageEntry::getHourIndex)
                .thenComparingInt(StoppageEntry::getMinuteOffset)
                .thenComparingInt(e -> typeOrder(e.getType()));
    }

    private static int typeOrder(StoppageType t) {
        if (t == null) return 2;
        return switch (t) {
            case FIXED -> 0;
            case TEMPO -> 1;
            default -> 2; // user-editable: organization/breakdown/material/quality
        };
    }
}
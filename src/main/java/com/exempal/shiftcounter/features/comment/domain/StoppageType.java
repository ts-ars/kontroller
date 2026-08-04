package com.exempal.shiftcounter.features.comment.domain;

public enum StoppageType {
    FIXED,
    TEMPO,
    ORGANIZATION,
    MATERIAL,
    BREAKDOWN,
    QUALITY;

    public static boolean isValid(String raw) {
        try {
            valueOf(raw.toUpperCase());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // 🟢 Метод для определения редактируемости
    public boolean isUserEditable() {
        return switch (this) {
            case ORGANIZATION, MATERIAL, BREAKDOWN, QUALITY -> true;
            case FIXED, TEMPO -> false;
        };
    }
}
package com.exempal.shiftcounter.features.settings.infrastructure;

import com.exempal.shiftcounter.features.settings.domain.ShiftHour;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class ShiftHourLabelMapper {

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * Метки только начала (например, "08:00", "09:00") — для UI.
     */
    public static List<String> toLabelsStartOnly(List<ShiftHour> hours) {
        return hours.stream()
                .map(hour -> hour.getStart().format(FORMAT))
                .collect(Collectors.toList());
    }

    /**
     * Полные метки интервалов (например, "08:00 – 09:00") — для внутренней логики.
     */
    public static List<String> toLabelsFull(List<ShiftHour> hours) {
        return hours.stream()
                .map(ShiftHour::toString) // используется уже реализованный метод toString()
                .collect(Collectors.toList());
    }
}

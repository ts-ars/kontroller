package com.exempal.shiftcounter.features.shift.domain;

import com.exempal.shiftcounter.features.settings.domain.Settings;
import com.exempal.shiftcounter.features.settings.domain.ShiftHour;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalTime;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class ShiftMetricsCalculator {

    public ShiftMetrics calculateFor(Settings settings) {
        return calculateFor(settings, extractLabels(settings));
    }

    public ShiftMetrics calculateFor(Settings settings, List<String> labels) {
        Map<String, ShiftHour> hourMap = new LinkedHashMap<>();
        List<ShiftHour> hours = settings.getHours();
        List<Integer> plans = settings.getHourlyPlans();

        for (ShiftHour h : hours) {
            hourMap.put(h.getStart().toString(), h); // ключ: "HH:mm"
        }

        List<Integer> usedPlans = new ArrayList<>();
        List<Integer> durationsMinutes = new ArrayList<>();
        List<Double> cansPerMinute = new ArrayList<>();

        for (int i = 0; i < labels.size(); i++) {
            String rawLabel = labels.get(i);

            // поддержка "HH:mm" и "HH:mm – HH:mm"
            LocalTime startFromLabel = parseStart(rawLabel);
            ShiftHour hour = hourMap.get(startFromLabel.toString());

            LocalTime start;
            LocalTime end;

            if (hour != null) {
                start = hour.getStart();
                end   = hour.getEnd();
            } else {
                // расширенный/внешний слот: правая граница из метки, иначе +1h
                start = startFromLabel;
                LocalTime endFromLabel = parseEndOrNull(rawLabel);
                end   = (endFromLabel != null) ? endFromLabel : start.plusHours(1);
            }

            var startDateTime = LocalDate.of(2000, 1, 1).atTime(start);
            var endDateTime = LocalDate.of(2000, 1, end.isAfter(start) ? 1 : 2).atTime(end);
            int minutes = (int) Duration.between(startDateTime, endDateTime).toMinutes();
            int plan    = (i < plans.size()) ? plans.get(i) : 0;

            usedPlans.add(plan);
            durationsMinutes.add(minutes);
            cansPerMinute.add(round((double) plan / minutes));
        }

        return new ShiftMetrics(labels, usedPlans, durationsMinutes, cansPerMinute);
    }


    private List<String> extractLabels(Settings settings) {
        return settings.getHours().stream()
                .map(h -> h.getStart().toString())
                .toList();
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private static LocalTime parseStart(String label) {
        int dash = label.indexOf('–');              // длинное тире U+2013
        String left = (dash >= 0) ? label.substring(0, dash).trim() : label.trim();
        return LocalTime.parse(left);
    }

    private static LocalTime parseEndOrNull(String label) {
        int dash = label.indexOf('–');
        if (dash < 0) return null;
        String right = label.substring(dash + 1).trim();
        return right.isEmpty() ? null : LocalTime.parse(right);
    }
}

package com.exempal.shiftcounter.features.shift.application;

import com.exempal.shiftcounter.features.shift.domain.ProductionDay;
import com.exempal.shiftcounter.features.shift.domain.ShiftInterval;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class ShiftIntervalService {
    private static final DateTimeFormatter LABEL = DateTimeFormatter.ofPattern("HH:mm")
            .withResolverStyle(ResolverStyle.STRICT);

    public List<ShiftInterval> resolve(LocalDate productionDate, List<String> labels, int planCount) {
        if (labels == null || labels.isEmpty()) return List.of();
        if (planCount < 0 || planCount > labels.size()) {
            throw new IllegalArgumentException("plan count must be between zero and interval count");
        }
        ProductionDay day = ProductionDay.of(productionDate);
        List<LocalDateTime> starts = new ArrayList<>(labels.size());
        LocalDateTime previous = null;
        for (String label : labels) {
            LocalTime time = parse(label);
            LocalDateTime start = LocalDateTime.of(
                    time.isBefore(ProductionDay.BOUNDARY) ? productionDate.plusDays(1) : productionDate, time);
            if (previous != null && !start.isAfter(previous)) {
                throw new IllegalArgumentException("Time values must be strictly ordered in the production day");
            }
            if (!day.contains(start)) throw new IllegalArgumentException("Time is outside the production day");
            starts.add(start);
            previous = start;
        }

        List<ShiftInterval> intervals = new ArrayList<>(starts.size());
        for (int index = 0; index < starts.size(); index++) {
            LocalDateTime start = starts.get(index);
            LocalDateTime end = index + 1 < starts.size() ? starts.get(index + 1) : finalEnd(start);
            if (end.isAfter(day.end())) throw new IllegalArgumentException("interval crosses production-day end");
            intervals.add(new ShiftInterval(index, start, end, index < planCount));
        }
        return List.copyOf(intervals);
    }

    public Optional<ShiftInterval> find(LocalDate productionDate, List<String> labels,
                                        int planCount, LocalDateTime timestamp) {
        return resolve(productionDate, labels, planCount).stream()
                .filter(interval -> interval.contains(timestamp)).findFirst();
    }

    public List<String> extendUntil(LocalDate productionDate, List<String> labels,
                                    int planCount, LocalDateTime timestamp) {
        if (labels == null || labels.isEmpty()) return labels;
        ProductionDay day = ProductionDay.of(productionDate);
        if (!day.contains(timestamp)) return List.copyOf(labels);
        List<String> result = new ArrayList<>(labels);
        while (find(productionDate, result, planCount, timestamp).isEmpty()) {
            List<ShiftInterval> intervals = resolve(productionDate, result, planCount);
            ShiftInterval last = intervals.getLast();
            if (!last.end().isBefore(day.end())) break;
            if (timestamp.isBefore(last.start())) break;
            result.add(last.end().toLocalTime().format(LABEL));
        }
        return List.copyOf(result);
    }

    private LocalDateTime finalEnd(LocalDateTime start) {
        int minute = start.getMinute();
        if (start.getSecond() != 0 || start.getNano() != 0 || (minute != 0 && minute != 30)) {
            throw new IllegalArgumentException("final Time must end in :00 or :30");
        }
        return minute == 0 ? start.plusHours(1) : start.plusMinutes(30);
    }

    private LocalTime parse(String raw) {
        if (raw == null || raw.isBlank()) throw new IllegalArgumentException("Time is required");
        String value = raw.trim();
        try {
            return LocalTime.parse(value, LABEL);
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException("Time must use HH:mm format", exception);
        }
    }
}

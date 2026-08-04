package com.exempal.shiftcounter.features.shift.application;

import com.exempal.shiftcounter.features.settings.domain.ShiftHour;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class ShiftTimeHelper {

    /**
     * Индекс интервала определяется как: [labels[i], labels[i+1]) для всех, кроме последнего.
     * Для последнего: [labels[last], labels[last] + 1h] (или бесконечность, если тебе нужно).
     */
    public int resolveHourIndex(LocalTime time, List<String> hourlyLabels) {
        if (hourlyLabels == null || hourlyLabels.isEmpty()) return -1;

        for (int i = 0; i < hourlyLabels.size(); i++) {
            LocalTime start = parseStart(hourlyLabels.get(i));
            LocalTime end = computeIntervalEnd(hourlyLabels, i);
            // Интервал полуоткрытый: [start, end)
            if (!time.isBefore(start) && time.isBefore(end)) {
                return i;
            }
        }
        // Если позже последнего end — считаем последним интервалом (если тебе так нужно)
        LocalTime lastStart = parseStart(hourlyLabels.get(hourlyLabels.size() - 1));
        LocalTime lastEnd = computeIntervalEnd(hourlyLabels, hourlyLabels.size() - 1);
        if (!time.isBefore(lastEnd)) {
            // вариант 1: вернуть последний индекс
            return hourlyLabels.size() - 1;
            // вариант 2: вернуть -1, если за границей смены — выбери нужное поведение
        }
        return -1;
    }

    /**
     * Расширяет часы смены, если сигнал позже конца последнего интервала.
     * Новый интервал начинается с последнего end и длится 1 час.
     */
    public List<String> extendShiftHours(List<String> currentHours, LocalTime signalTime) {
        if (currentHours == null || currentHours.isEmpty()) return currentHours;

        List<String> updated = new ArrayList<>(currentHours);
        LocalTime end = computeIntervalEnd(updated, updated.size() - 1);

        while (signalTime.isAfter(end)) {
            LocalTime newStart = end;
            LocalTime newEnd = newStart.plusHours(1);
            updated.add(newStart.toString()); // сохраняем как "HH:mm"
            end = newEnd;
        }
        return updated;
    }

    // --- Новая базовая логика начала/конца ---

    private LocalTime parseStart(String label) {
        // Поддержка двух форматов: "HH:mm" ИЛИ "HH:mm – HH:mm"
        if (label.contains("–")) {
            String[] parts = label.split("–");
            return LocalTime.parse(parts[0].trim());
        }
        return LocalTime.parse(label.trim());
    }

    private LocalTime computeIntervalEnd(List<String> labels, int index) {
        if (index < labels.size() - 1) {
            // конец = следующая метка
            return parseStart(labels.get(index + 1));
        }
        // последний интервал: +1 час (или сделай бесконечным, если нужно)
        return parseStart(labels.get(index)).plusHours(1);
    }

    /**
     * Начало интервала по метке и дате.
     */
    public LocalDateTime resolveStartTime(String label, LocalDate date) {
        return LocalDateTime.of(date, parseStart(label));
    }

    /**
     * Конец интервала по индексу и дате: для i < last — это следующая метка; для последнего — +1h.
     */
    public LocalDateTime resolveEndTime(List<String> labels, int index, LocalDate date) {
        return LocalDateTime.of(date, computeIntervalEnd(labels, index));
    }

    // Оставляю parse(ShiftHour) только если где-то используешь
    @Deprecated
    private ShiftHour parse(String label) {
        // Больше не используем это для границ, оставлено для обратной совместимости
        String[] parts = label.split("–");
        LocalTime start = LocalTime.parse(parts[0].trim());
        LocalTime end = parts.length > 1 ? LocalTime.parse(parts[1].trim()) : start.plusHours(1);
        return new ShiftHour(start, end);
    }

    // Попадает ли ts в любой интервал смены этой даты?
    public boolean contains(LocalDate shiftDate, List<String> labels, LocalDateTime ts) {
        if (labels == null || labels.isEmpty()) return false;
        for (int i = 0; i < labels.size(); i++) {
            LocalDateTime start = resolveStartTime(labels.get(i), shiftDate);
            LocalDateTime end   = resolveIntervalEnd(labels, i, shiftDate);
            if (!ts.isBefore(start) && ts.isBefore(end)) return true; // [start, end)
        }
        return false;
    }

    // Найти индекс интервала по ts (с учётом даты)
    public int resolveHourIndex(LocalDate shiftDate, List<String> labels, LocalDateTime ts) {
        if (labels == null || labels.isEmpty()) return -1;
        for (int i = 0; i < labels.size(); i++) {
            LocalDateTime start = resolveStartTime(labels.get(i), shiftDate);
            LocalDateTime end   = resolveIntervalEnd(labels, i, shiftDate);
            if (!ts.isBefore(start) && ts.isBefore(end)) return i;
        }
        return -1;
    }

    // Продлить labels вперёд, пока последний end < ts (учитывает переход через 00:00)
    public List<String> extendUntil(LocalDate shiftDate, List<String> labels, LocalDateTime ts) {
        if (labels == null || labels.isEmpty()) return labels;
        List<String> out = new ArrayList<>(labels);
        LocalDateTime end = resolveIntervalEnd(out, out.size() - 1, shiftDate);
        while (!end.isAfter(ts)) { // пока end <= ts
            LocalTime newStart = end.toLocalTime();
            out.add(newStart.toString()); // "HH:mm"
            end = end.plusHours(1);
        }
        return out;
    }

    // Вспомогательное: конец интервала i на дату shiftDate
    private LocalDateTime resolveIntervalEnd(List<String> labels, int i, LocalDate shiftDate) {
        if (i < labels.size() - 1) return resolveStartTime(labels.get(i + 1), shiftDate);
        return resolveStartTime(labels.get(i), shiftDate).plusHours(1);
    }
}
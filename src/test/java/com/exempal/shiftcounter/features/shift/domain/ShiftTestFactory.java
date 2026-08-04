package com.exempal.shiftcounter.features.shift.domain;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Утилита для создания тестовых экземпляров Shift.
 * Используется в unit, integration и WebMvc тестах.
 */
public class ShiftTestFactory {

    /**
     * Создаёт Shift с указанным id (для моков и контролируемых сценариев).
     */
    public static Shift shiftWithId(Long id, LocalDate date, int actual) {
        List<Integer> hourly = distributeActual(actual, 3);
        List<String> labels = List.of("08:00", "09:00", "10:00");
        List<Integer> plan = List.of(100, 100, 100);
        return new Shift(id, date, plan, actual, hourly, labels);
    }

    /**
     * Создаёт Shift без id (подходит для сохранения через JPA).
     */
    public static Shift shift(LocalDate date, int actual) {
        List<Integer> hourly = distributeActual(actual, 3);
        List<String> labels = List.of("08:00", "09:00", "10:00");
        List<Integer> plan = List.of(100, 100, 100);
        return new Shift(date, plan, actual, hourly, labels);
    }

    /**
     * Универсальный метод с полным контролем всех параметров.
     */
    public static Shift shiftCustom(Long id, LocalDate date, List<Integer> plan, List<Integer> actuals, List<String> labels) {
        int totalActual = actuals.stream().mapToInt(Integer::intValue).sum();
        return new Shift(id, date, plan, totalActual, actuals, labels);
    }

    /**
     * Универсальный метод без id (например, для создания новой смены).
     */
    public static Shift shiftCustom(LocalDate date, List<Integer> plan, List<Integer> actuals, List<String> labels) {
        return shiftCustom(null, date, plan, actuals, labels);
    }

    /**
     * Генератор смены на N часов с равномерным распределением actual.
     */
    public static Shift shiftWithHours(LocalDate date, int actual, int hoursCount) {
        List<String> labels = IntStream.range(0, hoursCount)
                .mapToObj(i -> String.format("%02d:00", 8 + i))
                .toList();

        List<Integer> hourly = distributeActual(actual, hoursCount);
        List<Integer> plan = Collections.nCopies(hoursCount, 100);

        return new Shift(date, plan, actual, hourly, labels);
    }

    /**
     * Распределяет общее значение actual по N интервалам.
     */
    private static List<Integer> distributeActual(int total, int size) {
        int base = total / size;
        int remainder = total % size;
        List<Integer> result = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            result.add(i < remainder ? base + 1 : base);
        }
        return result;
    }
}
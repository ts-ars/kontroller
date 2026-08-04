package com.exempal.shiftcounter.features.comment;

import com.exempal.shiftcounter.features.comment.domain.StoppageEntry;
import com.exempal.shiftcounter.features.comment.domain.StoppageType;

/**
 * Утилита для создания тестовых экземпляров StoppageEntry.
 * Используется в unit и WebMvc тестах (например, в ReportPageTest).
 */
public class StoppageTestFactory {

    /**
     * Создаёт дефолтную остановку типа breakdown на первом часу смены.
     */
    public static StoppageEntry defaultBreakdownToday() {
        return entry(0, 10.0, 400, StoppageType.BREAKDOWN, "belt");
    }

    /**
     * Универсальный фабричный метод создания StoppageEntry.
     *
     * @param hourIndex индекс часа в смене (0, 1, 2, ...)
     * @param minutes   длительность остановки в минутах
     * @param cans      количество недополученных банок
     * @param type      тип проблемы (StoppageType enum)
     * @param comment   комментарий к остановке
     * @return готовый StoppageEntry
     */
    public static StoppageEntry entry(int hourIndex, double minutes, int cans, StoppageType type, String comment) {
        StoppageEntry entry = new StoppageEntry();
        entry.setHourIndex(hourIndex);
        entry.setMinutes(minutes);
        entry.setCans(cans);
        entry.setType(type); // теперь это StoppageType, всё верно
        entry.setComment(comment);
        return entry;
    }
}
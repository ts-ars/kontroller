package com.exempal.shiftcounter.features.comment.calculator;

import com.exempal.shiftcounter.features.comment.adapter.dto.CommentRowDto;
import com.exempal.shiftcounter.features.comment.domain.StoppageEntry;
import com.exempal.shiftcounter.features.comment.domain.StoppageType;
import com.exempal.shiftcounter.features.shift.domain.Shift;
import com.exempal.shiftcounter.features.shift.domain.ShiftMetrics;

import java.util.ArrayList;
import java.util.List;

/**
 * Маппит пользовательские строки (CommentRowDto) в доменные StoppageEntry.
 * Cans берём из DTO, а если он == 0, считаем из minutes * canPerMinute(hourIndex).
 */
public class StoppageUserOverrideMapper {

    public List<StoppageEntry> map(
            Shift shift,
            List<CommentRowDto> rows,
            ShiftMetrics metrics
    ) {
        List<StoppageEntry> result = new ArrayList<>();
        if (rows == null || rows.isEmpty()) return result;

        for (CommentRowDto row : rows) {
            int hourIndex = row.hourIndex();
            double minutes = row.minutes();

            // Если пользователь уже указал cans — уважим его; иначе рассчитаем
            int cans = row.cans() > 0
                    ? row.cans()
                    : (int) Math.round(minutes * metrics.canPerMinute().get(hourIndex));

            StoppageEntry entry = new StoppageEntry();
            entry.setShift(shift.getEntity());     // объект сущности из Shift
            entry.setHourIndex(hourIndex);
            entry.setMinutes(minutes);
            entry.setCans(cans);
            entry.setType(StoppageType.valueOf(row.type()));
            entry.setComment(row.comment());

            result.add(entry);
        }
        return result;
    }
}
package com.exempal.shiftcounter.features.comment.adapter.mapper;

import com.exempal.shiftcounter.features.comment.adapter.dto.CommentRowDto;
import com.exempal.shiftcounter.features.comment.domain.StoppageEntry;
import com.exempal.shiftcounter.features.comment.domain.StoppageType;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class CommentRowMapper {
    private static final DateTimeFormatter TF = DateTimeFormatter.ofPattern("HH:mm");

    public static CommentRowDto toDto(StoppageEntry entry) {
        boolean editable = entry.getType() == null || entry.getType().isUserEditable();

        // формат времени как "HH:mm"
        String formattedTime = "";
        if (entry.getTime() != null) {
            if (entry.getType() == StoppageType.FIXED) {
                LocalTime start = entry.getTime();
                long minutesRounded = Math.max(1, Math.round(entry.getMinutes())); // защита от 0
                LocalTime end = start.plusMinutes(minutesRounded);
                formattedTime = STR."\{start.format(TF)} – \{end.format(TF)}";
            } else {
                formattedTime = entry.getTime().format(TF);              // tempo/прочие — как было
            }
        }
        return new CommentRowDto(
                entry.getId(),
                entry.getHourIndex(),
                entry.getMinutes(),
                entry.getCans(),
                entry.getType() != null ? entry.getType().name().toLowerCase() : "",
                entry.getComment(),
                formattedTime,
                editable
        );
    }
}
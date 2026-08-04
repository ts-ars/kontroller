package com.exempal.shiftcounter.features.comment.adapter.dto;

public record CommentRowDto(
        Long id,
        int hourIndex,
        double minutes,
        int cans,
        String type,
        String comment,
        String time,
        boolean editable
) {}

package com.exempal.shiftcounter.features.comment.adapter.dto;

import java.util.List;

public record LossRowDto(
        long id,
        String time,
        long roundedMinutes,
        int lostCans,
        String detectionType,
        long allocatedMinutes,
        long remainingMinutes,
        List<LossExplanationResponse> explanations
) {}

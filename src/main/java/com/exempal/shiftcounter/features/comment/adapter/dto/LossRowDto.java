package com.exempal.shiftcounter.features.comment.adapter.dto;

import com.exempal.shiftcounter.features.comment.domain.ExplanationStatus;
import com.exempal.shiftcounter.features.comment.domain.StoppageState;

import java.util.List;
import java.util.UUID;

public record LossRowDto(
        long id,
        UUID detectionKey,
        String time,
        long roundedMinutes,
        int lostCans,
        String detectionType,
        StoppageState state,
        ExplanationStatus explanationStatus,
        long allocatedMinutes,
        long remainingMinutes,
        long version,
        List<LossExplanationResponse> explanations
) {}

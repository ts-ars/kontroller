package com.exempal.shiftcounter.features.comment.adapter.dto;

import com.exempal.shiftcounter.features.comment.domain.LossCategory;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record LossExplanationRequest(
        @NotNull LossCategory category,
        String comment,
        @Min(0) int allocatedMinutes
) {}

package com.exempal.shiftcounter.features.comment.adapter.dto;

import com.exempal.shiftcounter.features.comment.domain.ExplanationStatus;
import com.exempal.shiftcounter.features.comment.domain.StoppageState;

import java.util.UUID;

public record StoppageViewDto(Long id, UUID detectionKey, String time, int minutes, String type,
                              StoppageState state, ExplanationStatus explanationStatus, long version) {
}

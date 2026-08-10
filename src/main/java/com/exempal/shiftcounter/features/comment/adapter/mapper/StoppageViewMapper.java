// 📁 features.comment.adapter.mapper.StoppageViewMapper

package com.exempal.shiftcounter.features.comment.adapter.mapper;

import com.exempal.shiftcounter.features.comment.adapter.dto.StoppageViewDto;
import com.exempal.shiftcounter.features.comment.domain.Stoppage;

public class StoppageViewMapper {

    public static StoppageViewDto toDto(Stoppage entry) {
        return new StoppageViewDto(
                entry.id(), entry.detectionKey(), entry.sensorKey(), entry.startedAt().toString(), entry.roundedMinutes(),
                entry.detectionType().name().toLowerCase(), entry.state(), entry.explanationStatus(), entry.version()
        );
    }
}

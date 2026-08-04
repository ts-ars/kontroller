// 📁 features.comment.adapter.mapper.StoppageViewMapper

package com.exempal.shiftcounter.features.comment.adapter.mapper;

import com.exempal.shiftcounter.features.comment.adapter.dto.StoppageViewDto;
import com.exempal.shiftcounter.features.comment.domain.StoppageEntry;

public class StoppageViewMapper {

    public static StoppageViewDto toDto(StoppageEntry entry) {
        return new StoppageViewDto(
                entry.getId(),
                entry.getLabel(),
                entry.getMinutes(),
                entry.getType() != null ? entry.getType().toString().toLowerCase() : "",
                entry.getComment()
        );
    }
}

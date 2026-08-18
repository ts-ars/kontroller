package com.exempal.shiftcounter.features.comment.adapter.persistence;

import com.exempal.shiftcounter.features.comment.application.ExcludedStoppagePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class JpaExcludedStoppageAdapter implements ExcludedStoppagePort {
    private final StoppageJpaRepository repository;

    @Override
    public void exclude(long id, String actor, String reason, LocalDateTime at) {
        var row = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Stoppage not found"));
        row.setExcluded(true); row.setExcludedAt(at); row.setExcludedBy(actor); row.setExclusionReason(reason);
    }

    @Override
    public void restore(long id) {
        var row = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Stoppage not found"));
        row.setExcluded(false); row.setExcludedAt(null); row.setExcludedBy(null); row.setExclusionReason(null);
    }
}

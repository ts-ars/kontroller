package com.exempal.shiftcounter.features.comment.application;

import com.exempal.shiftcounter.features.user.domain.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class StoppageExclusionService {
    private final ExcludedStoppagePort repository;
    private final CurrentCommentActor actors;

    @Transactional
    public void exclude(long id, String reason) {
        var actor = actors.require();
        requireManager(actor.role());
        repository.exclude(id, actor.displayName(), reason == null ? "" : reason.trim(), LocalDateTime.now());
    }

    @Transactional
    public void restore(long id) {
        var actor = actors.require();
        requireManager(actor.role());
        repository.restore(id);
    }

    private void requireManager(UserRole role) {
        if (role != UserRole.ADMIN && role != UserRole.OWNER) throw new CommentAccessDeniedException();
    }
}

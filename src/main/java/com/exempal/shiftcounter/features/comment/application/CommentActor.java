package com.exempal.shiftcounter.features.comment.application;

import com.exempal.shiftcounter.features.user.domain.UserRole;
import java.util.UUID;

public record CommentActor(UUID userId, String displayName, UserRole role) {
    public boolean mayModify(UUID authorUserId) {
        return role == UserRole.ADMIN || role == UserRole.OWNER || userId.equals(authorUserId);
    }
}

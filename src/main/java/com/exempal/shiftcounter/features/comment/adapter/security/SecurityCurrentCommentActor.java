package com.exempal.shiftcounter.features.comment.adapter.security;

import com.exempal.shiftcounter.features.comment.application.*;
import com.exempal.shiftcounter.features.user.adapter.persistence.AppUserRepository;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityCurrentCommentActor implements CurrentCommentActor {
    private final AppUserRepository users;
    public SecurityCurrentCommentActor(AppUserRepository users) { this.users = users; }

    @Override public CommentActor require() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated())
            throw new AuthenticationCredentialsNotFoundException("Authentication is required");
        var user = users.findByDisplayNameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new AuthenticationCredentialsNotFoundException("Authenticated user no longer exists"));
        return new CommentActor(user.getId(), user.getDisplayName(), user.getRole());
    }
}

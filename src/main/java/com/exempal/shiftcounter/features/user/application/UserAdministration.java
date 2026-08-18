package com.exempal.shiftcounter.features.user.application;

import com.exempal.shiftcounter.features.user.domain.*;
import java.time.Instant;
import java.util.*;

public interface UserAdministration {
    record UserView(UUID id, String displayName, UserRole role, UserStatus status, int failedAttempts, Instant lockedUntil, long version) {}
    List<UserView> list();
    void create(String displayName, String pin, UserRole role);
    void updateProfile(UUID id, String displayName, UserRole role);
    void changePin(UUID id, String pin);
    void changeStatus(UUID id, UserStatus status);
}

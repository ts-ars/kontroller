package com.exempal.shiftcounter.features.user.adapter.persistence;

import com.exempal.shiftcounter.features.user.domain.UserRole;
import com.exempal.shiftcounter.features.user.domain.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface AppUserRepository extends JpaRepository<AppUserEntity, UUID> {
    List<AppUserEntity> findAllByOrderByDisplayNameAsc();
    boolean existsByRole(UserRole role);
    boolean existsByDisplayNameIgnoreCase(String displayName);
}

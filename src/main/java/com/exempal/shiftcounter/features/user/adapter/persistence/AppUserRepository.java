package com.exempal.shiftcounter.features.user.adapter.persistence;

import com.exempal.shiftcounter.features.user.domain.UserRole;
import com.exempal.shiftcounter.features.user.domain.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import jakarta.persistence.LockModeType;
import java.util.*;

public interface AppUserRepository extends JpaRepository<AppUserEntity, UUID> {
    List<AppUserEntity> findAllByOrderByDisplayNameAsc();
    boolean existsByRole(UserRole role);
    boolean existsByDisplayNameIgnoreCase(String displayName);
    boolean existsByDisplayNameIgnoreCaseAndIdNot(String displayName, UUID id);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select user from AppUserEntity user order by user.displayName")
    List<AppUserEntity> findAllForUpdate();
}

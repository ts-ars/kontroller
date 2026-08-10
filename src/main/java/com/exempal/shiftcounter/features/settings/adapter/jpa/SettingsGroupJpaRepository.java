package com.exempal.shiftcounter.features.settings.adapter.jpa;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;

public interface SettingsGroupJpaRepository extends JpaRepository<SettingsGroupEntity, String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select group from SettingsGroupEntity group where group.id = :id")
    Optional<SettingsGroupEntity> findByIdForUpdate(String id);
}

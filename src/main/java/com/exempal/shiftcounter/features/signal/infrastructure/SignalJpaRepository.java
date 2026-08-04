package com.exempal.shiftcounter.features.signal.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface SignalJpaRepository extends JpaRepository<SignalEntity, UUID> {
    List<SignalEntity> findAllByTimestampBetween(LocalDateTime start, LocalDateTime end);
}
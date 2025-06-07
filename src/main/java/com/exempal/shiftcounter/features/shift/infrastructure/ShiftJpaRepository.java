package com.exempal.shiftcounter.features.shift.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface ShiftJpaRepository extends JpaRepository<ShiftEntity, Long> {
    Optional<ShiftEntity> findByDate(LocalDate date);
}

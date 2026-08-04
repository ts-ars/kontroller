package com.exempal.shiftcounter.features.shift.infrastructure;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface ShiftJpaRepository extends JpaRepository<ShiftEntity, Long> {

    Optional<ShiftEntity> findByDate(LocalDate date);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from ShiftEntity s where s.date = :date")
    Optional<ShiftEntity> findForUpdateByDate(@Param("date") LocalDate date);
}
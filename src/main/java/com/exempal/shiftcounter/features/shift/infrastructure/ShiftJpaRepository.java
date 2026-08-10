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

    @EntityGraph(attributePaths = {"hourlyActualValues", "hourlyPlanValues", "hourlyLabels"})
    Optional<ShiftEntity> findByDateAndSensorId(LocalDate date, String sensorId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {"hourlyActualValues", "hourlyPlanValues", "hourlyLabels"})
    @Query("select s from ShiftEntity s where s.date = :date and s.sensorId = :sensorId")
    Optional<ShiftEntity> findForUpdateByDateAndSensorId(@Param("date") LocalDate date,
                                                         @Param("sensorId") String sensorId);
}

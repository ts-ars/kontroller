package com.exempal.shiftcounter.features.signal.infrastructure;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

public interface CounterStateJpaRepository extends JpaRepository<CounterStateEntity, String> {
    @Modifying
    @Query(value = "insert into counter_states "
            + "(sensor_id, last_counter_value, last_read_at, production_date, continuity) "
            + "values (:sensorId, :counter, :readAt, :productionDate, 'CONTINUOUS') "
            + "on conflict (sensor_id) do nothing", nativeQuery = true)
    int initializeIfAbsent(@Param("sensorId") String sensorId,
                           @Param("counter") long counter,
                           @Param("readAt") LocalDateTime readAt,
                           @Param("productionDate") LocalDate productionDate);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from CounterStateEntity c where c.sensorId = :sensorId")
    Optional<CounterStateEntity> findForUpdate(@Param("sensorId") String sensorId);
}

package com.exempal.shiftcounter.features.signal.adapter.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface SignalJpaRepository extends JpaRepository<SignalEntity, UUID> {
    @Modifying
    @Transactional
    @Query(value = "insert into signals (id, sensor_id, timestamp, production_date, source, source_identity) "
            + "values (:id, :sensorId, :occurredAt, :productionDate, :source, :sourceIdentity) "
            + "on conflict (sensor_id, source, source_identity) do nothing", nativeQuery = true)
    int insertIfAbsent(@Param("id") UUID id,
                       @Param("sensorId") String sensorId,
                       @Param("occurredAt") LocalDateTime occurredAt,
                       @Param("productionDate") java.time.LocalDate productionDate,
                       @Param("source") String source,
                       @Param("sourceIdentity") String sourceIdentity);

    @Query("select s from SignalEntity s where s.sensorId = :sensorId "
            + "and s.occurredAt >= :start and s.occurredAt < :end order by s.occurredAt")
    List<SignalEntity> findAllInHalfOpenRange(@Param("sensorId") String sensorId,
                                               @Param("start") LocalDateTime start,
                                               @Param("end") LocalDateTime end);

    default List<SignalEntity> findAllInHalfOpenRange(LocalDateTime start, LocalDateTime end) {
        return findAllInHalfOpenRange("sensor-1", start, end);
    }
}

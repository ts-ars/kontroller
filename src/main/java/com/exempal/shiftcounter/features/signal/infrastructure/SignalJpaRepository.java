package com.exempal.shiftcounter.features.signal.infrastructure;

import com.exempal.shiftcounter.features.signal.domain.SignalSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface SignalJpaRepository extends JpaRepository<SignalEntity, UUID> {
    boolean existsBySensorIdAndSourceAndSourceIdentity(String sensorId, SignalSource source,
                                                       String sourceIdentity);

    @Query("select s from SignalEntity s where s.sensorId = :sensorId "
            + "and s.occurredAt >= :start and s.occurredAt < :end order by s.occurredAt")
    List<SignalEntity> findAllInHalfOpenRange(@Param("sensorId") String sensorId,
                                               @Param("start") LocalDateTime start,
                                               @Param("end") LocalDateTime end);

    default List<SignalEntity> findAllInHalfOpenRange(LocalDateTime start, LocalDateTime end) {
        return findAllInHalfOpenRange("sensor-1", start, end);
    }
}

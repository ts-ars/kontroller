package com.exempal.shiftcounter.features.signal.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface SignalJpaRepository extends JpaRepository<SignalEntity, UUID> {
    @Query("select s from SignalEntity s where s.timestamp >= :start and s.timestamp < :end order by s.timestamp")
    List<SignalEntity> findAllInHalfOpenRange(@Param("start") LocalDateTime start,
                                               @Param("end") LocalDateTime end);
}

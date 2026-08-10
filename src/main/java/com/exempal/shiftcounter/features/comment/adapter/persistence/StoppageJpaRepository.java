package com.exempal.shiftcounter.features.comment.adapter.persistence;

import com.exempal.shiftcounter.features.comment.domain.StoppageState;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface StoppageJpaRepository extends JpaRepository<StoppageEntity, Long> {
    @Override
    @EntityGraph(attributePaths = {"explanations"})
    Optional<StoppageEntity> findById(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {"explanations"})
    @Query("select s from StoppageEntity s where s.id = :id")
    Optional<StoppageEntity> findForUpdateById(@Param("id") long id);

    @EntityGraph(attributePaths = {"explanations"})
    @Query("select distinct s from StoppageEntity s where s.shiftId in (select sh.id from ShiftEntity sh where sh.date = :date) and s.sensorKey = :sensorId and s.detectionType is not null")
    List<StoppageEntity> findSystemByShiftDateAndSensorId(@Param("date") LocalDate date,
                                                          @Param("sensorId") String sensorId);

    @EntityGraph(attributePaths = {"explanations"})
    @Query("select distinct s from StoppageEntity s where s.shiftId in (select sh.id from ShiftEntity sh where sh.date between :from and :to) and s.sensorKey = :sensorId and s.detectionType is not null")
    List<StoppageEntity> findSystemByShiftDateBetweenAndSensorId(@Param("from") LocalDate from,
                                                                 @Param("to") LocalDate to,
                                                                 @Param("sensorId") String sensorId);

    @EntityGraph(attributePaths = {"explanations"})
    @Query("""
            select distinct s from StoppageEntity s
            where s.shiftId = :shiftId and s.intervalIndex = :intervalIndex
              and s.state = :state and s.detectionType is not null
            """)
    List<StoppageEntity> findActiveByShiftAndInterval(@Param("shiftId") long shiftId,
                                                       @Param("intervalIndex") int intervalIndex,
                                                       @Param("state") StoppageState state);

    @EntityGraph(attributePaths = {"explanations"})
    @Query("""
            select distinct s from StoppageEntity s
            where s.shiftId = :shiftId and s.sensorKey = :sensorKey
              and s.intervalIndex between :fromInterval and :toInterval
              and s.state = :state and s.detectionType is not null
            """)
    List<StoppageEntity> findActiveByShiftSensorAndIntervalRange(
            @Param("shiftId") long shiftId, @Param("sensorKey") String sensorKey,
            @Param("fromInterval") int fromInterval, @Param("toInterval") int toInterval,
            @Param("state") StoppageState state);
}

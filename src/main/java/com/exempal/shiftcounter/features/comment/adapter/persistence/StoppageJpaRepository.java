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
    @EntityGraph(attributePaths = {"shift", "explanations"})
    Optional<StoppageEntity> findById(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {"shift", "explanations"})
    @Query("select s from StoppageEntity s where s.id = :id")
    Optional<StoppageEntity> findForUpdateById(@Param("id") long id);

    @EntityGraph(attributePaths = {"shift", "explanations"})
    @Query("select distinct s from StoppageEntity s where s.shift.date = :date and s.detectionType is not null")
    List<StoppageEntity> findSystemByShiftDate(@Param("date") LocalDate date);

    @EntityGraph(attributePaths = {"shift", "explanations"})
    @Query("select distinct s from StoppageEntity s where s.shift.date between :from and :to and s.detectionType is not null")
    List<StoppageEntity> findSystemByShiftDateBetween(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @EntityGraph(attributePaths = {"shift", "explanations"})
    @Query("""
            select distinct s from StoppageEntity s
            where s.shift.date = :date and s.intervalIndex = :intervalIndex
              and s.roundedMinutes = :roundedMinutes and s.state = :state
              and s.detectionType is not null
            """)
    List<StoppageEntity> findActiveEquivalent(@Param("date") LocalDate date,
                                               @Param("intervalIndex") int intervalIndex,
                                               @Param("roundedMinutes") int roundedMinutes,
                                               @Param("state") StoppageState state);

    @EntityGraph(attributePaths = {"shift", "explanations"})
    @Query("""
            select distinct s from StoppageEntity s
            where s.shift.id = :shiftId and s.intervalIndex = :intervalIndex
              and s.state = :state and s.detectionType is not null
            """)
    List<StoppageEntity> findActiveByShiftAndInterval(@Param("shiftId") long shiftId,
                                                       @Param("intervalIndex") int intervalIndex,
                                                       @Param("state") StoppageState state);
}

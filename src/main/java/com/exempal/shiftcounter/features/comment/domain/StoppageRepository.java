package com.exempal.shiftcounter.features.comment.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface StoppageRepository extends JpaRepository<StoppageEntry, Long> {

    @Query("SELECT s FROM StoppageEntry s WHERE s.shift.date = :date")
    List<StoppageEntry> findByShiftDate(@Param("date") LocalDate date);

    @Query("SELECT s FROM StoppageEntry s WHERE s.shift.date BETWEEN :from AND :to")
    List<StoppageEntry> findByShiftDateBetween(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("""
SELECT s FROM StoppageEntry s
WHERE s.shift.date = :date
  AND s.hourIndex = :hour
  AND s.minutes = :minutes
""")
    Optional<StoppageEntry> findByShiftDateAndHourIndexAndMinutes(
            @Param("date") LocalDate date,
            @Param("hour") int hourIndex,
            @Param("minutes") double minutes
    );

    @Transactional
    @Modifying
    @Query("""
    DELETE FROM StoppageEntry s
    WHERE s.shift.id = :shiftId
      AND s.hourIndex = :hourIndex
      AND s.type IN :types
""")
    void deleteAllByShiftIdAndHourIndexAndTypeIn(
            @Param("shiftId") Long shiftId,
            @Param("hourIndex") int hourIndex,
            @Param("types") List<StoppageType> types
    );
}
package com.exempal.shiftcounter.features.comment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface StoppageRepository extends JpaRepository<StoppageEntry, Long> {
    List<StoppageEntry> findByDate(LocalDate date);                        // ← для ShiftPage
    List<StoppageEntry> findByDateBetween(LocalDate from, LocalDate to);  // ← для StoppageController
}


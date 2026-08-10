package com.exempal.shiftcounter.features.comment.application;

import com.exempal.shiftcounter.features.comment.domain.Stoppage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface StoppageRepository {
    Optional<Stoppage> findById(long id);
    Optional<Stoppage> findForUpdateById(long id);
    List<Stoppage> findByShiftDate(LocalDate date);
    List<Stoppage> findByShiftDateBetween(LocalDate from, LocalDate to);
    Optional<Stoppage> findActiveEquivalent(LocalDate date, int intervalIndex, int roundedMinutes);
    List<Stoppage> findActiveByShiftAndInterval(long shiftId, int intervalIndex);
    Stoppage save(Stoppage stoppage);
    List<Stoppage> saveAll(List<Stoppage> stoppages);
}

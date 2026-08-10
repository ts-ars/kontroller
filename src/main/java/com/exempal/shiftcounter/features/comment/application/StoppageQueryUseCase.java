package com.exempal.shiftcounter.features.comment.application;

import com.exempal.shiftcounter.features.comment.domain.Stoppage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class StoppageQueryUseCase {
    private final StoppageRepository repository;

    public StoppageQueryUseCase(StoppageRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<Stoppage> findBetween(LocalDate from, LocalDate to, String sensorId) {
        return repository.findByShiftDateBetweenAndSensorId(from, to, sensorId);
    }
}

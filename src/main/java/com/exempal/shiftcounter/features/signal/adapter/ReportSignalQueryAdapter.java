package com.exempal.shiftcounter.features.signal.adapter;

import com.exempal.shiftcounter.features.report.application.ReportSignalQueryPort;
import com.exempal.shiftcounter.features.signal.adapter.persistence.SignalJpaRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ReportSignalQueryAdapter implements ReportSignalQueryPort {
    private final SignalJpaRepository repository;

    public ReportSignalQueryAdapter(SignalJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public long count(String sensorId, LocalDateTime fromInclusive, LocalDateTime toExclusive) {
        return repository.countInHalfOpenRange(sensorId, fromInclusive, toExclusive);
    }
}

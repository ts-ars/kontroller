package com.exempal.shiftcounter.features.signal.infrastructure;

import com.exempal.shiftcounter.features.sensor.domain.SensorId;
import com.exempal.shiftcounter.features.signal.domain.Signal;
import com.exempal.shiftcounter.features.signal.domain.SignalStoragePort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class SignalJpaAdapter implements SignalStoragePort {
    private final SignalJpaRepository repository;

    public SignalJpaAdapter(SignalJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public boolean saveIfAbsent(Signal signal) {
        return repository.insertIfAbsent(signal.id(), signal.sensorId().value(), signal.occurredAt(),
                signal.productionDate(), signal.source().name(), signal.sourceIdentity()) == 1;
    }

    @Override
    @Transactional
    public void save(Signal signal) {
        saveIfAbsent(signal);
    }

    @Override
    public List<Signal> findBySensorAndRange(String sensorId, LocalDateTime from, LocalDateTime to) {
        return repository.findAllInHalfOpenRange(sensorId, from, to).stream()
                .map(entity -> new Signal(entity.getId(), SensorId.of(entity.getSensorId()),
                        entity.getOccurredAt(), entity.getProductionDate(), entity.getSource(),
                        entity.getSourceIdentity()))
                .toList();
    }

    public List<Signal> findByRange(LocalDateTime from, LocalDateTime to) {
        return findBySensorAndRange("sensor-1", from, to);
    }
}

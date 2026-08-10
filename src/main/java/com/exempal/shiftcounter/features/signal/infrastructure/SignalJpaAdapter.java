package com.exempal.shiftcounter.features.signal.infrastructure;

import com.exempal.shiftcounter.features.sensor.domain.SensorId;
import com.exempal.shiftcounter.features.signal.domain.Signal;
import com.exempal.shiftcounter.features.signal.domain.SignalStoragePort;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class SignalJpaAdapter implements SignalStoragePort {
    private final SignalJpaRepository repository;

    public SignalJpaAdapter(SignalJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean saveIfAbsent(Signal signal) {
        if (repository.existsBySensorIdAndSourceAndSourceIdentity(signal.sensorId().value(), signal.source(),
                signal.sourceIdentity())) return false;
        repository.save(new SignalEntity(signal.id(), signal.sensorId().value(), signal.occurredAt(),
                signal.productionDate(), signal.source(), signal.sourceIdentity()));
        return true;
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

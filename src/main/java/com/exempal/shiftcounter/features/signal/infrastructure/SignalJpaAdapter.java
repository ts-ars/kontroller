package com.exempal.shiftcounter.features.signal.infrastructure;

import com.exempal.shiftcounter.features.signal.domain.Signal;
import com.exempal.shiftcounter.features.signal.domain.SignalStoragePort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class SignalJpaAdapter implements SignalStoragePort {

    private final SignalJpaRepository repository;

    public SignalJpaAdapter(SignalJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public void save(Signal signal) {
        repository.save(new SignalEntity(UUID.randomUUID(), signal.timestamp()));
    }

    @Override
    public List<Signal> findByRange(java.time.LocalDateTime from, java.time.LocalDateTime to) {
        return repository.findAllInHalfOpenRange(from, to)
                .stream()
                .map(entity -> new Signal(entity.getTimestamp()))
                .toList();
    }
}

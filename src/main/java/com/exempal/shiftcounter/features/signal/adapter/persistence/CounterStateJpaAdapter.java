package com.exempal.shiftcounter.features.signal.adapter.persistence;

import com.exempal.shiftcounter.features.signal.application.CounterStateStoragePort;
import com.exempal.shiftcounter.features.sensor.domain.SensorId;
import com.exempal.shiftcounter.features.signal.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class CounterStateJpaAdapter implements CounterStateStoragePort {
    private final CounterStateJpaRepository repository;

    @Override
    public CounterStateLoad getOrInitializeForUpdate(SensorId sensorId, long currentCounter,
                                                     LocalDateTime readAt, LocalDate productionDate) {
        boolean initialized = repository.initializeIfAbsent(sensorId.value(), currentCounter, readAt,
                productionDate) == 1;
        CounterStateEntity entity = repository.findForUpdate(sensorId.value()).orElseThrow();
        return new CounterStateLoad(toDomain(entity), initialized);
    }

    @Override
    public void save(CounterState state) {
        repository.save(new CounterStateEntity(state.sensorId().value(), state.lastCounterValue(),
                state.lastReadAt(), state.productionDate(), state.continuity()));
    }

    private CounterState toDomain(CounterStateEntity entity) {
        return new CounterState(SensorId.of(entity.getSensorId()), entity.getLastCounterValue(),
                entity.getLastReadAt(), entity.getProductionDate(), entity.getContinuity());
    }
}

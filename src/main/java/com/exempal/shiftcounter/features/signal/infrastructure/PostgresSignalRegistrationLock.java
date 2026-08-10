package com.exempal.shiftcounter.features.signal.infrastructure;

import com.exempal.shiftcounter.features.signal.domain.SignalRegistrationLock;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class PostgresSignalRegistrationLock implements SignalRegistrationLock {
    private final EntityManager entityManager;

    @Override
    public void acquire(LocalDate productionDate, String sensorId) {
        entityManager.createNativeQuery("select pg_advisory_xact_lock(hashtextextended(:sensorId, :day))")
                .setParameter("sensorId", sensorId)
                .setParameter("day", productionDate.toEpochDay())
                .getSingleResult();
    }
}

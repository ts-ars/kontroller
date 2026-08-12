package com.exempal.shiftcounter.features.signal;

import com.exempal.shiftcounter.features.signal.application.*;
import com.exempal.shiftcounter.features.sensor.domain.SensorId;
import com.exempal.shiftcounter.features.signal.domain.*;
import com.exempal.shiftcounter.features.signal.adapter.persistence.SignalJpaRepository;
import com.exempal.shiftcounter.features.shift.application.ActualDataPort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class Stage7SignalTransactionIntegrationTest {
    @Autowired SignalInputPort registration;
    @Autowired ActualDataPort shifts;
    @Autowired SignalJpaRepository signals;
    @Autowired SignalRegistrationLock registrationLock;
    @Autowired org.springframework.transaction.PlatformTransactionManager transactionManager;

    @Test
    void concurrentUniqueSignalsForOneSensorLoseNoActual() throws Exception {
        int deliveries = 16;
        LocalDateTime occurredAt = LocalDateTime.of(2026, 8, 10, 8, 15);
        List<SignalRegistrationResult> results = runConcurrently(deliveries, index -> registration.register(
                new RegisterSignalCommand(SensorId.of("sensor-2"), occurredAt, SignalSource.RECOVERY,
                        "concurrent-" + index)));

        assertThat(results).allMatch(SignalRegistrationResult::accepted);
        assertThat(signals.count()).isEqualTo(deliveries);
        var shift = shifts.findByDateAndSensorId(LocalDate.of(2026, 8, 10), "sensor-2").orElseThrow();
        assertThat(shift.getActual()).isEqualTo(deliveries);
        int eight = shift.getHourlyLabels().indexOf("08:00");
        assertThat(eight).isNotNegative();
        assertThat(shift.getHourlyActualValues().get(eight)).isEqualTo(deliveries);
    }

    @Test
    void concurrentDuplicateSourceIdentityIncrementsOnlyOnce() throws Exception {
        int deliveries = 16;
        LocalDateTime occurredAt = LocalDateTime.of(2026, 8, 10, 8, 15);
        List<SignalRegistrationResult> results = runConcurrently(deliveries, index -> registration.register(
                new RegisterSignalCommand(SensorId.of("sensor-3"), occurredAt, SignalSource.RECOVERY,
                        "same-physical-signal")));

        assertThat(results).filteredOn(SignalRegistrationResult::accepted).hasSize(1);
        assertThat(signals.count()).isEqualTo(1);
        assertThat(shifts.findByDateAndSensorId(LocalDate.of(2026, 8, 10), "sensor-3").orElseThrow()
                .getActual()).isEqualTo(1);
    }

    @Test
    void differentSensorLocksCanProceedInParallel() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch firstLocked = new CountDownLatch(1);
        CountDownLatch secondLocked = new CountDownLatch(1);
        CountDownLatch releaseFirst = new CountDownLatch(1);
        var transactions = new org.springframework.transaction.support.TransactionTemplate(transactionManager);
        Future<?> first = executor.submit(() -> transactions.executeWithoutResult(status -> {
            registrationLock.acquire(LocalDate.of(2026, 8, 10), "sensor-4");
            firstLocked.countDown();
            await(releaseFirst);
        }));
        assertThat(firstLocked.await(5, TimeUnit.SECONDS)).isTrue();
        Future<?> second = executor.submit(() -> transactions.executeWithoutResult(status -> {
            registrationLock.acquire(LocalDate.of(2026, 8, 10), "sensor-5");
            secondLocked.countDown();
        }));

        assertThat(secondLocked.await(5, TimeUnit.SECONDS)).isTrue();
        releaseFirst.countDown();
        first.get(5, TimeUnit.SECONDS);
        second.get(5, TimeUnit.SECONDS);
        executor.shutdownNow();
    }

    private <T> List<T> runConcurrently(int count, java.util.function.IntFunction<T> action) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(count);
        CountDownLatch ready = new CountDownLatch(count);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<T>> futures = new ArrayList<>();
        for (int index = 0; index < count; index++) {
            int value = index;
            futures.add(executor.submit(() -> {
                ready.countDown();
                start.await();
                return action.apply(value);
            }));
        }
        assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
        start.countDown();
        List<T> results = new ArrayList<>();
        for (Future<T> future : futures) results.add(future.get(30, TimeUnit.SECONDS));
        executor.shutdownNow();
        return results;
    }

    private static void await(CountDownLatch latch) {
        try {
            if (!latch.await(10, TimeUnit.SECONDS)) throw new IllegalStateException("timed out");
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(exception);
        }
    }
}

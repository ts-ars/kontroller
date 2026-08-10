package com.exempal.shiftcounter.features.signal;

import com.exempal.shiftcounter.features.sensor.domain.SensorCatalog;
import com.exempal.shiftcounter.features.shift.application.ActualDataPort;
import com.exempal.shiftcounter.features.signal.application.SignalInputPort;
import com.exempal.shiftcounter.features.signal.domain.RegisterSignalCommand;
import com.exempal.shiftcounter.features.signal.domain.SignalRegistrationResult;
import com.exempal.shiftcounter.features.signal.domain.SignalSource;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Tag("load")
class Stage10SixSensorLoadTest {
    @Autowired SignalInputPort registration;
    @Autowired ActualDataPort shifts;

    @Test
    void parallelInputAcrossSixSensorsLosesAndDuplicatesNothing() throws Exception {
        int perSensor = 20;
        LocalDateTime occurredAt = LocalDateTime.of(2026, 8, 10, 8, 15);
        List<Callable<SignalRegistrationResult>> unique = new ArrayList<>();
        SensorCatalog.all().forEach(sensor -> {
            for (int index = 0; index < perSensor; index++) {
                int signal = index;
                unique.add(() -> registration.register(new RegisterSignalCommand(sensor.id(), occurredAt,
                        SignalSource.RECOVERY, "release-load-" + signal)));
            }
        });
        assertThat(runTogether(unique)).allMatch(SignalRegistrationResult::accepted);

        List<Callable<SignalRegistrationResult>> duplicates = SensorCatalog.all().stream()
                .map(sensor -> (Callable<SignalRegistrationResult>) () -> registration.register(
                        new RegisterSignalCommand(sensor.id(), occurredAt, SignalSource.RECOVERY, "release-load-0")))
                .toList();
        assertThat(runTogether(duplicates)).noneMatch(SignalRegistrationResult::accepted);

        for (int number = 1; number <= 6; number++) {
            String sensorId = "sensor-" + number;
            var shift = shifts.findByDateAndSensorId(LocalDate.of(2026, 8, 10), sensorId).orElseThrow();
            assertThat(shift.getActual()).as(sensorId).isEqualTo(perSensor);
            assertThat(shift.getHourlyActualValues()).startsWith(perSensor);
        }
    }

    private static <T> List<T> runTogether(List<Callable<T>> tasks) throws Exception {
        int workers = Math.min(24, tasks.size());
        var executor = Executors.newFixedThreadPool(workers);
        CountDownLatch ready = new CountDownLatch(workers);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<T>> futures = tasks.stream().map(task -> executor.submit(() -> {
            ready.countDown();
            start.await();
            return task.call();
        })).toList();
        assertThat(ready.await(10, TimeUnit.SECONDS)).isTrue();
        start.countDown();
        List<T> results = new ArrayList<>();
        for (Future<T> future : futures) results.add(future.get(60, TimeUnit.SECONDS));
        executor.shutdownNow();
        return results;
    }
}

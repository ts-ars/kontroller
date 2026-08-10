package com.exempal.shiftcounter.features.shift.application;

import com.exempal.shiftcounter.common.domain.EventPublisherPort;
import com.exempal.shiftcounter.features.shift.domain.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@Transactional
public class ShiftPlannerUseCase {

    private static final Logger log = LoggerFactory.getLogger(ShiftPlannerUseCase.class);

    private final ActualDataPort actualDataPort;
    private final ShiftInitializer shiftInitializer;
    private final EventPublisherPort eventPublisherPort;

    public ShiftPlannerUseCase(
            ActualDataPort actualDataPort,
            ShiftInitializer shiftInitializer,
            EventPublisherPort eventPublisherPort
    ) {
        this.actualDataPort = actualDataPort;
        this.shiftInitializer = shiftInitializer;
        this.eventPublisherPort = eventPublisherPort;
    }

    public Shift getOrCreateShift(LocalDate date) {
        return getOrCreateShift(date, com.exempal.shiftcounter.features.sensor.domain.SensorCatalog.SENSOR_1);
    }

    public Shift getOrCreateShift(LocalDate date, String sensorId) {
        return actualDataPort.findByDateAndSensorId(date, sensorId)
                .orElseGet(() -> {
                    log.info("🆕 Создание смены на {}", date);
                    return shiftInitializer.createNewShift(date, sensorId);
                });
    }

    public void updateShift(Shift updated) {
        actualDataPort.save(updated);
        log.info("💾 Смена сохранена: {}", updated.getDate());
        eventPublisherPort.publish(new ShiftUpdatedEvent(
                updated.getDate(),
                updated.getSensorId(),
                updated.getHourlyActualValues(),
                updated.getHourlyPlanValues(),
                updated.getHourlyLabels()
        ));
    }

}

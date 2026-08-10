package com.exempal.shiftcounter.features.shift.application;

import com.exempal.shiftcounter.common.domain.EventPublisherPort;
import com.exempal.shiftcounter.features.shift.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class ShiftInitializerService implements ShiftInitializer {

    private static final Logger log = LoggerFactory.getLogger(ShiftInitializerService.class);

    private final ShiftFactory shiftFactory;
    private final ActualDataPort actualDataPort;
    private final EventPublisherPort eventPublisher;

    public ShiftInitializerService(
            ShiftFactory shiftFactory,
            ActualDataPort actualDataPort,
            EventPublisherPort eventPublisher
    ) {
        this.shiftFactory = shiftFactory;
        this.actualDataPort = actualDataPort;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Shift createNewShift(LocalDate date, String sensorId) {
        return actualDataPort.findByDateAndSensorId(date, sensorId).orElseGet(() -> {
            Shift shift = shiftFactory.createNewShift(date, sensorId);
            actualDataPort.save(shift);

            log.info("✅ Смена создана: {} | План: {}", date, shift.getHourlyPlanValues());

            eventPublisher.publish(new ShiftUpdatedEvent(
                    shift.getDate(), shift.getSensorId(),
                    shift.getHourlyActualValues(),
                    shift.getHourlyPlanValues(),
                    shift.getHourlyLabels()
            ));

            return shift;
        });
    }

    @Override
    public Shift recalculateShift(LocalDate date, String sensorId) {
        Shift existing = actualDataPort.findByDateAndSensorId(date, sensorId)
                .orElseThrow(() -> new ShiftNotFoundException(date));

        Shift recalculated = shiftFactory.recalculateFrom(existing);
        actualDataPort.saveOrReplace(recalculated);

        log.info("🔁 Смена обновлена: {} | План: {} | Факт: {}", date, recalculated.getHourlyPlanValues(), recalculated.getHourlyActualValues());

        eventPublisher.publish(new ShiftUpdatedEvent(
                recalculated.getDate(), recalculated.getSensorId(),
                recalculated.getHourlyActualValues(),
                recalculated.getHourlyPlanValues(),
                recalculated.getHourlyLabels()
        ));

        return recalculated;
    }
}

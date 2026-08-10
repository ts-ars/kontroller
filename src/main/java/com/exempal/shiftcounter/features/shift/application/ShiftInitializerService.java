package com.exempal.shiftcounter.features.shift.application;

import com.exempal.shiftcounter.common.domain.EventPublisherPort;
import com.exempal.shiftcounter.features.shift.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;

@Service
@Transactional
public class ShiftInitializerService implements ShiftInitializer {

    private static final Logger log = LoggerFactory.getLogger(ShiftInitializerService.class);

    private final ShiftSettingsPort settings;
    private final ActualDataPort actualDataPort;
    private final EventPublisherPort eventPublisher;

    public ShiftInitializerService(
            ShiftSettingsPort settings,
            ActualDataPort actualDataPort,
            EventPublisherPort eventPublisher
    ) {
        this.settings = settings;
        this.actualDataPort = actualDataPort;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Shift createNewShift(LocalDate date, String sensorId) {
        return actualDataPort.findByDateAndSensorId(date, sensorId).orElseGet(() -> {
            ShiftSettings current = settings.getForSensor(sensorId);
            Shift shift = new Shift(date, sensorId, current.plans(), 0,
                    Collections.nCopies(current.labels().size(), 0), current.labels());
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

    private Shift recalculateShift(LocalDate date, String sensorId) {
        Shift existing = actualDataPort.findByDateAndSensorId(date, sensorId)
                .orElseThrow(() -> new ShiftNotFoundException(date));

        ShiftSettings current = settings.getForSensor(sensorId);
        var actual = new java.util.ArrayList<>(existing.getHourlyActualValues());
        while (actual.size() < current.labels().size()) actual.add(0);
        if (actual.size() > current.labels().size()) actual = new java.util.ArrayList<>(actual.subList(0, current.labels().size()));
        Shift recalculated = existing.withUpdatedStructure(current.labels(), current.plans(), actual);
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

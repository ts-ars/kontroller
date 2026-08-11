package com.exempal.shiftcounter.features.settings.application;

import com.exempal.shiftcounter.common.domain.EventPublisherPort;
import com.exempal.shiftcounter.features.sensor.domain.SensorCatalog;
import com.exempal.shiftcounter.features.sensor.domain.SensorDefinition;
import com.exempal.shiftcounter.features.settings.domain.IntervalSetting;
import com.exempal.shiftcounter.features.settings.domain.SettingUpdatedEvent;
import com.exempal.shiftcounter.features.settings.domain.SettingsGroup;
import com.exempal.shiftcounter.features.settings.domain.SettingsRow;
import com.exempal.shiftcounter.features.settings.domain.SettingsSnapshot;
import com.exempal.shiftcounter.features.shift.application.ActualDataPort;
import com.exempal.shiftcounter.features.shift.application.ProductionDayService;
import com.exempal.shiftcounter.features.shift.application.ShiftIntervalService;
import com.exempal.shiftcounter.features.shift.application.ShiftTimeCorrectionService;
import com.exempal.shiftcounter.features.shift.domain.ShiftUpdatedEvent;
import com.exempal.shiftcounter.features.signal.application.SignalRegistrationLock;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Service
public class SettingsGroupService {
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");
    private final SettingsRepository settings;
    private final ActualDataPort shifts;
    private final ShiftTimeCorrectionService corrections;
    private final ShiftIntervalService intervalService;
    private final ProductionDayService productionDays;
    private final SignalRegistrationLock signalLocks;
    private final EventPublisherPort events;

    public SettingsGroupService(SettingsRepository settings, ActualDataPort shifts,
                                ShiftTimeCorrectionService corrections, ShiftIntervalService intervalService,
                                ProductionDayService productionDays, SignalRegistrationLock signalLocks,
                                EventPublisherPort events) {
        this.settings = settings;
        this.shifts = shifts;
        this.corrections = corrections;
        this.intervalService = intervalService;
        this.productionDays = productionDays;
        this.signalLocks = signalLocks;
        this.events = events;
    }

    @Transactional(readOnly = true)
    public SettingsSnapshot getSnapshot(String groupId) {
        requireApprovedRoute(groupId);
        return snapshot(settings.findById(SensorCatalog.SHARED_SETTINGS_GROUP),
                settings.findById(SensorCatalog.INDEPENDENT_SETTINGS_GROUP));
    }

    @Transactional
    public SettingsSnapshot update(UpdateSettingsCommand command) {
        requireApprovedRoute(command.groupId());
        SettingsSnapshot requested = parse(command);

        SettingsGroup previousShared = settings.findByIdForUpdate(SensorCatalog.SHARED_SETTINGS_GROUP);
        SettingsGroup previousIndependent = settings.findByIdForUpdate(SensorCatalog.INDEPENDENT_SETTINGS_GROUP);
        SettingsSnapshot previous = snapshot(previousShared, previousIndependent);

        boolean timeChanged = !hours(previous).equals(hours(requested));
        boolean sharedChanged = !sharedPlans(previous).equals(sharedPlans(requested));
        boolean sensor6Changed = !sensor6Plans(previous).equals(sensor6Plans(requested));
        List<SensorDefinition> affected = affectedSensors(timeChanged, sharedChanged, sensor6Changed);

        var now = productionDays.now();
        LocalDate date = productionDays.resolve(now).date();
        affected.stream().map(sensor -> sensor.id().value()).sorted()
                .forEach(sensorId -> signalLocks.acquire(date, sensorId));

        settings.save(toGroup(previousShared, requested, true));
        settings.save(toGroup(previousIndependent, requested, false));

        List<String> configuredHours = requested.rows().stream()
                .map(row -> row.hour().format(TIME)).toList();
        for (SensorDefinition sensor : affected) {
            String sensorId = sensor.id().value();
            shifts.findByDateAndSensorId(date, sensorId).ifPresent(current -> {
                List<Integer> plans = plansFor(sensor, requested);
                var saved = corrections.apply(current, configuredHours, plans, timeChanged, now);
                events.publish(new ShiftUpdatedEvent(saved.getDate(), sensorId,
                        saved.getHourlyActualValues(), saved.getHourlyPlanValues(), saved.getHourlyLabels()));
            });
        }
        events.publish(new SettingUpdatedEvent(command.groupId(), "updated"));
        return requested;
    }

    private SettingsSnapshot parse(UpdateSettingsCommand command) {
        if (command.hours() == null || command.sharedPlans() == null || command.sensor6Plans() == null
                || command.hours().size() < SettingsSnapshot.STANDARD_ROW_COUNT
                || command.hours().size() != command.sharedPlans().size()
                || command.hours().size() != command.sensor6Plans().size()) {
            throw new IllegalArgumentException("Hour and both editable plans must have at least 16 matching rows");
        }
        List<SettingsRow> rows = new ArrayList<>();
        for (int index = 0; index < command.hours().size(); index++) {
            Integer shared = command.sharedPlans().get(index);
            Integer sensor6 = command.sensor6Plans().get(index);
            if (shared == null || sensor6 == null) throw new IllegalArgumentException("Plan is required");
            try {
                rows.add(new SettingsRow(LocalTime.parse(command.hours().get(index), TIME), shared, sensor6));
            } catch (DateTimeParseException exception) {
                throw new IllegalArgumentException("Hour must use HH:mm format", exception);
            }
        }
        intervalService.resolve(LocalDate.of(2000, 1, 1), command.hours(), command.hours().size());
        return new SettingsSnapshot(rows);
    }

    private SettingsSnapshot snapshot(SettingsGroup shared, SettingsGroup independent) {
        if (!times(shared).equals(times(independent))) {
            throw new IllegalStateException("Settings groups must use one common Hour timeline");
        }
        if (shared.intervals().size() != independent.intervals().size()) {
            throw new IllegalStateException("Settings groups must have the same row count");
        }
        List<SettingsRow> rows = new ArrayList<>();
        for (int index = 0; index < shared.intervals().size(); index++) {
            rows.add(new SettingsRow(shared.intervals().get(index).startTime(),
                    shared.intervals().get(index).plan(), independent.intervals().get(index).plan()));
        }
        return new SettingsSnapshot(rows);
    }

    private SettingsGroup toGroup(SettingsGroup previous, SettingsSnapshot snapshot, boolean shared) {
        List<IntervalSetting> intervals = new ArrayList<>();
        for (int index = 0; index < snapshot.rows().size(); index++) {
            SettingsRow row = snapshot.rows().get(index);
            intervals.add(new IntervalSetting(row.hour(), shared ? row.sharedPlan() : row.sensor6Plan(), index));
        }
        return new SettingsGroup(previous.id(), previous.name(), previous.enabled(), intervals);
    }

    private List<SensorDefinition> affectedSensors(boolean timeChanged, boolean sharedChanged,
                                                   boolean sensor6Changed) {
        if (timeChanged) return SensorCatalog.all();
        return SensorCatalog.all().stream().filter(sensor ->
                (sharedChanged && !sensor.id().value().equals(SensorCatalog.SENSOR_6))
                        || (sensor6Changed && sensor.id().value().equals(SensorCatalog.SENSOR_6))).toList();
    }

    private List<Integer> plansFor(SensorDefinition sensor, SettingsSnapshot snapshot) {
        if (sensor.id().value().equals(SensorCatalog.SENSOR_6)) return sensor6Plans(snapshot);
        if (sensor.id().value().equals(SensorCatalog.SENSOR_5)) {
            return snapshot.rows().stream().map(SettingsRow::sensor5Plan).toList();
        }
        return sharedPlans(snapshot);
    }

    private List<LocalTime> times(SettingsGroup group) {
        return group.intervals().stream().map(IntervalSetting::startTime).toList();
    }

    private List<LocalTime> hours(SettingsSnapshot snapshot) {
        return snapshot.rows().stream().map(SettingsRow::hour).toList();
    }

    private List<Integer> sharedPlans(SettingsSnapshot snapshot) {
        return snapshot.rows().stream().map(SettingsRow::sharedPlan).toList();
    }

    private List<Integer> sensor6Plans(SettingsSnapshot snapshot) {
        return snapshot.rows().stream().map(SettingsRow::sensor6Plan).toList();
    }

    private void requireApprovedRoute(String groupId) {
        if (!SensorCatalog.SHARED_SETTINGS_GROUP.equals(groupId)
                && !SensorCatalog.INDEPENDENT_SETTINGS_GROUP.equals(groupId)) {
            throw new IllegalArgumentException("Unknown settings group: " + groupId);
        }
    }
}

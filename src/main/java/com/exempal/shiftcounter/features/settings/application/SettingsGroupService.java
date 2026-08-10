package com.exempal.shiftcounter.features.settings.application;

import com.exempal.shiftcounter.common.domain.EventPublisherPort;
import com.exempal.shiftcounter.features.sensor.domain.SensorCatalog;
import com.exempal.shiftcounter.features.settings.domain.*;
import com.exempal.shiftcounter.features.shift.application.ProductionDayService;
import com.exempal.shiftcounter.features.shift.application.ShiftIntervalService;
import com.exempal.shiftcounter.features.shift.application.ShiftTimeCorrectionService;
import com.exempal.shiftcounter.features.shift.application.ActualDataPort;
import com.exempal.shiftcounter.features.shift.domain.ShiftUpdatedEvent;
import com.exempal.shiftcounter.features.signal.application.SignalRegistrationLock;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
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
        this.settings = settings; this.shifts = shifts; this.corrections = corrections;
        this.intervalService = intervalService; this.productionDays = productionDays;
        this.signalLocks = signalLocks; this.events = events;
    }

    @Transactional(readOnly = true)
    public SettingsGroup get(String groupId) {
        requireApprovedGroup(groupId);
        return settings.findById(groupId);
    }

    @Transactional
    public SettingsGroup update(UpdateSettingsGroupCommand command) {
        requireApprovedGroup(command.groupId());
        validate(command);
        SettingsGroup previous = settings.findByIdForUpdate(command.groupId());
        var now = productionDays.now();
        LocalDate date = productionDays.resolve(now).date();
        List<String> members = SensorCatalog.all().stream()
                .filter(sensor -> sensor.settingsGroupId().equals(command.groupId()))
                .map(sensor -> sensor.id().value()).sorted().toList();
        members.forEach(sensor -> signalLocks.acquire(date, sensor));

        List<IntervalSetting> values = java.util.stream.IntStream.range(0, command.hours().size())
                .mapToObj(index -> new IntervalSetting(LocalTime.parse(command.hours().get(index), TIME),
                        command.plans().get(index), index)).toList();
        SettingsGroup updated = new SettingsGroup(command.groupId(), command.name(), command.enabled(), values);
        boolean timeChanged = !times(previous).equals(times(updated));
        boolean planChanged = !plans(previous).equals(plans(updated));
        settings.save(updated);

        if (timeChanged || planChanged) {
            for (String sensorId : members) {
                shifts.findByDateAndSensorId(date, sensorId).ifPresent(current -> {
                    var saved = corrections.apply(current, command.hours(), command.plans(), timeChanged, now);
                    events.publish(new ShiftUpdatedEvent(saved.getDate(), sensorId,
                            saved.getHourlyActualValues(), saved.getHourlyPlanValues(), saved.getHourlyLabels()));
                });
            }
        }
        events.publish(new SettingUpdatedEvent(command.groupId(), "updated"));
        return updated;
    }

    private void validate(UpdateSettingsGroupCommand command) {
        if (command.name() == null || command.name().isBlank()) throw new IllegalArgumentException("Name is required");
        if (command.hours() == null || command.plans() == null || command.hours().isEmpty()
                || command.hours().size() != command.plans().size()) {
            throw new IllegalArgumentException("Time and Plan must have the same non-empty size");
        }
        if (command.plans().stream().anyMatch(value -> value == null || value < 0)) {
            throw new IllegalArgumentException("Plan must not be negative");
        }
        intervalService.resolve(LocalDate.of(2000, 1, 1), command.hours(), command.plans().size());
    }

    private List<LocalTime> times(SettingsGroup group) {
        return group.intervals().stream().map(IntervalSetting::startTime).toList();
    }
    private List<Integer> plans(SettingsGroup group) {
        return group.intervals().stream().map(IntervalSetting::plan).toList();
    }
    private void requireApprovedGroup(String groupId) {
        if (!SensorCatalog.GROUP_1.equals(groupId) && !SensorCatalog.GROUP_2.equals(groupId)) {
            throw new IllegalArgumentException("Unknown settings group: " + groupId);
        }
    }
}

package com.exempal.shiftcounter.features.settings.integration;

import com.exempal.shiftcounter.features.comment.application.ReconcileStoppagesUseCase;
import com.exempal.shiftcounter.features.settings.application.SettingsGroupService;
import com.exempal.shiftcounter.features.settings.application.UpdateSettingsGroupCommand;
import com.exempal.shiftcounter.features.settings.domain.SettingsRepository;
import com.exempal.shiftcounter.features.settings.domain.SettingUpdatedEvent;
import com.exempal.shiftcounter.features.shift.application.ProductionDayService;
import com.exempal.shiftcounter.features.shift.domain.ActualDataPort;
import com.exempal.shiftcounter.features.shift.domain.Shift;
import com.exempal.shiftcounter.features.signal.domain.*;
import com.exempal.shiftcounter.features.sensor.domain.SensorId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@SpringBootTest
@RecordApplicationEvents
class Stage8SettingsGroupIntegrationTest {
    private static final List<String> HOURS = List.of("08:00", "09:00", "10:00", "11:00",
            "12:30", "13:30", "14:30", "15:30");
    @Autowired SettingsGroupService service;
    @Autowired SettingsRepository settings;
    @Autowired ActualDataPort shifts;
    @Autowired ProductionDayService productionDays;
    @Autowired SignalStoragePort signals;
    @MockBean ReconcileStoppagesUseCase reconcile;
    @Autowired ApplicationEvents applicationEvents;

    @Test
    void planOnlyUpdateAffectsCurrentMemberSensorsAndKeepsOtherGroupAndHistoryStable() {
        var date = productionDays.resolve(productionDays.now()).date();
        shifts.save(shift(date, "sensor-1", 3));
        shifts.save(shift(date, "sensor-2", 4));
        shifts.save(shift(date, "sensor-5", 5));
        shifts.save(shift(date.minusDays(1), "sensor-1", 6));
        var group2Before = settings.findById("settings-group-2");
        List<Integer> plans = List.of(101, 102, 103, 104, 105, 106, 107, 108);

        service.update(new UpdateSettingsGroupCommand("settings-group-1", "Sensors 1-4", true, HOURS, plans));

        assertThat(shifts.findByDateAndSensorId(date, "sensor-1").orElseThrow().getHourlyPlanValues()).isEqualTo(plans);
        assertThat(shifts.findByDateAndSensorId(date, "sensor-1").orElseThrow().getActual()).isEqualTo(3);
        assertThat(shifts.findByDateAndSensorId(date, "sensor-2").orElseThrow().getHourlyPlanValues()).isEqualTo(plans);
        assertThat(shifts.findByDateAndSensorId(date, "sensor-5").orElseThrow().getHourlyPlanValues()).containsOnly(10);
        assertThat(settings.findById("settings-group-2")).isEqualTo(group2Before);
        assertThat(shifts.findByDateAndSensorId(date.minusDays(1), "sensor-1").orElseThrow()
                .getHourlyPlanValues()).containsOnly(10);
        verify(reconcile, never()).reconcile(argThat(command -> command.sensorKey().equals("sensor-5")));
        assertThat(applicationEvents.stream(SettingUpdatedEvent.class)).hasSize(1);
        assertThat(applicationEvents.stream(com.exempal.shiftcounter.features.shift.domain.ShiftUpdatedEvent.class))
                .hasSize(2);
    }

    @Test
    void failureDuringOneMemberReconcileRollsBackGroupAndEveryShift() {
        var date = productionDays.resolve(productionDays.now()).date();
        shifts.save(shift(date, "sensor-1", 3));
        shifts.save(shift(date, "sensor-2", 4));
        var before = settings.findById("settings-group-1");
        doAnswer(invocation -> {
            var command = invocation.getArgument(0, com.exempal.shiftcounter.features.comment.application.ReconcileStoppagesCommand.class);
            if (command.sensorKey().equals("sensor-2")) throw new IllegalStateException("forced group failure");
            return null;
        }).when(reconcile).reconcile(any());

        assertThatThrownBy(() -> service.update(new UpdateSettingsGroupCommand("settings-group-1",
                "changed", true, HOURS, List.of(201, 202, 203, 204, 205, 206, 207, 208))))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("forced group failure");

        assertThat(settings.findById("settings-group-1")).isEqualTo(before);
        assertThat(shifts.findByDateAndSensorId(date, "sensor-1").orElseThrow().getHourlyPlanValues()).containsOnly(10);
        assertThat(shifts.findByDateAndSensorId(date, "sensor-2").orElseThrow().getHourlyPlanValues()).containsOnly(10);
        assertThat(applicationEvents.stream(SettingUpdatedEvent.class)).isEmpty();
        assertThat(applicationEvents.stream(com.exempal.shiftcounter.features.shift.domain.ShiftUpdatedEvent.class))
                .isEmpty();
    }

    @Test
    void timeChangeRedistributesPersistedSignalsForMemberSensor() {
        var date = productionDays.resolve(productionDays.now()).date();
        shifts.save(shift(date, "sensor-1", 2));
        signals.save(new Signal(java.util.UUID.randomUUID(), SensorId.of("sensor-1"),
                date.atTime(8, 45), date, SignalSource.RECOVERY, "stage8-0845"));
        signals.save(new Signal(java.util.UUID.randomUUID(), SensorId.of("sensor-1"),
                date.atTime(9, 45), date, SignalSource.RECOVERY, "stage8-0945"));
        List<String> changedHours = List.of("08:30", "09:30", "10:30", "11:30",
                "12:30", "13:30", "14:30", "15:30");

        service.update(new UpdateSettingsGroupCommand("settings-group-1", "Sensors 1-4", true,
                changedHours, java.util.Collections.nCopies(changedHours.size(), 10)));

        var updated = shifts.findByDateAndSensorId(date, "sensor-1").orElseThrow();
        assertThat(updated.getHourlyLabels()).isEqualTo(changedHours);
        assertThat(updated.getHourlyActualValues()).startsWith(1, 1);
        assertThat(updated.getActual()).isEqualTo(2);
    }

    private Shift shift(java.time.LocalDate date, String sensor, int actual) {
        var actuals = new java.util.ArrayList<>(java.util.Collections.nCopies(HOURS.size(), 0));
        actuals.set(0, actual);
        return new Shift(null, date, sensor, java.util.Collections.nCopies(HOURS.size(), 10), actual,
                actuals, HOURS);
    }
}

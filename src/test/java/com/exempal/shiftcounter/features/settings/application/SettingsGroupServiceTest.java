package com.exempal.shiftcounter.features.settings.application;

import com.exempal.shiftcounter.common.domain.EventPublisherPort;
import com.exempal.shiftcounter.features.signal.application.SignalRegistrationLock;
import com.exempal.shiftcounter.features.shift.application.ActualDataPort;
import com.exempal.shiftcounter.features.shift.application.ProductionDayService;
import com.exempal.shiftcounter.features.shift.application.ShiftIntervalService;
import com.exempal.shiftcounter.features.shift.application.ShiftTimeCorrectionService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

class SettingsGroupServiceTest {
    @Test
    void rejectsRemovalOfAnyStandardRowBeforeTakingLocksOrWriting() {
        SettingsRepository repository = mock(SettingsRepository.class);
        ActualDataPort shifts = mock(ActualDataPort.class);
        SignalRegistrationLock locks = mock(SignalRegistrationLock.class);
        EventPublisherPort events = mock(EventPublisherPort.class);
        SettingsGroupService service = new SettingsGroupService(repository, shifts,
                mock(ShiftTimeCorrectionService.class), new ShiftIntervalService(),
                mock(ProductionDayService.class), locks, events);

        assertThatThrownBy(() -> service.update(new UpdateSettingsCommand("settings-group-1",
                List.of("07:00"), List.of(100), List.of(300))))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("at least 16");
        verifyNoInteractions(repository, shifts, locks, events);
    }
}

package com.exempal.shiftcounter.features.shift.adapter.event;

import com.exempal.shiftcounter.features.shift.domain.ActualDataPort;
import com.exempal.shiftcounter.features.shift.domain.Shift;
import com.exempal.shiftcounter.features.shift.domain.ShiftTestFactory;
import com.exempal.shiftcounter.features.shift.domain.ShiftUpdatedEvent;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShiftUpdatedListenerTest {

    @Mock
    private ActualDataPort shiftPort;

    @InjectMocks
    private ShiftUpdatedListener listener;

    @Test
    void shouldHandleShiftUpdatedEvent() {
        // Arrange
        Shift shift = ShiftTestFactory.shift(LocalDate.of(2025, 6, 14), 100);

        ShiftUpdatedEvent event = new ShiftUpdatedEvent(
                shift.getDate(),
                shift.getHourlyActualValues(),
                shift.getHourlyPlanValues(),
                List.of("08:00", "09:00", "10:00")
        );

        // Act
        listener.handle(event);

        // Assert
        verify(shiftPort).saveOrReplace(any(Shift.class));
    }
}

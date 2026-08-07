package com.exempal.shiftcounter.features.shift.adapter.event;

import com.exempal.shiftcounter.features.shift.domain.Shift;
import com.exempal.shiftcounter.features.shift.domain.ShiftTestFactory;
import com.exempal.shiftcounter.features.shift.domain.ShiftUpdatedEvent;
import com.exempal.shiftcounter.features.shift.projection.ShiftProjectionUseCase;
import com.exempal.shiftcounter.features.shift.projection.ShiftView;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShiftUpdatedListenerTest {

    @Mock
    private SimpMessagingTemplate messaging;

    @Mock
    private ShiftProjectionUseCase projection;

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
        ShiftView view = new ShiftView(event.date(), event.actual(), event.plan(), event.hours());
        when(projection.buildView(event.date())).thenReturn(view);

        // Act
        listener.handle(event);

        // Assert
        verify(messaging).convertAndSend("/topic/shift-updates", view);
    }
}

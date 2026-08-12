package com.exempal.shiftcounter.features.shift.adapter.event;

import com.exempal.shiftcounter.features.shift.domain.Shift;
import com.exempal.shiftcounter.features.shift.domain.ShiftTestFactory;
import com.exempal.shiftcounter.features.shift.domain.ShiftUpdatedEvent;
import com.exempal.shiftcounter.features.shift.application.projection.ShiftProjectionUseCase;
import com.exempal.shiftcounter.features.shift.application.projection.ShiftView;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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
        when(projection.buildView(event.date(), event.sensorId())).thenReturn(view);
        ShiftView sensorFive = new ShiftView(event.date(), "sensor-5", event.actual(), event.plan(),
                event.hours(), List.of(true, true, true));
        when(projection.buildView(event.date(), "sensor-5")).thenReturn(sensorFive);

        // Act
        listener.handle(event);

        // Assert
        verify(messaging).convertAndSend("/topic/shift-updates", view);
        verify(messaging).convertAndSend("/topic/shift-updates/sensor-1", view);
        verify(messaging).convertAndSend("/topic/shift-updates/sensor-5", sensorFive);
    }

    @ParameterizedTest
    @ValueSource(strings = {"sensor-2", "sensor-3", "sensor-4", "sensor-5", "sensor-6"})
    void publishesEveryOtherSensorOnlyToItsScopedTopic(String sensorId) {
        LocalDate date = LocalDate.of(2026, 8, 7);
        ShiftUpdatedEvent event = new ShiftUpdatedEvent(date, sensorId, List.of(1), List.of(2), List.of("07:00"));
        ShiftView view = new ShiftView(date, sensorId, List.of(1), List.of(2), List.of("07:00"), List.of(true));
        when(projection.buildView(date, sensorId)).thenReturn(view);
        ShiftView sensorFive = new ShiftView(date, "sensor-5", List.of(5), List.of(8),
                List.of("07:00"), List.of(true));
        if (List.of("sensor-2", "sensor-3", "sensor-4").contains(sensorId)) {
            when(projection.buildView(date, "sensor-5")).thenReturn(sensorFive);
        }

        listener.handle(event);

        verify(messaging).convertAndSend("/topic/shift-updates/" + sensorId, view);
        verify(messaging, never()).convertAndSend("/topic/shift-updates", view);
        if (List.of("sensor-2", "sensor-3", "sensor-4").contains(sensorId)) {
            verify(messaging).convertAndSend("/topic/shift-updates/sensor-5", sensorFive);
        }
    }
}

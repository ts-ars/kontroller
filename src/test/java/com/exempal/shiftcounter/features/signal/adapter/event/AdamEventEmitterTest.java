package com.exempal.shiftcounter.features.signal.adapter.event;

import com.exempal.shiftcounter.features.signal.adapter.adam.AdamModbusAdapter;
import com.exempal.shiftcounter.features.signal.adapter.adam.AdamProperties;
import com.exempal.shiftcounter.features.signal.application.CounterInputPort;
import com.exempal.shiftcounter.features.signal.domain.CounterProcessingResult;
import com.exempal.shiftcounter.features.signal.domain.CounterProcessingStatus;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.*;

class AdamEventEmitterTest {
    @Test
    void everyPollPassesTheAbsoluteCounterToTheDeltaUseCase() {
        AdamModbusAdapter modbus = mock(AdamModbusAdapter.class);
        CounterInputPort counters = mock(CounterInputPort.class);
        AdamProperties.Device device = new AdamProperties.Device("sensor-1", "127.0.0.1", 502, 1, 0);
        AdamProperties properties = mock(AdamProperties.class);
        when(properties.enabled()).thenReturn(true);
        when(properties.devices()).thenReturn(List.of(device));
        when(modbus.readCounter(device)).thenReturn(100L, 100L, 103L);
        when(counters.process(any())).thenReturn(new CounterProcessingResult(
                CounterProcessingStatus.BASELINE_ESTABLISHED, 0, 0, LocalDate.of(2026, 8, 10)));
        AdamEventEmitter emitter = new AdamEventEmitter(modbus, counters, Clock.systemUTC(), properties);

        emitter.pollAdam();
        emitter.pollAdam();
        emitter.pollAdam();

        verify(counters, times(3)).process(any());
    }
}

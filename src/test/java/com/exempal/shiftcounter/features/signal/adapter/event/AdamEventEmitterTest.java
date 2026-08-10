package com.exempal.shiftcounter.features.signal.adapter.event;

import com.exempal.shiftcounter.features.signal.adapter.adam.AdamModbusAdapter;
import com.exempal.shiftcounter.features.signal.domain.CounterInputPort;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class AdamEventEmitterTest {
    @Test
    void everyPollPassesTheAbsoluteCounterToTheDeltaUseCase() {
        AdamModbusAdapter modbus = mock(AdamModbusAdapter.class);
        CounterInputPort counters = mock(CounterInputPort.class);
        when(modbus.readCounter(0)).thenReturn(100L, 100L, 103L);
        AdamEventEmitter emitter = new AdamEventEmitter(modbus, counters);

        emitter.pollAdam();
        emitter.pollAdam();
        emitter.pollAdam();

        verify(counters, times(3)).process(any());
    }
}

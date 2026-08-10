package com.exempal.shiftcounter.features.signal.adapter.event;

import com.exempal.shiftcounter.features.signal.adapter.adam.AdamModbusAdapter;
import com.exempal.shiftcounter.features.signal.domain.SignalInputPort;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class AdamEventEmitterTest {
    @Test
    void risingEdgeUsesPersistingSignalInputPortOnce() {
        AdamModbusAdapter modbus = mock(AdamModbusAdapter.class);
        SignalInputPort signals = mock(SignalInputPort.class);
        when(modbus.readDigitalInput(0)).thenReturn(true, true, false, true);
        AdamEventEmitter emitter = new AdamEventEmitter(modbus, signals);

        emitter.pollAdam();
        emitter.pollAdam();
        emitter.pollAdam();
        emitter.pollAdam();

        verify(signals, times(2)).register(any());
    }
}

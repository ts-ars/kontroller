package com.exempal.shiftcounter.features.signal.adapter.adam;

import com.serotonin.modbus4j.ModbusFactory;
import com.serotonin.modbus4j.ModbusMaster;
import com.serotonin.modbus4j.msg.ReadHoldingRegistersRequest;
import com.serotonin.modbus4j.msg.ReadHoldingRegistersResponse;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class AdamModbusAdapterTest {
    @Test
    void malformedRegisterResponseDisconnectsAndNextReadReconnects() throws Exception {
        ModbusFactory factory = mock(ModbusFactory.class);
        ModbusMaster first = mock(ModbusMaster.class);
        ModbusMaster second = mock(ModbusMaster.class);
        ReadHoldingRegistersResponse malformed = mock(ReadHoldingRegistersResponse.class);
        ReadHoldingRegistersResponse valid = mock(ReadHoldingRegistersResponse.class);
        when(factory.createTcpMaster(any(), eq(true))).thenReturn(first, second);
        when(first.send(any(ReadHoldingRegistersRequest.class))).thenReturn(malformed);
        when(second.send(any(ReadHoldingRegistersRequest.class))).thenReturn(valid);
        when(malformed.getShortData()).thenReturn(new short[]{1});
        when(valid.getShortData()).thenReturn(new short[]{2, 1});

        AdamProperties properties = properties();
        AdamProperties.Device device = properties.devices().get(0);
        AdamModbusAdapter adapter = new AdamModbusAdapter(properties, factory);

        assertThatThrownBy(() -> adapter.readCounter(device)).hasMessageContaining("returned 1 registers");
        assertThat(adapter.connectionStates().get(device.sensorId())).isFalse();
        assertThat(adapter.readCounter(device)).isEqualTo(65_538L);
        verify(first).destroy();
        verify(factory, times(2)).createTcpMaster(any(), eq(true));
    }

    private static AdamProperties properties() {
        List<AdamProperties.Device> devices = IntStream.rangeClosed(1, 6)
                .mapToObj(number -> new AdamProperties.Device("sensor-" + number, "192.0.2." + number,
                        502, 1, 0)).toList();
        return new AdamProperties(true, Duration.ofMillis(100), Duration.ofSeconds(2), 2, devices);
    }
}

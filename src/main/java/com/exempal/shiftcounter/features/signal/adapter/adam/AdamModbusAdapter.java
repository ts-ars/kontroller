package com.exempal.shiftcounter.features.signal.adapter.adam;

import com.serotonin.modbus4j.ModbusFactory;
import com.serotonin.modbus4j.ModbusMaster;
import com.serotonin.modbus4j.exception.ModbusInitException;
import com.serotonin.modbus4j.exception.ModbusTransportException;
import com.serotonin.modbus4j.ip.IpParameters;
import com.serotonin.modbus4j.msg.ReadHoldingRegistersRequest;
import com.serotonin.modbus4j.msg.ReadHoldingRegistersResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import jakarta.annotation.PreDestroy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@Profile("prod")
public class AdamModbusAdapter {
    private final ModbusFactory factory;
    private final AdamProperties properties;
    private final Map<String, ModbusMaster> masters = new ConcurrentHashMap<>();
    private final Map<String, Boolean> connected = new ConcurrentHashMap<>();

    public AdamModbusAdapter(AdamProperties properties) {
        this(properties, new ModbusFactory());
    }

    AdamModbusAdapter(AdamProperties properties, ModbusFactory factory) {
        this.properties = properties;
        this.factory = factory;
        properties.devices().forEach(device -> connected.put(device.sensorId(), false));
    }

    private ModbusMaster connect(AdamProperties.Device device) {
        try {
            IpParameters params = new IpParameters();
            params.setHost(device.host());
            params.setPort(device.port());
            params.setEncapsulated(false);
            ModbusMaster master = factory.createTcpMaster(params, true);
            master.setTimeout(Math.toIntExact(properties.timeout().toMillis()));
            master.setRetries(properties.retries());
            master.init();
            connected.put(device.sensorId(), true);
            masters.put(device.sensorId(), master);
            log.info("adamState=connected sensor={} host={} port={}", device.sensorId(), device.host(), device.port());
            return master;
        } catch (ModbusInitException e) {
            connected.put(device.sensorId(), false);
            log.warn("adamState=unavailable sensor={} result=connect-failed", device.sensorId());
            throw new IllegalStateException("ADAM connection failed for " + device.sensorId(), e);
        }
    }

    public long readCounter(AdamProperties.Device device) {
        ModbusMaster master = masters.get(device.sensorId());
        if (master == null || !connected.getOrDefault(device.sensorId(), false)) master = connect(device);
        try {
            int address = device.counterChannel() * 2;
            ReadHoldingRegistersRequest request = new ReadHoldingRegistersRequest(device.slaveId(), address, 2);
            ReadHoldingRegistersResponse response = (ReadHoldingRegistersResponse) master.send(request);
            if (response == null || response.isException()) {
                throw new IllegalStateException("Counter read failed for sensor " + device.sensorId() + ": "
                        + (response != null ? response.getExceptionMessage() : "null response"));
            }
            short[] data = response.getShortData();
            if (data == null || data.length != 2) {
                throw new IllegalStateException("Counter read returned "
                        + (data == null ? "null" : data.length) + " registers for sensor " + device.sensorId());
            }
            long lowWord = Short.toUnsignedLong(data[0]);
            long highWord = Short.toUnsignedLong(data[1]);
            return highWord * 65_536L + lowWord;
        } catch (ModbusTransportException e) {
            disconnect(device.sensorId());
            log.warn("adamState=disconnected sensor={} result=transport-failure", device.sensorId());
            throw new IllegalStateException("ADAM read failed for " + device.sensorId(), e);
        } catch (RuntimeException e) {
            disconnect(device.sensorId());
            log.warn("adamState=disconnected sensor={} result=invalid-response", device.sensorId());
            throw e;
        }
    }

    public Map<String, Boolean> connectionStates() {
        return Map.copyOf(connected);
    }

    private void disconnect(String sensorId) {
        ModbusMaster master = masters.remove(sensorId);
        if (master != null) master.destroy();
        connected.put(sensorId, false);
    }

    @PreDestroy
    public void shutdown() {
        properties.devices().forEach(device -> disconnect(device.sensorId()));
        log.info("adamState=closed result=graceful-shutdown");
    }
}

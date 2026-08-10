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

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
@Profile("prod")
public class AdamModbusAdapter {

    private static final String IP = "192.168.0.100";
    private static final int PORT = 502;
    private static final int SLAVE_ID = 1;

    private final ModbusFactory factory = new ModbusFactory();
    private ModbusMaster master;
    private final AtomicBoolean connected = new AtomicBoolean(false);

    @PostConstruct
    public void init() {
        try {
            IpParameters params = new IpParameters();
            params.setHost(IP);
            params.setPort(PORT);
            params.setEncapsulated(false);

            master = factory.createTcpMaster(params, true);
            master.init();
            connected.set(true);
            log.info("[MODBUS] Connected to ADAM at {}:{}", IP, PORT);
        } catch (ModbusInitException e) {
            log.error("[MODBUS] Initialization failed", e);
            connected.set(false);
        }
    }

    public long readCounter(int channel) {
        if (!connected.get()) {
            throw new IllegalStateException("Modbus not connected");
        }

        try {
            int address = channel * 2;
            ReadHoldingRegistersRequest request = new ReadHoldingRegistersRequest(SLAVE_ID, address, 2);
            ReadHoldingRegistersResponse response = (ReadHoldingRegistersResponse) master.send(request);
            if (response == null || response.isException()) {
                throw new IllegalStateException("Counter read failed for channel " + channel + ": "
                        + (response != null ? response.getExceptionMessage() : "null response"));
            }
            short[] data = response.getShortData();
            long lowWord = Short.toUnsignedLong(data[0]);
            long highWord = Short.toUnsignedLong(data[1]);
            return highWord * 65_536L + lowWord;
        } catch (ModbusTransportException e) {
            log.error("[MODBUS] Transport exception while reading counter channel {}", channel, e);
            connected.set(false);
            throw new RuntimeException("Modbus read failed", e);
        }
    }

    public boolean isConnected() {
        return connected.get();
    }

    @PreDestroy
    public void shutdown() {
        if (master != null) {
            master.destroy();
            connected.set(false);
            log.info("[MODBUS] Connection closed");
        }
    }
}

package com.exempal.shiftcounter.features.signal.adapter.adam;

import com.serotonin.modbus4j.ModbusFactory;
import com.serotonin.modbus4j.ModbusMaster;
import com.serotonin.modbus4j.exception.ModbusInitException;
import com.serotonin.modbus4j.exception.ModbusTransportException;
import com.serotonin.modbus4j.ip.IpParameters;
import com.serotonin.modbus4j.msg.ReadDiscreteInputsRequest;
import com.serotonin.modbus4j.msg.ReadDiscreteInputsResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Profile;

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

    public boolean readDigitalInput(int address) {
        if (!connected.get()) {
            throw new IllegalStateException("Modbus not connected");
        }

        try {
            ReadDiscreteInputsRequest request = new ReadDiscreteInputsRequest(SLAVE_ID, address, 1);
            ReadDiscreteInputsResponse response = (ReadDiscreteInputsResponse) master.send(request);
            if (response == null || response.isException()) {
                log.warn("[MODBUS] Failed to read DI[{}]: {}", address, response != null ? response.getExceptionMessage() : "null response");
                return false;
            }
            return response.getBooleanData()[0];
        } catch (ModbusTransportException e) {
            log.error("[MODBUS] Transport exception while reading DI[{}]", address, e);
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

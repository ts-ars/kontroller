package com.exempal.shiftcounter.features.signal.adapter.adam;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component("adam")
@Profile("prod")
public final class AdamHealthIndicator implements HealthIndicator {
    private final AdamProperties properties;
    private final AdamModbusAdapter adapter;

    public AdamHealthIndicator(AdamProperties properties, AdamModbusAdapter adapter) {
        this.properties = properties;
        this.adapter = adapter;
    }

    @Override
    public Health health() {
        if (!properties.enabled()) return Health.outOfService().withDetail("enabled", false).build();
        Map<String, Boolean> states = adapter.connectionStates();
        long connected = states.values().stream().filter(Boolean::booleanValue).count();
        Health.Builder status = connected == states.size() ? Health.up() : Health.down();
        return status.withDetail("connectedSensors", connected)
                .withDetail("configuredSensors", states.size()).withDetail("sensors", states).build();
    }
}

package com.exempal.shiftcounter.features.signal.adapter.adam;

import com.exempal.shiftcounter.features.sensor.domain.SensorCatalog;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Validated
@Profile("prod")
@ConfigurationProperties("adam")
public record AdamProperties(boolean enabled, Duration pollDelay, Duration timeout, @Min(0) int retries,
                             @NotEmpty List<@Valid Device> devices) {

    public AdamProperties {
        if (!enabled) {
            throw new IllegalArgumentException("adam.enabled must be true in production");
        }
        if (pollDelay == null || pollDelay.isNegative() || pollDelay.isZero()) {
            throw new IllegalArgumentException("adam.poll-delay must be positive");
        }
        if (timeout == null || timeout.isNegative() || timeout.isZero()) {
            throw new IllegalArgumentException("adam.timeout must be positive");
        }
        if (devices != null) {
            Set<String> configured = devices.stream().map(Device::sensorId).collect(Collectors.toSet());
            Set<String> expected = SensorCatalog.all().stream().map(sensor -> sensor.id().value())
                    .collect(Collectors.toSet());
            if (configured.size() != devices.size() || !configured.equals(expected)) {
                throw new IllegalArgumentException("ADAM devices must configure sensor-1 through sensor-6 exactly once");
            }
        }
    }

    public record Device(@NotBlank String sensorId, @NotBlank String host, @Min(1) int port,
                         @Min(1) int slaveId, @Min(0) int counterChannel) {}
}

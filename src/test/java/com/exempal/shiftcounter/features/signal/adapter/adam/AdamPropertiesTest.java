package com.exempal.shiftcounter.features.signal.adapter.adam;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AdamPropertiesTest {
    @Test
    void requiresAllSixStableSensorsExactlyOnce() {
        assertThatCode(() -> properties(devices(1, 6))).doesNotThrowAnyException();
        assertThatThrownBy(() -> properties(devices(1, 5)))
                .hasMessageContaining("sensor-1 through sensor-6");
        List<AdamProperties.Device> duplicate = devices(1, 5);
        duplicate.add(device(5));
        assertThatThrownBy(() -> properties(duplicate)).hasMessageContaining("exactly once");
    }

    @Test
    void requiresPositivePollingAndTimeoutValues() {
        assertThatThrownBy(() -> new AdamProperties(true, Duration.ZERO, Duration.ofSeconds(1), 1,
                devices(1, 6))).hasMessageContaining("poll-delay");
        assertThatThrownBy(() -> new AdamProperties(true, Duration.ofMillis(100), Duration.ZERO, 1,
                devices(1, 6))).hasMessageContaining("timeout");
    }

    @Test
    void rejectsDisabledAdamInProduction() {
        assertThatThrownBy(() -> new AdamProperties(false, Duration.ofMillis(100), Duration.ofSeconds(2), 2,
                devices(1, 6))).hasMessageContaining("adam.enabled must be true");
    }

    private static AdamProperties properties(List<AdamProperties.Device> devices) {
        return new AdamProperties(true, Duration.ofMillis(100), Duration.ofSeconds(2), 2, devices);
    }

    private static List<AdamProperties.Device> devices(int start, int end) {
        return new java.util.ArrayList<>(IntStream.rangeClosed(start, end).mapToObj(AdamPropertiesTest::device).toList());
    }

    private static AdamProperties.Device device(int number) {
        return new AdamProperties.Device("sensor-" + number, "192.0.2." + number, 502, 1, 0);
    }
}

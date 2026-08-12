package com.exempal.shiftcounter.features.settings.adapter.settings;

import com.exempal.shiftcounter.features.settings.application.SettingsRepository;
import com.exempal.shiftcounter.features.settings.domain.IntervalSetting;
import com.exempal.shiftcounter.features.settings.domain.SettingsGroup;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShiftSettingsProviderTest {
    @Test
    void resolvesSharedDerivedAndIndependentPlansWithoutCouplingActualOrSignals() {
        SettingsRepository repository = mock(SettingsRepository.class);
        when(repository.findById("settings-group-1")).thenReturn(group("settings-group-1", 100));
        when(repository.findById("settings-group-2")).thenReturn(group("settings-group-2", 300));
        ShiftSettingsProvider provider = new ShiftSettingsProvider(repository);

        assertThat(provider.getForSensor("sensor-1").plans()).containsExactly(100);
        assertThat(provider.getForSensor("sensor-4").plans()).containsExactly(100);
        assertThat(provider.getForSensor("sensor-5").plans()).containsExactly(400);
        assertThat(provider.getForSensor("sensor-6").plans()).containsExactly(300);
    }

    private SettingsGroup group(String id, int plan) {
        return new SettingsGroup(id, id, true,
                List.of(new IntervalSetting(LocalTime.of(7, 0), plan, 0)));
    }
}

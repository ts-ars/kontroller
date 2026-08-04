package com.exempal.shiftcounter.features.settings.adapter.jpa;

import com.exempal.shiftcounter.features.settings.domain.SettingsPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(locations = "classpath:application.yml")
public class JpaSettingsAdapterTest {

    @Autowired
    private SettingRepository settingRepository;

    private SettingsPort settingsPort;

    @BeforeEach
    void setUp() {
        settingsPort = new JpaSettingsAdapter(settingRepository);

        settingsPort.updateHours(List.of("08:00", "09:00", "10:00"));
        settingsPort.updateHourlyPlans(List.of("100", "150", "200"));
    }

    @Test
    void shouldReadHoursFromDb() {
        assertThat(settingsPort.getHours()).containsExactly("08:00", "09:00", "10:00");
    }

    @Test
    void shouldReadHourlyPlansFromDb() {
        assertThat(settingsPort.getHourlyPlans()).containsExactly("100", "150", "200");
    }

    @Test
    void shouldUpdateHourlyPlans() {
        settingsPort.updateHourlyPlans(List.of("123", "456"));
        assertThat(settingsPort.getHourlyPlans()).containsExactly("123", "456");
    }

    @Test
    void shouldThrowIfKeyMissing() {
        // удалим ключ вручную
        settingRepository.deleteById("hours");

        assertThatThrownBy(() -> settingsPort.getHours())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Missing setting: hours");
    }

    @Test
    void shouldUpdateRawKeyValuePair() {
        settingsPort.update("customKey", "simpleValue");
        var optional = settingRepository.findById("customKey");

        assertThat(optional).isPresent();
        assertThat(optional.get().getValue()).isEqualTo("simpleValue");
    }
}

package com.exempal.shiftcounter.features.settings.adapter.jpa;

import com.exempal.shiftcounter.features.settings.domain.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;
import java.time.LocalTime;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(locations = "classpath:application.yml")
class JpaSettingsGroupAdapterTest {
    @Autowired SettingsGroupJpaRepository groups;
    @Autowired IntervalSettingJpaRepository intervals;

    @Test
    void storesTimeAndPlanTogetherAndKeepsGroupsIndependent() {
        SettingsRepository repository = new JpaSettingsGroupAdapter(groups, intervals);
        repository.save(new SettingsGroup("settings-group-1", "one", true,
                List.of(new IntervalSetting(LocalTime.of(10, 0), 123, 0))));

        assertThat(repository.findById("settings-group-1").intervals())
                .containsExactly(new IntervalSetting(LocalTime.of(10, 0), 123, 0));
        assertThat(repository.findById("settings-group-2").intervals()).isNotEmpty();
    }
}

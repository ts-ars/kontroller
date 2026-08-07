package com.exempal.shiftcounter.features.settings.infrastructure;

import com.exempal.shiftcounter.features.settings.adapter.jpa.SettingEntity;
import com.exempal.shiftcounter.features.settings.adapter.jpa.SettingRepository;
import com.exempal.shiftcounter.features.shift.application.ShiftInitializerService;
import com.exempal.shiftcounter.features.shift.infrastructure.JpaShiftAdapter;
import com.exempal.shiftcounter.features.shift.infrastructure.ShiftEntity;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@org.springframework.test.context.ActiveProfiles("test")
@org.junit.jupiter.api.Tag("integration")
class ShiftSettingsApplierIntegrationTest {

    @Autowired
    private SettingRepository settingRepository;

    @Autowired
    private ShiftInitializerService shiftInitializer;

    @Autowired
    private ShiftSettingsApplier shiftSettingsApplier;

    @Autowired
    private ShiftSettingsProvider shiftSettingsProvider;

    @Autowired
    private JpaShiftAdapter shiftAdapter;

    @Test
    void whenNewHourAddedToSettings_thenApplySettingsExtendsCurrentShift() {

        settingRepository.deleteAll();

        // Given: Сохраняем начальные настройки как JSON
        settingRepository.save(new SettingEntity("hours", "[\"08:00\",\"09:00\"]"));
        settingRepository.save(new SettingEntity("hourlyPlans", "[100,200]"));
        shiftSettingsProvider.reload();

        LocalDate today = LocalDate.now();
        shiftInitializer.createNewShift(today);

        ShiftEntity original = shiftAdapter.findEntityByDate(today).orElseThrow();
        assertThat(original.getHourlyLabels()).containsExactly("08:00", "09:00");
        assertThat(original.getHourlyPlanValues()).containsExactly(100, 200);

        // When: добавим новый час и применим
        settingRepository.save(new SettingEntity("hours", "[\"08:00\",\"09:00\",\"10:00\"]"));
        settingRepository.save(new SettingEntity("hourlyPlans", "[100,200,300]"));
        shiftSettingsApplier.applySettingsToCurrentShift();

        // Then: смена обновлена
        ShiftEntity updated = shiftAdapter.findEntityByDate(today).orElseThrow();
        assertThat(updated.getHourlyLabels()).containsExactly("08:00", "09:00", "10:00");
        assertThat(updated.getHourlyPlanValues()).containsExactly(100, 200, 300);
    }
}

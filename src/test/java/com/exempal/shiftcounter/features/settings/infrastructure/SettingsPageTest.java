package com.exempal.shiftcounter.features.settings.infrastructure;

import com.exempal.shiftcounter.features.settings.application.SettingsGroupService;
import com.exempal.shiftcounter.features.settings.application.UpdateSettingsGroupCommand;
import com.exempal.shiftcounter.features.settings.domain.IntervalSetting;
import com.exempal.shiftcounter.features.settings.domain.SettingsGroup;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class SettingsPageTest {
    @Test
    void selectsAndUpdatesOneGroup() {
        SettingsGroupService service = mock(SettingsGroupService.class);
        var group = new SettingsGroup("settings-group-2", "Sensors 5-6", true, List.of(
                new IntervalSetting(LocalTime.of(8, 0), 100, 0),
                new IntervalSetting(LocalTime.of(9, 0), 200, 1)));
        when(service.get("settings-group-2")).thenReturn(group);
        SettingsPage page = new SettingsPage(service);
        var model = new ExtendedModelMap();

        page.populateModel(model, Map.of("groupId", "settings-group-2"));
        assertThat(model.get("hours")).isEqualTo(List.of("08:00", "09:00"));
        assertThat(model.get("plans")).isEqualTo(List.of(100, 200));

        assertThat(page.updateSettings("settings-group-2", List.of("10:00"), List.of(300)))
                .isEqualTo("redirect:/page/settings?groupId=settings-group-2");
        verify(service).update(new UpdateSettingsGroupCommand("settings-group-2", "Sensors 5-6", true,
                List.of("10:00"), List.of(300)));
    }
}

package com.exempal.shiftcounter.features.settings.adapter.settings;

import com.exempal.shiftcounter.features.settings.adapter.web.SettingsPage;
import com.exempal.shiftcounter.features.settings.application.SettingsGroupService;
import com.exempal.shiftcounter.features.settings.application.UpdateSettingsCommand;
import com.exempal.shiftcounter.features.settings.domain.SettingsRow;
import com.exempal.shiftcounter.features.settings.domain.SettingsSnapshot;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SettingsPageTest {
    @Test
    void rendersAndUpdatesOneCompositeSnapshotWithoutGroupTabsOrPpm() throws Exception {
        SettingsGroupService service = mock(SettingsGroupService.class);
        SettingsSnapshot snapshot = new SettingsSnapshot(List.of(
                new SettingsRow(LocalTime.of(7, 0), 100, 300),
                new SettingsRow(LocalTime.of(8, 0), 200, 400)));
        when(service.getSnapshot("settings-group-1")).thenReturn(snapshot);
        SettingsPage page = new SettingsPage(service);
        var model = new ExtendedModelMap();

        page.populateModel(model, Map.of());
        assertThat(model.get("rows")).isEqualTo(snapshot.rows());
        assertThat(model.get("sharedTotal")).isEqualTo(300);
        assertThat(model.get("sensor5Total")).isEqualTo(1200);
        assertThat(model.get("sensor6Total")).isEqualTo(700);

        assertThat(page.updateSettings("settings-group-1", List.of("07:00", "08:00"),
                List.of(100, 200), List.of(300, 400)))
                .isEqualTo("redirect:/page/settings?groupId=settings-group-1");
        verify(service).update(new UpdateSettingsCommand("settings-group-1",
                List.of("07:00", "08:00"), List.of(100, 200), List.of(300, 400)));

        String template = Files.readString(Path.of("src/main/resources/templates/features/settings/settings.html"));
        assertThat(template).contains("Plan Sensors 1–4", "Sensor 5", "Plan Sensor 6",
                "data-total=\"shared\"", "class=\"add-hour\"", "class=\"delete-row\"");
        assertThat(template).doesNotContain("PPM", "group tabs", "th:each=\"group");
    }
}

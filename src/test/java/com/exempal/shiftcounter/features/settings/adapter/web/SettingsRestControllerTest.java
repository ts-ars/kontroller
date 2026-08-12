package com.exempal.shiftcounter.features.settings.adapter.web;

import com.exempal.shiftcounter.features.settings.application.SettingsGroupService;
import com.exempal.shiftcounter.features.settings.application.UpdateSettingsCommand;
import com.exempal.shiftcounter.features.settings.domain.SettingsRow;
import com.exempal.shiftcounter.features.settings.domain.SettingsSnapshot;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalTime;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({SettingsRestController.class, SettingsExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = false)
class SettingsRestControllerTest {
    @Autowired MockMvc mvc;
    @MockBean SettingsGroupService service;

    @Test
    void routeReturnsOneDerivedSnapshot() throws Exception {
        when(service.getSnapshot("settings-group-1")).thenReturn(snapshot());

        mvc.perform(get("/api/settings/settings-group-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hours[0]").value("07:00"))
                .andExpect(jsonPath("$.sensors1To4Plans[0]").value(100))
                .andExpect(jsonPath("$.sensor5Plans[0]").value(400))
                .andExpect(jsonPath("$.sensor6Plans[0]").value(300))
                .andExpect(jsonPath("$.sensors1To4Total").value(300))
                .andExpect(jsonPath("$.sensor5Total").value(1200))
                .andExpect(jsonPath("$.sensor6Total").value(700));
    }

    @Test
    void routeAcceptsOnlyEditablePlansAndReturnsBackendDerivedSensorFive() throws Exception {
        UpdateSettingsCommand command = new UpdateSettingsCommand("settings-group-1",
                List.of("07:00", "08:00"), List.of(100, 200), List.of(300, 400));
        when(service.update(command)).thenReturn(snapshot());
        String body = "{\"hours\":[\"07:00\",\"08:00\"],"
                + "\"sensors1To4Plans\":[100,200],\"sensor6Plans\":[300,400]}";

        mvc.perform(post("/api/settings/settings-group-1")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sensor5Plans[1]").value(800));
        verify(service).update(command);
    }

    @Test
    void rejectsAnIncompleteSnapshotBeforeCallingApplicationService() throws Exception {
        String body = "{\"hours\":[\"07:00\"],\"sensors1To4Plans\":[],\"sensor6Plans\":[300]}";

        mvc.perform(post("/api/settings/settings-group-1")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }

    private SettingsSnapshot snapshot() {
        return new SettingsSnapshot(List.of(new SettingsRow(LocalTime.of(7, 0), 100, 300),
                new SettingsRow(LocalTime.of(8, 0), 200, 400)));
    }
}

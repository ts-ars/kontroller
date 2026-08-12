package com.exempal.shiftcounter.features.settings.adapter.web;

import com.exempal.shiftcounter.core.ProductionSecurityConfiguration;
import com.exempal.shiftcounter.features.settings.application.SettingsGroupService;
import com.exempal.shiftcounter.features.settings.application.UpdateSettingsCommand;
import com.exempal.shiftcounter.features.settings.domain.SettingsRow;
import com.exempal.shiftcounter.features.settings.domain.SettingsSnapshot;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({SettingsRestController.class, SettingsExceptionHandler.class})
@Import(ProductionSecurityConfiguration.class)
@ActiveProfiles("prod")
@TestPropertySource(properties = {
        "security.operator.username=operator", "security.operator.password=operator-secret",
        "security.admin.username=admin", "security.admin.password=admin-secret"
})
class SettingsProductionCsrfTest {
    @Autowired MockMvc mvc;
    @MockBean SettingsGroupService service;

    @Test
    void productionSettingsMutationRequiresAdminAndCsrf() throws Exception {
        when(service.update(any(UpdateSettingsCommand.class))).thenReturn(new SettingsSnapshot(List.of(
                new SettingsRow(LocalTime.of(7, 0), 100, 300))));
        String body = "{\"hours\":[\"07:00\"],\"sensors1To4Plans\":[100],\"sensor6Plans\":[300]}";

        mvc.perform(post("/api/settings/settings-group-1").with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/settings/settings-group-1").with(user("operator").roles("OPERATOR")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/settings/settings-group-1").with(user("admin").roles("ADMIN")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk());
    }
}

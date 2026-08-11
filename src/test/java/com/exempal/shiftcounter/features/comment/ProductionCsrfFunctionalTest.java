package com.exempal.shiftcounter.features.comment;

import com.exempal.shiftcounter.core.ProductionSecurityConfiguration;
import com.exempal.shiftcounter.features.comment.adapter.web.LossExplanationController;
import com.exempal.shiftcounter.features.comment.application.LossExplanationUseCase;
import com.exempal.shiftcounter.features.comment.domain.LossCategory;
import com.exempal.shiftcounter.features.comment.domain.LossExplanation;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LossExplanationController.class)
@Import(ProductionSecurityConfiguration.class)
@ActiveProfiles("prod")
@TestPropertySource(properties = {
        "security.operator.username=operator", "security.operator.password=operator-secret",
        "security.admin.username=admin", "security.admin.password=admin-secret"
})
class ProductionCsrfFunctionalTest {
    @Autowired MockMvc mvc;
    @MockBean LossExplanationUseCase useCase;

    @Test
    void productionExplanationMutationRequiresAndAcceptsCsrfToken() throws Exception {
        String body = "{\"category\":\"MATERIAL\",\"comment\":\"Roll change\",\"allocatedMinutes\":4}";
        when(useCase.create(7L, LossCategory.MATERIAL, "Roll change", 4))
                .thenReturn(new LossExplanation(3L, 7L, LossCategory.MATERIAL, "Roll change", 4, 40));

        mvc.perform(post("/api/stoppages/7/explanations").with(user("operator").roles("OPERATOR"))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());

        mvc.perform(post("/api/stoppages/7/explanations").with(user("operator").roles("OPERATOR")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
    }

    @Test
    void productionUpdateAndDeleteRequireAndAcceptCsrfToken() throws Exception {
        String body = "{\"category\":\"QUALITY\",\"comment\":\"Checked\",\"allocatedMinutes\":2}";
        when(useCase.update(7L, 3L, LossCategory.QUALITY, "Checked", 2))
                .thenReturn(new LossExplanation(3L, 7L, LossCategory.QUALITY, "Checked", 2, 20));

        mvc.perform(put("/api/stoppages/7/explanations/3").with(user("operator").roles("OPERATOR"))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());
        mvc.perform(put("/api/stoppages/7/explanations/3").with(user("operator").roles("OPERATOR")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk());

        mvc.perform(delete("/api/stoppages/7/explanations/3").with(user("operator").roles("OPERATOR")))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/api/stoppages/7/explanations/3").with(user("operator").roles("OPERATOR")).with(csrf()))
                .andExpect(status().isNoContent());
    }
}

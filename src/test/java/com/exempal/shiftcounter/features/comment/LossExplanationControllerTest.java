package com.exempal.shiftcounter.features.comment;

import com.exempal.shiftcounter.features.comment.adapter.web.LossExplanationController;
import com.exempal.shiftcounter.features.comment.adapter.web.LossExplanationExceptionHandler;
import com.exempal.shiftcounter.features.comment.application.LossAllocationException;
import com.exempal.shiftcounter.features.comment.application.LossExplanationUseCase;
import com.exempal.shiftcounter.features.comment.domain.LossCategory;
import com.exempal.shiftcounter.features.comment.domain.LossExplanation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class LossExplanationControllerTest {
    private LossExplanationUseCase useCase;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        useCase = mock(LossExplanationUseCase.class);
        mvc = MockMvcBuilders.standaloneSetup(new LossExplanationController(useCase))
                .setControllerAdvice(new LossExplanationExceptionHandler())
                .build();
    }

    @Test
    void createsExplanationAndReturnsBackendCalculatedCans() throws Exception {
        when(useCase.create(7L, LossCategory.MATERIAL, "Roll change", 4))
                .thenReturn(new LossExplanation(3L, 7L, LossCategory.MATERIAL, "Roll change", 4, 40));

        mvc.perform(post("/api/stoppages/7/explanations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"category":"MATERIAL","comment":"Roll change","allocatedMinutes":4,
                                 "allocatedCans":999,"detectionType":"BREAKDOWN"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.allocatedCans").value(40))
                .andExpect(jsonPath("$.stoppageId").value(7));

        verify(useCase).create(7L, LossCategory.MATERIAL, "Roll change", 4);
    }

    @Test
    void reportsAllocationViolationAsBadRequest() throws Exception {
        when(useCase.create(eq(7L), any(), any(), anyInt()))
                .thenThrow(new LossAllocationException("allocated minutes exceed stoppage rounded minutes"));

        mvc.perform(post("/api/stoppages/7/explanations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"category\":\"QUALITY\",\"comment\":\"\",\"allocatedMinutes\":11}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("allocated minutes exceed stoppage rounded minutes"));
    }
}

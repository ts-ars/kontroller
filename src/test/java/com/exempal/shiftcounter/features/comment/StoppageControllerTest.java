package com.exempal.shiftcounter.features.comment;

import com.exempal.shiftcounter.features.comment.adapter.web.StoppageController;
import com.exempal.shiftcounter.features.comment.application.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class StoppageControllerTest {
    private ReconcileStoppagesUseCase reconcile;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        reconcile = mock(ReconcileStoppagesUseCase.class);
        mvc = MockMvcBuilders.standaloneSetup(new StoppageController(
                mock(StoppageRepository.class), reconcile)).build();
    }

    @Test
    void delegatesExplicitCalculationTimeAndReturnsDiagnostics() throws Exception {
        when(reconcile.reconcile(any())).thenReturn(new ReconcileResult(2, List.of(), List.of(
                ReconcileDiagnostic.warning(ReconcileDiagnosticCode.FIXED_EXCEEDS_TOTAL_LOSS,
                        "bounded")), 1, true));

        mvc.perform(post("/api/stoppages/recalculate")
                        .param("date", "2026-08-07")
                        .param("intervalIndex", "2")
                        .param("calculationTime", "2026-08-07T10:30:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.intervalIndex").value(2))
                .andExpect(jsonPath("$.diagnostics[0]").value("FIXED_EXCEEDS_TOTAL_LOSS: bounded"));
        verify(reconcile).reconcile(new ReconcileStoppagesCommand(java.time.LocalDate.of(2026, 8, 7),
                "primary", 2, java.time.LocalDateTime.of(2026, 8, 7, 10, 30)));
    }

    @Test
    void fatalDiagnosticReturnsUnprocessableEntity() throws Exception {
        when(reconcile.reconcile(any())).thenReturn(new ReconcileResult(-1, List.of(), List.of(
                ReconcileDiagnostic.fatal(ReconcileDiagnosticCode.INVALID_INTERVAL, "missing")), 0, false));
        mvc.perform(post("/api/stoppages/recalculate")
                        .param("date", "2026-08-07")
                        .param("calculationTime", "2026-08-07T10:30:00"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.persisted").value(false));
    }
}

package com.exempal.shiftcounter.features.comment;

import com.exempal.shiftcounter.features.comment.application.*;
import com.exempal.shiftcounter.features.comment.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class LossExplanationServiceTest {
    private StoppageRepository stoppages;
    private LossExplanationRepository explanations;
    private LossExplanationService service;
    private StoppageEntry stoppage;

    @BeforeEach
    void setUp() {
        stoppages = mock(StoppageRepository.class);
        explanations = mock(LossExplanationRepository.class);
        service = new LossExplanationService(stoppages, explanations);
        stoppage = mock(StoppageEntry.class);
        when(stoppage.getId()).thenReturn(10L);
        when(stoppage.getType()).thenReturn(StoppageType.FIXED);
        when(stoppage.getMinutes()).thenReturn(10.0);
        when(stoppage.getCans()).thenReturn(100);
        when(stoppages.findById(10L)).thenReturn(Optional.of(stoppage));
        when(explanations.findByStoppageId(10L)).thenReturn(List.of());
        when(explanations.save(any())).thenAnswer(invocation -> {
            LossExplanation value = invocation.getArgument(0);
            return new LossExplanation(value.id() == null ? 1L : value.id(), value.stoppageId(), value.category(),
                    value.comment(), value.allocatedMinutes(), value.allocatedCans());
        });
    }

    @Test
    void createsExplanationWithoutAcceptingSystemOwnedFields() {
        LossExplanation saved = service.create(10L, LossCategory.MATERIAL, "Roll change", 4);

        assertThat(saved.stoppageId()).isEqualTo(10L);
        assertThat(saved.allocatedMinutes()).isEqualTo(4);
        assertThat(saved.allocatedCans()).isEqualTo(40);
    }

    @Test
    void rejectsAggregateOverAllocation() {
        when(explanations.findByStoppageId(10L)).thenReturn(List.of(
                new LossExplanation(2L, 10L, LossCategory.QUALITY, "Existing", 7, 70)));

        assertThatThrownBy(() -> service.create(10L, LossCategory.MATERIAL, "Too much", 4))
                .isInstanceOf(LossAllocationException.class)
                .hasMessageContaining("exceed");
        verify(explanations, never()).save(any());
    }

    @Test
    void rejectsExplanationForLegacyOperatorRow() {
        when(stoppage.getType()).thenReturn(StoppageType.BREAKDOWN);

        assertThatThrownBy(() -> service.create(10L, LossCategory.BREAKDOWN, "Legacy", 1))
                .isInstanceOf(LossAllocationException.class);
    }

    @Test
    void updateCannotMoveExplanationFromAnotherLoss() {
        when(explanations.findById(3L)).thenReturn(Optional.of(
                new LossExplanation(3L, 11L, LossCategory.QUALITY, "Other", 1, 10)));

        assertThatThrownBy(() -> service.update(10L, 3L, LossCategory.MATERIAL, "Move", 1))
                .isInstanceOf(LossExplanationNotFoundException.class);
    }
}

package com.exempal.shiftcounter.invariants;

import com.exempal.shiftcounter.features.comment.calculator.StoppageCalculatorImpl;
import com.exempal.shiftcounter.features.comment.calculator.StoppageFixedLossCalculator;
import com.exempal.shiftcounter.features.comment.calculator.StoppageTempoLossCalculator;
import com.exempal.shiftcounter.features.comment.calculator.StoppageUserOverrideMapper;
import com.exempal.shiftcounter.features.comment.domain.StoppageEntry;
import com.exempal.shiftcounter.features.signal.domain.Signal;
import com.exempal.shiftcounter.features.signal.infrastructure.SignalEntity;
import com.exempal.shiftcounter.features.signal.infrastructure.SignalJpaAdapter;
import com.exempal.shiftcounter.features.signal.infrastructure.SignalJpaRepository;
import com.exempal.shiftcounter.features.shift.application.ShiftTimeHelper;
import com.exempal.shiftcounter.features.shift.domain.Shift;
import com.exempal.shiftcounter.features.shift.domain.ShiftMetrics;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@org.junit.jupiter.api.Tag("unit")
class Stage0InvariantProtectionTest {

    private static final LocalDate SHIFT_DATE = LocalDate.of(2026, 8, 7);

    @Test
    @Disabled("Stage 6: Shift currently accepts an actual total inconsistent with hourly actuals")
    void i1ShiftActualEqualsSumOfHourlyActualValues() {
        Shift shift = new Shift(
                SHIFT_DATE,
                List.of(60, 60),
                999,
                List.of(1, 2),
                List.of("08:00", "09:00")
        );

        assertThat(shift.getActual()).isEqualTo(3);
    }

    @Test
    @Disabled("Stage 4: current-interval TEMPO is still proportional to elapsed time")
    void i2IntervalLossIsNeverNegativeAndUsesFullPlan() {
        StoppageTempoLossCalculator calculator = new StoppageTempoLossCalculator(new ShiftTimeHelper());
        Shift shift = shiftWithPlanAndActual(60, 10);

        Optional<StoppageEntry> tempo = calculator.calculateTempo(
                shift,
                0,
                10,
                0,
                1.0,
                LocalDateTime.of(SHIFT_DATE, LocalTime.of(8, 30))
        );

        assertThat(tempo).isPresent();
        assertThat(tempo.orElseThrow().getCans()).isEqualTo(50);
    }

    @Test
    @Disabled("Stage 4: FIXED is not yet bounded by total loss before TEMPO is derived")
    void i3FixedPlusTempoEqualsIntervalLoss() {
        StoppageTempoLossCalculator calculator = new StoppageTempoLossCalculator(new ShiftTimeHelper());
        Shift shift = shiftWithPlanAndActual(60, 20);
        int fixedLoss = 50;

        int tempoLoss = calculator.calculateTempo(
                        shift,
                        0,
                        20,
                        fixedLoss,
                        1.0,
                        LocalDateTime.of(SHIFT_DATE, LocalTime.of(9, 0))
                )
                .map(StoppageEntry::getCans)
                .orElse(0);

        assertThat(fixedLoss + tempoLoss).isEqualTo(40);
    }

    @Test
    @Disabled("Stages 2-4: Reconcile has no LossExplanation model to preserve operator-owned data")
    void i4RecalculationPreservesOperatorCategoryCommentAndAllocatedMinutes() {
        StoppageFixedLossCalculator fixed = mock(StoppageFixedLossCalculator.class);
        StoppageTempoLossCalculator tempo = mock(StoppageTempoLossCalculator.class);
        when(fixed.calculateFixed(any(), anyInt(), any(), anyDouble())).thenReturn(List.of());
        when(tempo.calculateTempo(any(), anyInt(), anyInt(), anyInt(), anyDouble(), any()))
                .thenReturn(Optional.empty());
        StoppageCalculatorImpl calculator = new StoppageCalculatorImpl(
                fixed,
                tempo,
                mock(StoppageUserOverrideMapper.class)
        );

        StoppageEntry existingOperatorData = new StoppageEntry();
        existingOperatorData.setComment("material missing");
        existingOperatorData.setMinutes(7);

        List<StoppageEntry> recalculated = calculator.recalculate(
                shiftWithPlanAndActual(60, 10),
                0,
                List.of(),
                metrics(60),
                LocalDateTime.of(SHIFT_DATE, LocalTime.of(9, 0))
        );

        assertThat(recalculated)
                .anySatisfy(entry -> {
                    assertThat(entry.getComment()).isEqualTo(existingOperatorData.getComment());
                    assertThat(entry.getMinutes()).isEqualTo(existingOperatorData.getMinutes());
                });
    }

    @Test
    @Disabled("Stage 6: Signal has no source identity, so duplicate physical input is persisted twice")
    void i5OnePhysicalSignalCausesOneActualIncrement() {
        SignalJpaRepository repository = mock(SignalJpaRepository.class);
        SignalJpaAdapter adapter = new SignalJpaAdapter(repository);
        Signal physicalSignal = new Signal(LocalDateTime.of(SHIFT_DATE, LocalTime.of(8, 15)));

        adapter.save(physicalSignal);
        adapter.save(physicalSignal);

        ArgumentCaptor<SignalEntity> saved = ArgumentCaptor.forClass(SignalEntity.class);
        verify(repository).save(saved.capture());
        assertThat(saved.getAllValues()).hasSize(1);
    }

    private static Shift shiftWithPlanAndActual(int plan, int actual) {
        return new Shift(
                SHIFT_DATE,
                List.of(plan),
                actual,
                List.of(actual),
                List.of("08:00")
        );
    }

    private static ShiftMetrics metrics(int plan) {
        return new ShiftMetrics(
                List.of("08:00"),
                List.of(plan),
                List.of(60),
                List.of(1.0)
        );
    }
}

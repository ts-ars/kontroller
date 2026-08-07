package com.exempal.shiftcounter.architecture;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.exempal.shiftcounter.features.shift.domain.Shift;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class Stage0InvariantProtectionTest {
    @Test
    void totalActualEqualsSumOfHourlyActual() {
        Shift shift = new Shift(LocalDate.of(2026, 8, 7), List.of(10, 10), 999,
                List.of(3, 4), List.of("08:00", "09:00"));
        assertEquals(7, shift.getActual());
    }

    @Test @Disabled("Known defect assigned to Stage 4: production loss calculation is not unified")
    void intervalLossIsNeverNegative() { fail("Activate with unified Reconcile"); }

    @Test @Disabled("Known defect assigned to Stage 4: FIXED plus TEMPO balance is not unified")
    void fixedPlusTempoEqualsIntervalLoss() { fail("Activate with unified Reconcile"); }

    @Test @Disabled("Stage 4: Reconcile needs stable loss identity before preservation can be verified end-to-end")
    void reconcilePreservesOperatorExplanation() { fail("Activate after explanation and Reconcile stages"); }

    @Test @Disabled("Known defect assigned to Stages 6–7: physical signal identity is not implemented")
    void duplicatePhysicalSignalIncrementsActualOnce() { fail("Activate after signal identity and transactions"); }
}

package com.exempal.shiftcounter.architecture;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;

class Stage0InvariantProtectionTest {
    @Test @Disabled("Known Stage 0 defect: Shift accepts total actual independent of hourly actual; fix before Stage 2")
    void totalActualEqualsSumOfHourlyActual() { fail("Activate when I1 is enforced"); }

    @Test @Disabled("Known defect assigned to Stage 4: production loss calculation is not unified")
    void intervalLossIsNeverNegative() { fail("Activate with unified Reconcile"); }

    @Test @Disabled("Known defect assigned to Stage 4: FIXED plus TEMPO balance is not unified")
    void fixedPlusTempoEqualsIntervalLoss() { fail("Activate with unified Reconcile"); }

    @Test @Disabled("Known defect assigned to Stages 2–4: operator fields are not protected by the target model")
    void reconcilePreservesOperatorExplanation() { fail("Activate after explanation and Reconcile stages"); }

    @Test @Disabled("Known defect assigned to Stages 6–7: physical signal identity is not implemented")
    void duplicatePhysicalSignalIncrementsActualOnce() { fail("Activate after signal identity and transactions"); }
}

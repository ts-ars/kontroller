# Stage 0 — Business Invariants

Status: **APPROVED**

These invariants constrain every later stage.

## Invariants

```text
I1. shift.actual = sum(shift.hourlyActualValues)

I2. intervalLoss = max(0, intervalPlan - intervalActual)

I3. fixedLoss + tempoLoss = intervalLoss

I4. Recalculation preserves operator category, comment and allocated minutes.

I5. One unique physical signal causes one actual increment.
```

- One accepted signal increments the selected sensor interval actual by one and keeps the shift total equal to the sum of interval actuals.
- When `actual >= plan`, FIXED and TEMPO are zero; negative losses do not exist.
- The current unfinished interval uses its full plan, not a plan proportional to elapsed time.
- Recalculation may change backend-owned detection fields, duration, cans and interval assignment, but must preserve operator-owned explanation data.
- Reprocessing the same unique signal must not create another Signal or another increment.

## Definition of Done

Every invariant is represented by an automated protection test before later production-code changes proceed.


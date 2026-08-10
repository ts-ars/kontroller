# Stage 2 — Comments and Loss Explanations

Status: **APPROVED / IMPLEMENTED**

## Ownership

Backend creates and owns each system loss (`FIXED` or `TEMPO`): identity, interval, detection type, start/end, exact duration, rounded minutes and lost cans. UI must not edit these fields.

The operator creates zero or more explanations for an existing loss and supplies only:

```text
category
comment
allocatedMinutes (whole number)
```

Backend calculates `allocatedCans`. The operator never enters cans. “Add reason” creates another explanation, not another stoppage. FIXED and TEMPO use this same model.

## Implemented stage boundary

- Stage 2 owns the minimum persisted explanation model, explanation CRUD, allocation validation, backend cans calculation, UI and reporting.
- Stable detection identity, loss state, optimistic locking and complete domain/JPA separation remain Stage 3.
- Explanation CRUD preserves operator data. Preservation and relinking when Reconcile changes a system loss remain Stage 4.
- Legacy category rows remain intact and are listed in `legacy_loss_explanation_migration_report`; they are not linked by guesswork.

## Allocation rules

```text
sum(allocatedMinutes) <= stoppage.roundedMinutes
```

- Equality means fully explained.
- A smaller sum leaves unexplained minutes.
- A larger sum is rejected during normal editing.

For the Stage 2 partial-allocation contract:

```text
allocatedCans = round(stoppageLostCans * allocatedMinutes / stoppageRoundedMinutes)
```

The Stage 4 largest-remainder pass replaces independent rounding when exact cross-explanation balancing is implemented.

Exact duration is calculated with `java.time.Duration`. Whole minutes use mathematical half-up rounding: 0–29 seconds down, 30–59 seconds up. Do not calculate time using `double`, `float`, `seconds / 60.0` or floating-point `Math.round`.

## Boundaries

This stage changes the explanation contract only. It does not change FIXED/TEMPO detection, signals, shifts, Reconcile, events or time boundaries.

## Definition of Done

- One system loss accepts any number of explanations.
- Explanations allocate whole minutes; backend derives cans.
- Explanation actions do not create system stoppages.
- Explanation create/update/delete preserves all unrelated operator data; Reconcile preservation is explicitly assigned to Stage 4.
- FIXED and TEMPO behave identically from the operator's perspective.


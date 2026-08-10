# Stage 3 — Stoppage and LossExplanation Model

Status: **APPROVED / IMPLEMENTED**

## Domain model

```text
Stoppage 1 ────── 0..N LossExplanation
```

- `DetectionType`: `FIXED`, `TEMPO` — how the backend detected loss.
- `LossCategory`: `ORGANIZATION`, `BREAKDOWN`, `MATERIAL`, `QUALITY` — why the operator says it occurred.
- `Stoppage` owns detection identity, shift/sensor interval, exact time, rounded minutes, lost cans, state and explanations.
- `LossExplanation` owns category, comment and allocated whole minutes; backend owns allocated cans.

`ExplanationStatus` is derived:

```text
0 allocated                         → UNEXPLAINED
0 < allocated < roundedMinutes      → PARTIALLY_EXPLAINED
allocated = roundedMinutes          → FULLY_EXPLAINED
allocated > roundedMinutes          → ALLOCATION_CONFLICT
```

If Reconcile reduces a loss below already allocated operator minutes, the system preserves those minutes and exposes `ALLOCATION_CONFLICT`. If a system loss disappears, it becomes `RESOLVED`; it is not physically deleted with its explanations.

## Identity and persistence

- System losses have a stable UUID `detectionKey`.
- PostgreSQL uses separate `stoppages` and `loss_explanations` tables with a foreign key, constraints, unique detection key and optimistic versions.
- Domain classes do not depend on JPA, `JpaRepository` or persistence entities.
- Repository is an application/domain port; persistence mapping lives in the adapter.
- Schema evolution uses a new Flyway version; applied migrations are not edited.
- Legacy migration must be rehearsed on a database copy and report records that cannot be linked unambiguously.

## Definition of Done

1. Detection type and operator category are separate.
2. Stoppage and LossExplanation are separate entities with a 1:N relationship.
3. Data ownership and `java.time` duration/rounding are enforced.
4. Normal edits cannot over-allocate minutes; system shrink creates a conflict without rewriting operator data.
5. Resolved losses retain explanations and audit history.
6. Domain and JPA models remain separate.
7. Versioned migration, domain tests and persistence tests pass.

## Implemented boundary

- The pure aggregate owns UUID identity, shift/sensor interval identity, exact start/duration, rounded minutes, lost cans, state, optimistic version and explanations.
- Normal explanation commands lock the parent aggregate and cannot over-allocate. A backend system measurement may shrink the loss and derives `ALLOCATION_CONFLICT` without rewriting operator fields.
- JPA `StoppageEntity` and `LossExplanationEntity` are separate from domain and both carry optimistic versions.
- V4 preserves legacy rows, backfills unambiguous FIXED/TEMPO rows and reports system rows that have no interval label. An automated copy-style rehearsal migrates V1/V2 data through V3/V4.
- Until Stage 4, recalculation uses a safe compatibility transition: prior ACTIVE rows become RESOLVED and new detections are inserted. Matching, update-in-place and preservation/relinking of explanations across Reconcile remain Stage 4; invariant I4 therefore remains disabled.


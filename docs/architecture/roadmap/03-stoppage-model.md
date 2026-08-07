# Stage 3 вЂ” Stoppage and LossExplanation Model

Status: **APPROVED / NOT IMPLEMENTED**

## Domain model

```text
Stoppage 1 в”Ђв”Ђв”Ђв”Ђв”Ђв”Ђ 0..N LossExplanation
```

- `DetectionType`: `FIXED`, `TEMPO` вЂ” how the backend detected loss.
- `LossCategory`: `ORGANIZATION`, `BREAKDOWN`, `MATERIAL`, `QUALITY` вЂ” why the operator says it occurred.
- `Stoppage` owns detection identity, shift/sensor interval, exact time, rounded minutes, lost cans, state and explanations.
- `LossExplanation` owns category, comment and allocated whole minutes; backend owns allocated cans.

`ExplanationStatus` is derived:

```text
0 allocated                         в†’ UNEXPLAINED
0 < allocated < roundedMinutes      в†’ PARTIALLY_EXPLAINED
allocated = roundedMinutes          в†’ FULLY_EXPLAINED
allocated > roundedMinutes          в†’ ALLOCATION_CONFLICT
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


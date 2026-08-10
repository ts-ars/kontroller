# Implementation Status

Roadmap A: **APPROVED**  
Implementation: **IN PROGRESS**
Source of truth: this repository

| Stage | Specification | Implementation |
|---|---|---|
| 0 — Business invariants | APPROVED | BASELINE FOR ALL WORK |
| 1 — Test environment | APPROVED | DONE |
| 2 — Comments | APPROVED | DONE |
| 3 — Stoppage model | APPROVED | DONE |
| 4 — Reconcile | APPROVED | DONE |
| 5 — Time | APPROVED | DONE |
| 6 — Signals | APPROVED | DONE |
| 7 — Transactions | APPROVED | DONE |
| 8 — Settings | APPROVED | TODO |
| 9 — Architectural cleanup | APPROVED | TODO |
| 10 — Production release | APPROVED | TODO |

## Current action

Stage 7 is implemented in a separate change set based on merged `main` commit
`9d1561f6b40ac76e204cf28422acb612693effed`. Signal persistence, Actual increment and Reconcile now
share one transaction; concurrent registration is serialized per production date and sensor;
database conflict handling makes duplicate source identity a no-op; and Shift update delivery is
after commit. ADAM uses persisted per-sensor Counter Input state, applies counter delta, preserves the
old-day assignment for a poll crossing 07:00 and records discontinuity without inventing production.

Do not extend this change set with Stage 8 settings-group storage or update behavior, Stage 9 cleanup
or Stage 10 production operations.


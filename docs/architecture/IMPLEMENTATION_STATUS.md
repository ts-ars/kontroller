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
| 6 — Signals | APPROVED | TODO |
| 7 — Transactions | APPROVED | TODO |
| 8 — Settings | APPROVED | TODO |
| 9 — Architectural cleanup | APPROVED | TODO |
| 10 — Production release | APPROVED | TODO |

## Current action

Stage 5 is implemented in the separately audited and approved change set based on merged
`main` commit `9c2923b6424d86c00c323ac59b7c37afb89fed42`. Production-day resolution,
cross-midnight interval semantics, plan-required extension, timestamp redistribution and injected
`Clock` are verified. Keep the Stage 5 review separate and do not begin Stage 6 in the same change set.


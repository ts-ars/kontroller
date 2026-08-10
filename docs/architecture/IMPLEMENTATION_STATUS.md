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
| 7 — Transactions | APPROVED | TODO |
| 8 — Settings | APPROVED | TODO |
| 9 — Architectural cleanup | APPROVED | TODO |
| 10 — Production release | APPROVED | TODO |

## Current action

Stage 6 is implemented in a separate change set based on merged `main` commit
`4cfeb9a0e01618c4e80fc2ac1a6d919dbb7cbbd7`. The historical `primary` data migrates to
`sensor-1`; the stable six-sensor catalog and settings-group ownership are persisted; Signal source
identity provides sequential duplicate no-op behavior; and Shift, Signal, API/event and Reconcile
boundaries preserve sensor identity.

Do not extend this change set with Stage 7 transaction/concurrency/counter recovery or Stage 8
settings-group behavior.


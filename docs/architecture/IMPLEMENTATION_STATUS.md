# Implementation Status

Roadmap A: **APPROVED**  
Implementation: **STAGE 9 COMPLETE**
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
| 8 — Settings | APPROVED | DONE |
| 9 — Architectural cleanup | APPROVED | DONE |
| 10 — Production release | APPROVED | TODO |

## Current action

Stage 9 is implemented in a separate change set based on merged Stage 8 `main` commit
`2beb964783edd2145584c504027cf59bba513747`. Feature layers now follow
`adapter -> application -> domain`; cross-feature calls use application ports; domain and JPA models
are separated; transactions start at application boundaries; and duplicate/dead architectural paths
are removed. ArchUnit and focused domain tests protect these decisions.

No database migration was required. Stage 10 production operations remain out of scope.

## Previous action (Stage 8)

Stage 8 is implemented in a separate change set based on merged Stage 7 `main` commit
`f42f4fc3adfcb3ab2a287cde5d01cf0a5f9c333b`. Two persisted settings groups now own ordered
Time/Plan interval records. A group update and recalculation of its existing current member shifts is
one transaction; Time changes redistribute persisted signals, plan-only changes preserve Actual,
events follow commit, the other group is isolated and completed shifts remain unchanged.

That Stage 8 change set did not include Stage 9 cleanup or Stage 10 production operations.


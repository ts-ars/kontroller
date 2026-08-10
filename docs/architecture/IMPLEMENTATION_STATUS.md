# Implementation Status

Roadmap A: **APPROVED**  
Implementation: **STAGE 10 REPOSITORY CONTROLS IMPLEMENTED; OPERATIONAL RELEASE GATES PENDING**
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
| 10 — Production release | APPROVED | IN PROGRESS — EXTERNAL RELEASE GATES PENDING |

## Current action

Stage 10 repository-owned controls are implemented in a separate change set based on merged Stage 9
`main` commit `a06957711f1ce9caeeabf550da86f17f1ded892f`. Production startup validates the
profile, database target, least-privilege role and externally supplied credentials. Six ADAM devices,
polling, timeout and retry are externalized; failures reconnect independently and expose a private
ADAM health group. Production adds operator/admin authorization, settings-change audit events,
correlation context, structured logs, graceful shutdown and private liveness/readiness management.
CI verifies the full suite and release load tests, then packages a version/commit-identified artifact.
Deployment, ADAM, backup/restore and rollback procedures are documented without secrets.

No database migration is required: V7 already owns durable per-sensor counter state and V8 remains
the current schema. Stage 10 is not marked DONE until real secret rotation, infrastructure/TLS/network
configuration, backup restore and migration rehearsal, six-device smoke/restart/outage validation and
production monitoring evidence are separately completed and approved.

## Previous action (Stage 9)

Stage 9 was implemented in a separate change set based on merged Stage 8 `main` commit
`2beb964783edd2145584c504027cf59bba513747`. Feature layers follow `adapter -> application -> domain`;
cross-feature calls use application ports; domain and JPA models are separated; transactions start at
application boundaries; and duplicate/dead architectural paths are removed.

## Previous action (Stage 8)

Stage 8 is implemented in a separate change set based on merged Stage 7 `main` commit
`f42f4fc3adfcb3ab2a287cde5d01cf0a5f9c333b`. Two persisted settings groups now own ordered
Time/Plan interval records. A group update and recalculation of its existing current member shifts is
one transaction; Time changes redistribute persisted signals, plan-only changes preserve Actual,
events follow commit, the other group is isolated and completed shifts remain unchanged.

That Stage 8 change set did not include Stage 9 cleanup or Stage 10 production operations.


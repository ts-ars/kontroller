# Roadmap A

Status: **APPROVED**  
Implementation: **NOT STARTED**

Roadmap A is the approved architectural plan for Shift. It is a specification, not evidence that the application already implements these rules. Architectural changes require explicit review.

## Stages

0. [Business invariants](roadmap/00-invariants.md)
1. [Stable test environment](roadmap/01-test-environment.md)
2. [Comments and loss explanations](roadmap/02-comments.md)
3. [Stoppage and LossExplanation model](roadmap/03-stoppage-model.md)
4. [Unified Reconcile](roadmap/04-reconcile.md)
5. [Time and production day](roadmap/05-time.md)
6. [Signals and sensors](roadmap/06-signals.md)
7. [Transactions and ADAM-6050 Counter Input](roadmap/07-transactions.md)
8. [Settings](roadmap/08-settings.md)
9. [Architectural cleanup](roadmap/09-architecture-cleanup.md)
10. [Production release](roadmap/10-production-release.md)

Approved cross-stage decisions are listed in [DECISIONS.md](DECISIONS.md). Current implementation state is recorded in [IMPLEMENTATION_STATUS.md](IMPLEMENTATION_STATUS.md).

## Execution rule

Stages are implemented in order. For each stage:

```text
audit в†’ change plan в†’ implementation в†’ tests в†’ Definition of Done
в†’ commit в†’ update IMPLEMENTATION_STATUS.md в†’ next stage
```

Before Stage 1 changes, inspect the existing project against Stage 1 and report what exists, conflicts, is missing, which files would change, and which tests exist or are required. Do not treat this document as authorization to rewrite the whole project at once.


# Stage 4 вЂ” Unified Reconcile

Status: **APPROVED / NOT IMPLEMENTED**

## Single scenario

All triggers call one `ReconcileStoppagesUseCase`. Controllers, listeners and calculators do not independently save FIXED or TEMPO.

For one sensor interval:

```text
totalLoss = max(0, plan - actual)
effectiveFixed = validated FIXED bounded by totalLoss
TEMPO = totalLoss - effectiveFixed
fixed + tempo = totalLoss
```

The current interval uses the full plan. TEMPO is the residual loss, not the result of a competing independent formula. `calculationTime` is supplied to the use case; it does not call `now()` internally.

## Reconciliation behavior

- Load the shift, interval, signals, plan/actual, productivity and existing stoppages once.
- Pure calculators receive a complete immutable context and do not query repositories.
- At most one active TEMPO exists per sensor interval; it is updated in place or marked `RESOLVED`.
- Multiple FIXED ranges are matched to existing ranges by overlap or a configured boundary-shift policy. Matching is deterministic; ambiguous matching is reported and not resolved by arbitrary list order.
- Existing `detectionKey` and explanations survive an update. New ranges receive new keys; missing ranges become `RESOLVED`.
- A FIXED crossing an interval boundary is represented as separate interval parts with the same incident identity, for example `10:55вЂ“11:00` and `11:00вЂ“11:10`. The operator enters explanations separately for each interval part.
- Reconcile never deletes, shortens or moves operator explanations. It recalculates allocated cans and exposes allocation conflicts.
- Fully allocated explanation cans must sum exactly to the stoppage lost cans using deterministic largest-remainder distribution.
- Reconcile is atomic for one interval and idempotent for identical input.

Diagnostic outcomes include invalid interval, FIXED exceeding total loss, ambiguous FIXED match, allocation conflict, balance mismatch and concurrent modification.

## Definition of Done

One use case serves every trigger; balance is exact; TEMPO and FIXED identities are stable; explanations survive; missing losses resolve rather than disappear; repeated and concurrent runs create no duplicates; controllers/listeners contain no calculation or direct persistence; all Reconcile tests pass.


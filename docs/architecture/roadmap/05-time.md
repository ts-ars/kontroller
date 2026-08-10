# Stage 5 — Time and Production Day

Status: **APPROVED / IMPLEMENTED**

## Production day

For production date `D`:

```text
[D 07:00, D+1 07:00)
```

Exactly `07:00` belongs to the new production day. Old days cannot be extended beyond that boundary.

## Interval model

- Ordered `Time` values are the source of interval boundaries; duration is not stored independently.
- Adjacent Time values define ordinary intervals, including approved 60- and 90-minute intervals.
- Intervals are ordered, continuous, non-overlapping and cover every accepted signal timestamp within the active production window.
- The final Time ending in `:00` creates a 60-minute final interval; ending in `:30` creates a 30-minute final interval, ending at the next whole hour.
- Every interval has its own plan supplied by the operator.

If a valid signal arrives beyond the currently configured end, it is saved and the time line is extended continuously with plan-required intervals until the timestamp is covered. Such an interval accumulates actual but does not run final loss, TEMPO or comments until an operator supplies the plan.

The operator may later correct Time and Plan. Time correction redistributes existing signals by `occurredAt`, recalculates interval actuals and runs Reconcile. Signals are not deleted or recreated.

## Definition of Done

All services use the same production-date and interval services; every valid signal maps to exactly one interval; the `07:00` boundary is unambiguous; plan-required behavior and operator correction preserve signals; time calculations use `java.time` and an injected `Clock` where current time is required.

## Implemented boundary

- `ProductionDayService` owns `[D 07:00, D+1 07:00)` resolution and the application `Clock`;
  current shift, comments, reports, settings and manual Reconcile use that boundary.
- `ShiftIntervalService` derives absolute, ordered, half-open intervals from persisted Time values,
  including midnight rollover, 60/90-minute intervals and the final `:00`/`:30` rule.
- A signal beyond the configured end extends the timeline only within the production day. Extended
  intervals keep actual independently of the plan list and skip Reconcile until a plan is supplied.
- Time correction reloads saved signals from the production window, redistributes actual by timestamp
  and invokes unified Reconcile. Plan-only correction preserves actual; removed intervals are resolved
  through the same Reconcile use case.
- Signal reads use an explicit `[start,end)` persistence query. Direct system time and system-zone calls
  outside the approved time configuration/service are rejected by an architecture test.
- Stage 5 retains the current `primary` sensor. Signal identity, six sensors, transaction atomicity,
  counter-boundary delta handling and settings groups remain Stages 6–8.


# Stage 5 вЂ” Time and Production Day

Status: **APPROVED / NOT IMPLEMENTED**

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


# Stage 7 вЂ” Transactions, Concurrency and ADAM Counter Input

Status: **APPROVED / NOT IMPLEMENTED**

## Atomicity

One signal is one business transaction:

```text
BEGIN в†’ persist Signal в†’ update sensor Actual в†’ Reconcile
в†’ persist losses в†’ COMMIT в†’ publish ShiftUpdatedEvent
```

Any failure before commit rolls back the whole scenario. WebSocket, HTTP response, notifications, logging and metrics are outside the business transaction and cannot turn a committed operation into a rollback.

Duplicate input performs no Signal insert, actual increment, Reconcile or update event. Locking is scoped by `productionDate + sensorId`: two threads cannot update one sensor/day concurrently, while different sensors may proceed in parallel. Late timestamped signals use `occurredAt` and the Stage 5 time rules.

Settings-group updates are also atomic: save settings and recalculate every sensor in that group, or persist none of those changes. Saving an operator explanation is a separate transaction and does not block signal registration unnecessarily.

## ADAM-6050

- Digital input mode is **Counter Input**.
- Application processing uses `delta = currentCounter - lastCounterValue`, not the absolute total as production actual.
- `lastCounterValue` is persisted per sensor; daily manual counter reset is not required.
- If one poll crosses `07:00`, the entire delta belongs to the old production day and the current reading becomes the new day's baseline.
- Restart recovery loads the stored counter state; it does not silently replace the baseline.
- `currentCounter < storedCounter` is `COUNTER_DISCONTINUITY`; the system must not invent or apply a delta.

## Definition of Done

Signal, actual and Reconcile are atomic; events are after commit; duplicates are no-ops; same-sensor concurrency loses no data and different sensors can run in parallel; settings-group updates are all-or-nothing; Counter Input delta, day-boundary behavior, restart and discontinuity tests pass.


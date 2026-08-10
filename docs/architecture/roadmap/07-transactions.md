# Stage 7 — Transactions, Concurrency and ADAM Counter Input

Status: **APPROVED / IMPLEMENTED**

## Atomicity

One signal is one business transaction:

```text
BEGIN → persist Signal → update sensor Actual → Reconcile
→ persist losses → COMMIT → publish ShiftUpdatedEvent
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

## Implemented boundary

- `SignalService.register` is the transaction boundary for Signal → Actual → Reconcile. A
  PostgreSQL transaction-scoped advisory lock keyed by `productionDate + sensorId` serializes one
  sensor/day without blocking a different sensor. Signal insertion uses the persisted unique source
  identity with `ON CONFLICT DO NOTHING`, so a concurrent duplicate performs no downstream work.
- The synchronous product-detected application path remains inside that transaction. The common
  Shift event adapter defers publication until `afterCommit` and discards it on rollback. A forced
  Reconcile failure test proves that Signal and Shift Actual are both rolled back.
- V7 adds one persisted `counter_states` row per sensor with last counter value, last read time,
  production date and continuity. The first reading establishes a baseline; restart uses that stored
  baseline; positive delta becomes stable `ADAM/counter-N` signals through the same registration use
  case; and a lower reading persists `COUNTER_DISCONTINUITY` without applying delta or replacing the
  trusted counter value.
- A poll crossing 07:00 attributes its whole positive delta using the previous read timestamp and
  production day, then persists the current reading as the new production day's baseline.
- Stage 8 group-settings save/recalculation is not introduced here. The Stage 7 transaction guarantee
  for such an update becomes applicable when the group update use case exists in Stage 8.


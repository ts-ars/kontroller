# Stage 6 — Signals and Six Sensors

Status: **APPROVED / IMPLEMENTED**

## Signal path

Every source—ADAM, HTTP simulation, batch, recovery or future adapter—calls the same registration scenario. An adapter supplies `sensorId`, timestamp and source identity; it does not know about shifts, stoppages, comments or UI.

`Signal` is the persisted source of truth and contains at least its identity, `sensorId`, `occurredAt`, production date, source and source-specific unique identity when available. A simulation request generates its own UUID; timestamp alone is not a unique signal key.

## Sensor model

There are six independent sensors. Each has its own:

```text
signals, actual, applied plan, FIXED, TEMPO,
LossExplanation, comments and Reconcile
```

One signal changes only `sensorId + productionDate + interval`. Different sensors are never summed or made to affect each other automatically.

- Sensors 1–4 belong to the first settings group.
- Sensors 5–6 belong to the second settings group.
- Settings are shared within a group; runtime facts and results remain per sensor.
- No Machine entity, pouch/washer conversion or sensor-specific `if/switch` path is part of this design.
- Adding a sensor requires data/configuration and assigning `settingsGroupId`, not a new business algorithm.

Reconcile always includes `sensorId`, and its balance is calculated independently per sensor.

## Definition of Done

There is one registration use case for every adapter; one physical signal causes one persisted signal, one increment and one Reconcile; duplicates and recovery do not double-count; all six sensors remain independent; settings-group changes trigger an individual recalculation for each member sensor; signal and sensor tests pass.

## Implemented boundary

- `SignalInputPort.register(RegisterSignalCommand)` is the single input scenario for ADAM and both
  test-only HTTP adapters. The command carries `sensorId`, `occurredAt`, `source` and
  `sourceIdentity`; HTTP simulation creates a new UUID identity for each request.
- A persisted Signal owns UUID identity, sensor, occurrence time, production date, source and source
  identity. `(sensor_id, source, source_identity)` is unique. A sequential duplicate is a no-op before
  the product-detected event, Actual increment and Reconcile trigger.
- The stable catalog contains `sensor-1` through `sensor-6`. Sensors 1–4 belong to
  `settings-group-1`; sensors 5–6 belong to `settings-group-2`. Stage 6 persists this ownership but
  does not introduce group settings or group-change behavior.
- Shift persistence is keyed by `(productionDate, sensorId)`. Signal reads, time redistribution,
  Reconcile, stoppage reads, shift projections, REST responses and update events preserve sensor
  identity. Runtime facts and losses are isolated per sensor.
- V6 migrates every historical single-sensor Shift, Signal and system Stoppage from `primary` to
  `sensor-1`, derives legacy signal production dates at the approved 07:00 boundary and creates the
  six-row sensor catalog.
- Stage 6 intentionally provides sequential idempotency only. Atomic Signal/Actual/Reconcile commit,
  same-sensor concurrency control, after-commit publication, counter delta handling and recovery from
  a partial failure remain Stage 7. Settings-group update behavior remains Stage 8.


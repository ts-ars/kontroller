# Stage 6 вЂ” Signals and Six Sensors

Status: **APPROVED / NOT IMPLEMENTED**

## Signal path

Every sourceвЂ”ADAM, HTTP simulation, batch, recovery or future adapterвЂ”calls the same registration scenario. An adapter supplies `sensorId`, timestamp and source identity; it does not know about shifts, stoppages, comments or UI.

`Signal` is the persisted source of truth and contains at least its identity, `sensorId`, `occurredAt`, production date, source and source-specific unique identity when available. A simulation request generates its own UUID; timestamp alone is not a unique signal key.

## Sensor model

There are six independent sensors. Each has its own:

```text
signals, actual, applied plan, FIXED, TEMPO,
LossExplanation, comments and Reconcile
```

One signal changes only `sensorId + productionDate + interval`. Different sensors are never summed or made to affect each other automatically.

- Sensors 1вЂ“4 belong to the first settings group.
- Sensors 5вЂ“6 belong to the second settings group.
- Settings are shared within a group; runtime facts and results remain per sensor.
- No Machine entity, pouch/washer conversion or sensor-specific `if/switch` path is part of this design.
- Adding a sensor requires data/configuration and assigning `settingsGroupId`, not a new business algorithm.

Reconcile always includes `sensorId`, and its balance is calculated independently per sensor.

## Definition of Done

There is one registration use case for every adapter; one physical signal causes one persisted signal, one increment and one Reconcile; duplicates and recovery do not double-count; all six sensors remain independent; settings-group changes trigger an individual recalculation for each member sensor; signal and sensor tests pass.


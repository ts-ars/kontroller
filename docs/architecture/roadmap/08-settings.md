# Stage 8 — Persisted Settings Groups

Status: **APPROVED / IMPLEMENTED**

## Model

The model and update sections below preserve the approved historical V8 contract. They are not
rewritten retroactively. The later **Approved V9 ownership revision** is authoritative wherever the
original 4+2 ownership conflicts with the six-sensor UI contract.

Settings belong to a sensor group, not globally to the application and not directly to a delivery adapter.

```text
SettingsGroup
  id, name, enabled
  IntervalSetting[]
    startTime
    plan
    order
  FIXED/TEMPO/Reconcile parameters
```

- In V8, Group 1 applied to Sensors 1–4.
- In V8, Group 2 applied to Sensors 5–6.
- Each sensor belongs to exactly one group.
- Time and Plan are stored together as interval records, not as parallel arrays.
- Duration is derived from neighboring Time values.
- `SettingsRepository` is the source of truth; no in-memory alternative, cache with its own lifecycle or magic plan/time values.

## Updates

- Changing Time redistributes saved signals, recalculates actual, runs Reconcile and refreshes the resulting comments/conflicts for each sensor in the group.
- Changing only Plan runs Reconcile without recalculating actual.
- Updating one group does not affect the other.
- Completed shift history remains based on its original settings; new shifts use current settings.
- The system never changes Time or Plan automatically; the operator owns those changes.

Validation rejects empty, duplicated or unordered Time values, overlapping intervals and negative plans.

## Definition of Done

Two independent groups exist; every sensor has one group; interval settings are the sole Time/Plan source; Time and Plan changes trigger only their approved recalculations; historical shifts remain stable; invalid settings cannot be saved; group-isolation and recalculation tests pass.

## Implemented boundary

- V8 replaces the global `settings` key/value table with `settings_groups` and ordered
  `interval_settings`. It migrates the legacy Time/Plan values into both approved groups and stores
  each Time with its Plan under database uniqueness and non-negative constraints.
- `SettingsRepository` is the only settings source. Reads resolve the sensor's persisted group on
  demand; the former process-local provider cache and parallel-array persistence adapter are gone.
- `SettingsGroupService.update` is the transaction boundary. It locks the group and every member
  sensor for the current production day, then saves settings and updates all existing current member
  shifts or rolls the entire update back. Events are delivered only after commit.
- Time changes use the Stage 5 correction service to redistribute persisted per-sensor signals and
  Reconcile. Plan-only changes preserve Actual and run Reconcile. The other group and completed
  production dates are not modified.
- Empty, mismatched, duplicated, unordered or invalid Time values and negative plans are rejected.
  `enabled` is persisted as group state; no operational enable/disable behavior is invented because
  the approved Stage 8 contract does not define one. Stages 9 and 10 remain outside this change set.

## Approved V9 ownership revision

The later six-sensor UI contract deliberately supersedes only the original 4+2 settings ownership:

- Sensors 1–4 share one editable plan; Sensor 5 derives that plan at ×4; Sensor 6 is independent.
- Hour is one timeline saved atomically for all six sensors.
- Sensor 5 keeps independent signals and Actual but has no own FIXED/TEMPO/LossExplanation workflow.
- V9 force-loads the approved 16 rows and updates persisted sensor ownership without editing V1–V8 or
  completed shift snapshots.
- The existing active-shift guarantees remain: plan-only preserves Actual, Time redistributes persisted
  signals, affected sensors Reconcile, completed history is immutable and a failure rolls everything back.
- `/api/settings/{groupId}` remains the route but transports one composite Settings snapshot.
- Add/Delete rotates only the half-tail: the nearest previous full plan fills the old tail, the old
  half-plan moves forward by 60 minutes, only the last extension can be removed, and deletion restores
  the previous snapshot exactly without crossing the next `07:00` boundary.
- Regression evidence must cover all six active shifts in one common-Time update, derived Sensor 5
  plans without Sensor 5 Reconcile, independent Sensor 6 plans, Actual preservation for plan-only
  changes, timestamp redistribution for Time changes, completed-history stability and rollback.


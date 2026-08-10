# Stage 8 — Two Settings Groups

Status: **APPROVED / IMPLEMENTED**

## Model

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

- Group 1 applies to Sensors 1–4.
- Group 2 applies to Sensors 5–6.
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


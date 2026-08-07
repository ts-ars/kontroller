# Stage 8 вЂ” Two Settings Groups

Status: **APPROVED / NOT IMPLEMENTED**

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

- Group 1 applies to Sensors 1вЂ“4.
- Group 2 applies to Sensors 5вЂ“6.
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


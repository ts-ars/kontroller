# Approved Decisions

This file contains only final cross-stage decisions. Detailed contracts and Definitions of Done live in `roadmap/`.

1. GitHub repository content is the source of truth for Shift code and architecture.
2. Roadmap A is fully designed but not implemented.
3. Production day is `[D 07:00, D+1 07:00)`; exactly `07:00` belongs to the new day.
4. Each of six sensors has independent signals, actual, plan application, FIXED, TEMPO, explanations and Reconcile.
5. Sensors 1–4 use one settings group; sensors 5–6 use a second independent group. Shared settings never imply shared actual.
6. Machine, pouch and washer conversion models are outside the approved design. Routing is by `sensorId` and `settingsGroupId`.
7. ADAM-6050 operates in Counter Input mode. The application processes counter `delta` and persists the last counter state; daily manual reset is not required.
8. If a counter poll crosses the `07:00` boundary, the whole delta belongs to the old production day and the current counter becomes the new day's baseline.
9. One physical signal may increase actual only once. Duplicate input performs no increment, Reconcile or update event.
10. One signal scenario is atomic: Signal → Actual → Reconcile → commit. UI notification is after commit.
11. Backend owns system loss data. The operator owns category, comment and whole allocated minutes. Backend calculates allocated cans.
12. One Stoppage has zero or many LossExplanation records. FIXED and TEMPO use the same explanation model.
13. Operator explanations are not automatically deleted, shortened or rewritten by Reconcile. Conflicts are exposed explicitly.
14. `TEMPO = max(0, plan - actual) - effective FIXED`; current intervals use the full interval plan.
15. All time calculations use `java.time`; loss duration is rounded to whole minutes using half-up rules at 30 seconds.
16. Intervals are derived from ordered `Time` values. The final `Time` ending in `:00` produces a 60-minute last interval; ending in `:30` produces a 30-minute last interval.
17. Changing interval Time redistributes saved signals by timestamp, recalculates actual and runs Reconcile. Changing only Plan runs Reconcile without recalculating actual.
18. Historical completed shifts retain the settings with which they were created; new shifts use current settings.
19. Dependencies point `adapter → application → domain`. Transactions live at the application boundary. Domain, JPA entities and transport DTOs are separate.
20. Test and production environments, databases and database users are separate. HTTP simulation is test-only; ADAM is production-only.
21. Secrets are not stored in Git. Production schema changes are owned by Flyway; Hibernate validates the schema.
22. Stage 2 introduces the minimum persisted `LossExplanation` model and the `Stoppage 1:N LossExplanation` relationship. Stable detection identity, loss state, optimistic locking and full domain/JPA separation remain Stage 3.
23. Stage 2 preserves explanations during ordinary explanation CRUD. Stable preservation and relinking during Reconcile remain Stage 4 and are not implemented by Stage 2.
24. Stage 2 derives partial `allocatedCans` proportionally as `round(stoppageLostCans * allocatedMinutes / stoppageRoundedMinutes)`. Deterministic largest-remainder distribution across all explanations remains Stage 4.
25. Legacy operator-category stoppage rows are not linked automatically. V3 preserves them and records them in a migration report for later reviewed migration.
26. Stage 2 applies integer half-up rounding wherever an exact `Duration` is available. Persisted exact start/end/duration and unified production-day time boundaries remain Stages 3 and 5.
27. Stage 3 represents the current single sensor as the explicit key `primary`. `Stoppage` owns `shiftId`, `sensorKey`, interval index, exact `LocalDateTime startedAt` and exact `Duration`; Stage 5 still owns production-day and interval-boundary semantics.
28. Stage 3 replaces the domain/JPA hybrid with a pure `Stoppage` aggregate and an application repository port. JPA entities, Spring Data repositories and mapping are confined to the persistence adapter.
29. Stage 3 compatibility never physically deletes an active loss: the existing per-hour recalculation marks previous rows `RESOLVED` before inserting new detections. Stable matching, update-in-place, identity preservation across recalculation and explanation relinking remain Stage 4.
30. V4 backfills unambiguous FIXED/TEMPO rows with deterministic UUID keys, exact legacy-derived time/duration, state and versions. Rows without an interval label remain in the legacy representation and are recorded in `stoppage_model_migration_report`; V1–V3 are not edited.


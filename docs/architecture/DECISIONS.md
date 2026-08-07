# Approved Decisions

This file contains only final cross-stage decisions. Detailed contracts and Definitions of Done live in `roadmap/`.

1. GitHub repository content is the source of truth for Shift code and architecture.
2. Roadmap A is fully designed but not implemented.
3. Production day is `[D 07:00, D+1 07:00)`; exactly `07:00` belongs to the new day.
4. Each of six sensors has independent signals, actual, plan application, FIXED, TEMPO, explanations and Reconcile.
5. Sensors 1вЂ“4 use one settings group; sensors 5вЂ“6 use a second independent group. Shared settings never imply shared actual.
6. Machine, pouch and washer conversion models are outside the approved design. Routing is by `sensorId` and `settingsGroupId`.
7. ADAM-6050 operates in Counter Input mode. The application processes counter `delta` and persists the last counter state; daily manual reset is not required.
8. If a counter poll crosses the `07:00` boundary, the whole delta belongs to the old production day and the current counter becomes the new day's baseline.
9. One physical signal may increase actual only once. Duplicate input performs no increment, Reconcile or update event.
10. One signal scenario is atomic: Signal в†’ Actual в†’ Reconcile в†’ commit. UI notification is after commit.
11. Backend owns system loss data. The operator owns category, comment and whole allocated minutes. Backend calculates allocated cans.
12. One Stoppage has zero or many LossExplanation records. FIXED and TEMPO use the same explanation model.
13. Operator explanations are not automatically deleted, shortened or rewritten by Reconcile. Conflicts are exposed explicitly.
14. `TEMPO = max(0, plan - actual) - effective FIXED`; current intervals use the full interval plan.
15. All time calculations use `java.time`; loss duration is rounded to whole minutes using half-up rules at 30 seconds.
16. Intervals are derived from ordered `Time` values. The final `Time` ending in `:00` produces a 60-minute last interval; ending in `:30` produces a 30-minute last interval.
17. Changing interval Time redistributes saved signals by timestamp, recalculates actual and runs Reconcile. Changing only Plan runs Reconcile without recalculating actual.
18. Historical completed shifts retain the settings with which they were created; new shifts use current settings.
19. Dependencies point `adapter в†’ application в†’ domain`. Transactions live at the application boundary. Domain, JPA entities and transport DTOs are separate.
20. Test and production environments, databases and database users are separate. HTTP simulation is test-only; ADAM is production-only.
21. Secrets are not stored in Git. Production schema changes are owned by Flyway; Hibernate validates the schema.


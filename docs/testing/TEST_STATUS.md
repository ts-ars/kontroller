# Test Status

## Environment

- Date: 2026-08-11
- Branch: `codex/stage-10-release-hardening`, based on merged Stage 10 controls commit `1bd5468`
- Spring profile: `test`
- Required database: `shiftcounter_test`
- Required database user: `shift_test`
- Production database access: denied by provisioning policy and verified by `TestEnvironmentIsolationIT`
- Java: 21.0.5
- Maven Wrapper: 3.9.9

## Latest verified commands

| Command | Result |
|---|---|
| `.\mvnw.cmd test-compile -DskipTests` | PASS |
| Stage 8 focused settings suite | PASS — 6 executed, 0 skipped/failures/errors |
| First final `.\mvnw.cmd clean verify` | PASS — 121 executed, 0 skipped/failures/errors; 6:56 |
| Second clean `.\mvnw.cmd clean verify` on a fresh database | PASS — same 121 totals, 0 skipped/failures/errors; 9:11 |
| Stage 9 final `mvn -q clean verify` | PASS — 131 executed, 0 skipped/failures/errors; 5:16 |
| Stage 10 CI `./mvnw clean verify` | PASS — 138 executed, 0 skipped/failures/errors; 1:18 |
| Stage 10 release load suite | PASS — 2 executed, 0 skipped/failures/errors; 2:08 |
| Stage 10 versioned package/upload | PASS — `shiftcounter-0.0.20.jar` |
| Stage 10 follow-up `clean verify` | PASS — 143 executed (134 Surefire + 9 Failsafe), 0 skipped/failures/errors; 11:34 |
| Stage 10 follow-up local load suite | NOT COMPLETED — exceeded the 10-minute local command limit |

## PR 4 integrated regression attempt

- Integration base: `16d755e088806044e8531b818eb9408b7e76b6c0` (PR 1 + PR 2 + PR 3 bundle),
  verified as a descendant of `7a083371c84dc3bd6aca9ff62ae33b505abafe87`.
- `test-compile -DskipTests`: PASS.
- Focused database-free domain/read-model/MVC/CSRF/WebSocket suite: PASS — 58 executed,
  0 skipped/failures/errors.
- Isolated PostgreSQL `clean verify`: NOT RUN — Docker daemon and a local PostgreSQL installation
  were unavailable. No production or user database was contacted.
- Migration rehearsal through V9 and the new `Pr4SixSensorRegressionIT`: COMPILED, NOT EXECUTED for
  the same isolated-PostgreSQL limitation.
- Release load suite: NOT RUN — both load scenarios require the isolated test database.
- Visual review: source-level HTML/CSS comparison completed against all four approved mockups. Browser
  rendering of local `file://` artifacts was blocked by the in-app browser security policy, so this is
  not pixel-level or browser sign-off.
- Detailed coverage and limitations: `docs/testing/PR4_REGRESSION_EVIDENCE.md`.

Stage 10 adds focused production configuration/startup/ADAM tests and a separately tagged parallel
six-sensor release load scenario. The local Stage 10 cloud checkout could not resolve Maven
dependencies because its Maven cache was empty and outbound Maven Central/JitPack access was
unavailable; YAML parsing and `git diff --check` passed locally. GitHub CI run 18 then completed the
authoritative full verification, real load suite, versioned packaging and artifact upload.

Stage 8 verification used PostgreSQL 15.13 in isolated Docker projects
`kontroller-stage8` and `kontroller-stage8b`, with host ports `55438` and `55439`. The
user project container `shift-postgres` on port `5432` was not modified or used.

## Latest test totals

- Surefire: 129 executed, 0 failures, 0 errors, 0 skipped.
- Failsafe: 9 executed, 0 failures, 0 errors, 0 skipped.
- Combined: 138 executed, 0 failures, 0 errors, 0 skipped.
- Release load suite: 2 executed, 0 failures, 0 errors, 0 skipped.

## Test classification

- Unit and normal Spring tests run through Maven Surefire.
- Integration environment checks carry JUnit tag `e2e`; Surefire excludes them.
- Maven Failsafe runs the `e2e` group during `verify`.
- The 7,992-signal load scenario carries JUnit tag `load` and is not part of standard `verify`.
- All Maven test phases set `spring.profiles.active=test`.

## Environment protections

- Test profile rejects any JDBC database other than `shiftcounter_test`.
- Test profile rejects any database user other than `shift_test`.
- ADAM beans are absent in `test` and HTTP simulation beans are absent outside `test`.
- PostgreSQL provisioning revokes public connect access and grants each role only its own database.
- `TestEnvironmentIsolationIT` verifies the active profile, JDBC catalog/user, Flyway history,
  bean gating and denial of `shift_test` access to production.
- Spring integration tests use a centralized database reset listener.
- `StoppageMigrationRehearsalIT` migrates a separate schema through historical V1–V9 data,
  verifies deterministic valid-row backfill, `primary` to `sensor-1`, the six-row catalog and
  counter-state schema, V9 Sensor 5/Sensor 6 ownership, both approved 16-row plan sources and totals,
  records migration reports for invalid legacy rows, then removes the schema.

## Stage 4 protection and scenario coverage

- I1, I2, I3 and I4 are active and passing. I2–I4 were activated by unified Reconcile.
- `StoppageCalculatorTest` protects full current-interval plan, bounded FIXED and exact residual TEMPO.
- `StoppageMatcherTest` protects stable identity and operator data, exact idempotence, resolution,
  ambiguous-match rejection and adjacent incident identity.
- `StoppageReconcilesServiceTest` protects unified orchestration and no-write repeated input.
- `StoppageControllerTest` protects delegation and diagnostic transport.
- `Stage4ReconcileArchitectureTest` rejects the removed independent calculation/persistence paths.
- `StoppageReconcilePersistenceIT` proves no duplicate or second write on identical input,
  serialization of two concurrent calls and update-in-place with explanation preservation/conflict.
- Persistence and migration tests protect the V5 `incident_key` mapping and historical rehearsal.

## Stage 5 protection and scenario coverage

- `ProductionDayServiceTest` protects the exact `07:00` boundary and half-open production window.
- `ShiftIntervalServiceTest` protects midnight rollover, 90-minute intervals, exact interval
  boundaries, final `:30`, ordering validation, plan-required extension and the `D+1 07:00` cap.
- `ShiftProductRegistrarTimeTest` protects previous-production-date assignment after midnight and
  accumulation without Reconcile in an interval whose plan is not supplied.
- `ShiftTimeCorrectionServiceTest` protects timestamp redistribution and resolution of removed
  intervals through the unified Reconcile use case.
- `StoppageReconcilesServiceTest` protects absolute cross-midnight signal ranges and resolve-only
  Reconcile for removed intervals.
- `SignalJpaAdapterTest` protects the explicit persistence query contract `[start,end)`.
- `AdamEventEmitterTest` protects the production poll path from the absolute device counter into the
  counter-delta application use case.
- `SettingsPageTest` protects application of operator Time/Plan corrections from the actual HTML form.
- `Stage5TimeArchitectureTest` rejects direct current-time/system-zone calls outside the approved
  time boundary and prevents restoration of the obsolete duplicate time helper.

## Stage 6 protection and scenario coverage

- I5 is active and passing: sequential duplicate source identity publishes only one increment trigger.
- `SensorCatalogTest` protects the six stable IDs and approved 4+2 settings-group ownership.
- `SignalServiceTest` protects source identity duplicate no-op, sensor propagation and production-date
  assignment at the 07:00 boundary.
- `Stage6SignalRegistrationIntegrationTest` exercises the complete synchronous path: two deliveries
  of one `sensor-2` recovery identity create one Signal/increment/Reconcile, while the same source
  identity for `sensor-3` remains an independent fact and does not create `sensor-1` state.
- `ShiftSensorIsolationTest` protects independent Actual values; sensor-aware signal, shift, stoppage,
  report, REST projection and WebSocket event tests protect boundary propagation.
- `Stage6SensorArchitectureTest` rejects the historical `primary` literal in production Java, requires
  every current adapter to use the registration command and keeps Stage 7 transaction semantics out.
- Migration rehearsal protects historical Shift, Signal and Stoppage ownership by `sensor-1` and the
  V6 source-identity/catalog schema.

## Stage 7 protection and scenario coverage

- `Stage7SignalTransactionIntegrationTest` proves no lost Actual across 16 concurrent same-sensor
  registrations, one accepted result for 16 concurrent deliveries of the same source identity and
  independent transaction locks for different sensors.
- `Stage7SignalRollbackIntegrationTest` forces Reconcile to fail after the Shift increment and proves
  that both Signal and Shift are absent after rollback.
- `SpringEventPublisherAdapterTest` proves immediate delivery outside a transaction, after-commit
  delivery inside one and no delivery after rollback.
- `CounterInputServiceTest` protects baseline creation, stable counter-value identities, positive
  delta, 07:00 attribution, restart recovery and discontinuity without baseline replacement.
- `Stage7CounterRecoveryIntegrationTest` repeats restart, boundary and discontinuity behavior through
  Flyway V7 and real PostgreSQL persistence while verifying resulting per-sensor Actual.
- `Stage7TransactionArchitectureTest` protects the application transaction boundary, atomic database
  conflict handling and the absence of the obsolete in-memory ADAM edge state.

## Stage 8 protection and scenario coverage

- `JpaSettingsGroupAdapterTest` protects two independent persisted groups and paired Time/Plan rows.
- `SettingsSnapshotIntegrationTest` preserves the Stage 8 Actual/history/rollback guarantees while
  applying the approved shared/derived/independent ownership revision atomically.
- `SettingsPageTest` protects the unified table, totals and composite update routing.
- `Stage8SettingsArchitectureTest` rejects restoration of the global key/value storage or process-local
  settings cache and protects the application transaction/locking boundary.
- Migration rehearsal through V8 proves legacy global Time/Plan values become identical group settings;
  V9 then replaces them with the approved 16-row snapshot and new ownership.

## Stage 9 protection and scenario coverage

- `Stage9ArchitectureTest` enforces domain independence, application-to-adapter separation,
  controller/repository separation, cycle-free feature dependencies and removal of obsolete
  `infrastructure`/`api` production packages.
- `Stage9DomainInvariantTest` protects non-negative Shift values, plan/label bounds, contiguous and
  production-day-ordered settings intervals, unique interval starts and required sensor ownership.
- Existing Stage 0–8 architecture and behavior tests were updated to the final package boundaries and
  continue to pass without weakening their contracts.
- The full suite passed against isolated PostgreSQL 16.14 in `kontroller-stage9-postgres` on host port
  `55440`; the user project database on port `5432` was not used.
- Stage 9 adds no migration: Flyway validates the existing eight migrations and reports schema V8 as
  current.

## Intentionally disabled tests

None. The three Stage 8 placeholders were replaced by executable contract tests.

## Stage 8 status

**COMPLETE under the approved Stage 8 boundary.** Two groups are persisted independently; Time and Plan
are stored together; current member shifts update atomically; Time changes redistribute signals;
plan-only changes preserve Actual; history and the other group remain stable; and notifications follow
commit. That Stage 8 change set did not introduce Stage 9 or Stage 10 work.

## Stage 9 status

**COMPLETE under the approved Stage 9 boundary.** Layer direction, application ports, aggregate/entity
separation, use-case transaction ownership, dead-path removal and domain invariants are implemented
and protected. Stage 10 production release work was not introduced.

## Stage 10 status

**IN PROGRESS.** Repository-owned release controls and executable tests are present. Real production
backup/restore, migration rehearsal, secret rotation, network/TLS/monitoring setup, six-device smoke,
counter-running restart and database-outage evidence remain external blocking gates.

## V9 Settings ownership follow-up

- `SettingsSnapshotTest` protects the approved totals and reversible 60-minute half-tail rotation up to
  the 07:00 boundary.
- `SettingsSnapshotIntegrationTest` covers shared/derived/independent plan application, Sensor 5 Actual
  preservation, common-Time redistribution, completed-history immutability and transaction rollback.
- `SettingsRestControllerTest`, `SettingsPageTest` and `SettingsProductionCsrfTest` protect the composite
  route contract, the single-table UI and production authorization/CSRF boundary.
- `SettingsV9MigrationContractTest` protects the new migration and confirms V1–V8 remain present and
  unmodified in the migration chain.
- Stage 10 remains in progress; this follow-up contains no production action or external release evidence.

## Reproduction

1. Start Docker Desktop or provide an isolated PostgreSQL instance.
2. Start that isolated instance on an unused host port.
3. Export `TEST_DB_URL=jdbc:postgresql://localhost:55442/shiftcounter_test`,
   `TEST_DB_USERNAME=shift_test`, the test password and the matching production-isolation variables.
4. Run `.\mvnw.cmd clean verify`.
5. Confirm zero failures/errors, Flyway V1–V9 and logs for `shiftcounter_test`/`shift_test` with no
   `[MODBUS] Initialization` message.

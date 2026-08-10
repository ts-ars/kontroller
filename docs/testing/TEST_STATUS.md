# Test Status

## Environment

- Date: 2026-08-10
- Branch: `codex/stage-6-sensors`, based on merged `main` commit `4cfeb9a`
- Spring profile: `test`
- Required database: `shiftcounter_test`
- Required database user: `shift_test`
- Production database access: denied by provisioning policy and verified by `TestEnvironmentIsolationIT`
- Java: 21.0.5
- Maven Wrapper: 3.9.9

## Latest verified commands

| Command | Result |
|---|---|
| `.\mvnw.cmd clean test-compile -DskipTests` | PASS |
| Stage 6 focused unit suite | PASS — 11 executed, 0 skipped/failures/errors |
| Stage 6 end-to-end registration scenario | PASS — duplicate no-op and two-sensor isolation |
| First final `.\mvnw.cmd clean verify` | PASS — 109 discovered, 106 executed, 3 skipped, 0 failures/errors; 9:10 |
| Second consecutive `.\mvnw.cmd clean verify` | PASS — same 109/106/3 totals, 0 failures/errors; 3:08 |

Stage 6 verification used PostgreSQL 15.13 in the isolated Docker project
`kontroller-stage6-final`, volume `kontroller-stage6-final_pgdata` and host port `55436`. The
user project container `shift-postgres` on port `5432` was not modified or used.

## Latest test totals

- Surefire: 98 discovered, 97 executed, 0 failures, 0 errors, 1 skipped.
- Failsafe: 11 discovered, 9 executed, 0 failures, 0 errors, 2 skipped.
- Combined: 109 discovered, 106 executed, 0 failures, 0 errors, 3 intentionally disabled.
- Load test: excluded from standard `verify` and classified with JUnit tag `load`.

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
- `StoppageMigrationRehearsalIT` migrates a separate schema through historical V1–V6 data,
  verifies deterministic valid-row backfill, `primary` to `sensor-1`, the six-row catalog and
  migration reports for invalid legacy rows, then removes the schema.

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
- `AdamEventEmitterTest` protects the production path through the signal input port so a detected
  timestamp is persisted before the domain event is published.
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

## Intentionally disabled tests

1. `ShiftSettingsApplierIntegrationTest` — settings application remains Stage 8.
2. `ShiftSettingsDeliveryGuaranteeTest` — settings delivery remains Stage 8.
3. `ShiftSettingsIntegrationTest` — settings update integration remains Stage 8.

## Stage 6 status

**COMPLETE under the approved Stage 6 boundary.** Six stable sensors, persisted group ownership,
sensor-aware Shift/Signal/Reconcile/API/event facts, historical `sensor-1` migration and sequential
source-identity idempotency are covered and passing. Atomic Signal/Actual/Reconcile commit,
same-sensor concurrent input, partial-failure recovery and counter delta remain Stage 7; group settings
and their recalculation behavior remain Stage 8.

## Reproduction

1. Start Docker Desktop or provide an isolated PostgreSQL 15 instance.
2. Export the three compose passwords and run `docker compose --project-name kontroller-stage6-final up -d`
   on a fresh volume with `POSTGRES_PORT=55436`.
3. Export `TEST_DB_URL=jdbc:postgresql://localhost:55436/shiftcounter_test`,
   `TEST_DB_USERNAME=shift_test`, the test password and the matching production-isolation variables.
4. Run `.\mvnw.cmd clean verify`.
5. Confirm 109 discovered / 106 executed / 3 skipped, zero failures/errors, Flyway V1–V6 and logs for
   `shiftcounter_test`/`shift_test` with no `[MODBUS] Initialization` message.

# Test Status

## Environment

- Date: 2026-08-10
- Branch: `codex/stage-5-time`, based on merged `main` commit `9c2923b`
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
| Final review-focused Stage 5 suite | PASS — 15 executed, 0 skipped/failures/errors |
| First `.\mvnw.cmd clean verify` | PASS — 101 discovered, 97 executed, 4 skipped, 0 failures/errors; 6:11 |
| Second consecutive `.\mvnw.cmd clean verify` | PASS — same 101/97/4 totals, 0 failures/errors; 6:04 |

Stage 5 verification used PostgreSQL 15.13 in the isolated Docker project
`kontroller-stage5-final`, volume `kontroller-stage5-final_pgdata` and host port `55435`. The
user project container `shift-postgres` on port `5432` was not modified or used.

## Latest test totals

- Surefire: 90 discovered, 88 executed, 0 failures, 0 errors, 2 skipped.
- Failsafe: 11 discovered, 9 executed, 0 failures, 0 errors, 2 skipped.
- Combined: 101 discovered, 97 executed, 0 failures, 0 errors, 4 intentionally disabled.
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
- `StoppageMigrationRehearsalIT` migrates a separate schema through historical V1–V5 data,
  verifies deterministic valid-row backfill and migration reports for invalid legacy rows, then
  removes the schema.

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

## Intentionally disabled tests

1. I5 — physical signal idempotency remains assigned to Stages 6–7.
2. `ShiftSettingsApplierIntegrationTest` — settings application remains Stage 8.
3. `ShiftSettingsDeliveryGuaranteeTest` — settings delivery remains Stage 8.
4. `ShiftSettingsIntegrationTest` — settings update integration remains Stage 8.

## Stage 5 status

**COMPLETE under the approved Stage 5 boundary.** Production-date and interval resolution are unified;
cross-midnight, exact `07:00`, final interval, plan-required extension, timestamp redistribution,
injected Clock and half-open signal reads are covered and passing. Physical signal identity and six
sensors remain Stage 6; atomicity/counter input remains Stage 7; settings groups remain Stage 8.

## Reproduction

1. Start Docker Desktop or provide an isolated PostgreSQL 15 instance.
2. Export the three compose passwords and run `docker compose --project-name kontroller-stage5-final up -d`
   on a fresh volume with `POSTGRES_PORT=55435`.
3. Export `TEST_DB_URL=jdbc:postgresql://localhost:55435/shiftcounter_test`,
   `TEST_DB_USERNAME=shift_test`, the test password and the matching production-isolation variables.
4. Run `.\mvnw.cmd clean verify`.
5. Confirm 101 discovered / 97 executed / 4 skipped, zero failures/errors, Flyway V1–V5 and logs for
   `shiftcounter_test`/`shift_test` with no `[MODBUS] Initialization` message.

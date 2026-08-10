# Test Status

## Environment

- Date: 2026-08-07
- Branch: local `codex/stage-3-stoppage-model`, stacked on Stage 2 commit `708c31d` / draft PR #6
- Spring profile: `test`
- Required database: `shiftcounter_test`
- Required database user: `shift_test`
- Production database access: denied by provisioning policy and verified by `TestEnvironmentIsolationIT`
- Java: 21.0.5
- Maven Wrapper: 3.9.9

## Latest verified commands

| Command | Result |
|---|---|
| `.\\mvnw.cmd clean test-compile -DskipTests` | PASS |
| `.\\mvnw.cmd '-Dtest=StoppageModelTest,StoppageRoundingTest,LossExplanationServiceTest,Stage3DomainArchitectureTest' test` | PASS — 15 tests |
| `.\\mvnw.cmd '-Dit.test=StoppageMigrationRehearsalIT' failsafe:integration-test failsafe:verify` | PASS — 1 migration rehearsal |
| `.\\mvnw.cmd clean verify` | PASS by completed Surefire/Failsafe reports — 64 discovered, 57 executed |

Stage 3 verification uses PostgreSQL 15.13 in Docker project `kontroller-stage3`, volume `kontroller-stage3_pgdata` and host port `55433`. The existing user project container `shift-postgres` on port `5432` is not modified or used.

## Latest test totals

- Surefire: 56 discovered, 51 executed, 0 failures, 0 errors, 5 skipped.
- Failsafe: 8 discovered, 6 executed, 0 failures, 0 errors, 2 skipped.
- Unique intentionally disabled tests: 7.
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
- `TestEnvironmentIsolationIT` verifies the active profile, JDBC catalog/user, Flyway history, bean gating and denial of `shift_test` access to production.
- Spring integration tests use a centralized database reset listener.
- `StoppageMigrationRehearsalIT` migrates a separate schema through historical V1–V4 data, verifies deterministic valid-row backfill and migration reports for invalid legacy rows, then removes the schema.

## Known defects represented by disabled protection tests

1. I2 — non-negative interval loss requires unified Reconcile (Stage 4).
2. I3 — FIXED + TEMPO balance requires unified Reconcile (Stage 4).
3. I4 — stable preservation/relinking of explanations during Reconcile requires Stage 4.
4. I5 — physical signal idempotency requires Stages 6–7.

These tests remain intentionally disabled. Stage 3 supplies the stoppage identity, state, exact-time model, aggregate persistence and migration boundary; it does not implement Stage 4 matching or later business stages merely to make protection tests green.

Additional existing tests remain disabled until their approved stages:

- 3 settings/application-delivery tests — Stage 8.

I1 is active and passing. Stage 3 adds active aggregate, rounding, architecture, optimistic-concurrency, persistence and migration-rehearsal coverage.

## Stage 3 status

**COMPLETE under the approved Stage 3 boundary.** The full suite passes against isolated PostgreSQL. Stable update-in-place matching, unified Reconcile and I4 explanation preservation/relinking remain explicitly assigned to Stage 4.

## Reproduction

1. Start Docker Desktop or provide an isolated PostgreSQL 15 instance.
2. Export `POSTGRES_SUPERUSER_PASSWORD`, `SHIFT_PROD_DB_PASSWORD`, `SHIFT_TEST_DB_PASSWORD` and run `docker compose --project-name kontroller-stage3 up -d` on a fresh volume. Set `SHIFT_DB_PORT=55433` to avoid the existing project database on `5432`.
3. Export `TEST_DB_URL=jdbc:postgresql://localhost:55433/shiftcounter_test`, `TEST_DB_USERNAME=shift_test`, `TEST_DB_PASSWORD` and `PROD_DB_URL=jdbc:postgresql://localhost:55433/shiftcounter`, then run `.\\mvnw.cmd clean verify`.
4. Confirm the reports contain 0 failures/errors and the logs contain `shiftcounter_test`/`shift_test` with no `[MODBUS] Initialization` message.

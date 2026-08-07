# Test Status

## Environment

- Date: 2026-08-07
- Branch: local `main` Stage 2 change set based on `b861d25d5a0bb182cdb1fd4edcf622f0b13af6bb`
- Spring profile: `test`
- Required database: `shiftcounter_test`
- Required database user: `shift_test`
- Production database access: denied by provisioning policy and verified by `TestEnvironmentIsolationIT`
- Java: 21.0.5
- Maven Wrapper: 3.9.9

## Latest verified commands

| Command | Result |
|---|---|
| `.\\mvnw.cmd -DskipTests compile` | PASS |
| `.\\mvnw.cmd test-compile -DskipTests` | PASS |
| `.\\mvnw.cmd clean verify` (Stage 2 run 1) | PASS — 3 min 53 s |
| `.\\mvnw.cmd clean verify` (Stage 2 run 2) | PASS — 4 min 27 s |

Stage 2 verification uses PostgreSQL 15.13 in Docker project `kontroller-stage2`, volume `kontroller-stage2_pgdata` and host port `55432`. The existing user project container `shift-postgres` on port `5432` is not modified or used.

## Latest test totals

Both clean runs produced the same totals:

- Surefire: 49 discovered, 44 executed, 0 failures, 0 errors, 5 skipped.
- Failsafe: 5 discovered, 3 executed, 0 failures, 0 errors, 2 skipped.
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

## Known defects represented by disabled protection tests

1. I2 — non-negative interval loss requires unified Reconcile (Stage 4).
2. I3 — FIXED + TEMPO balance requires unified Reconcile (Stage 4).
3. I4 — stable preservation/relinking of explanations during Reconcile requires Stage 4.
4. I5 — physical signal idempotency requires Stages 6–7.

These tests are intentionally disabled; Stage 2 does not implement later business stages merely to make them green.

Additional existing tests are explicitly disabled until their approved stages:

- 3 settings/application-delivery tests — Stage 8.

I1 is active and passing. Stage 2 replaces the three stale disabled comment-flow tests with active domain, application, web and persistence tests.

## Stage 2 status

**COMPLETE.** Two consecutive clean full runs passed with identical totals against the isolated PostgreSQL environment. `IMPLEMENTATION_STATUS.md` records Stage 2 as `DONE` under the approved Stage 2/3/4 boundary.

## Reproduction

1. Start Docker Desktop or provide an isolated PostgreSQL 15 instance.
2. Export `POSTGRES_SUPERUSER_PASSWORD`, `SHIFT_PROD_DB_PASSWORD`, `SHIFT_TEST_DB_PASSWORD` and run `docker compose --project-name kontroller-stage2 up -d` on a fresh volume. The default host port is `55432` to avoid the existing project database on `5432`.
3. Export `TEST_DB_PASSWORD` and `PROD_DB_URL`, then run `.\\mvnw.cmd clean verify` twice.
4. Confirm the logs contain `shiftcounter_test`/`shift_test` and no `[MODBUS] Initialization` message.

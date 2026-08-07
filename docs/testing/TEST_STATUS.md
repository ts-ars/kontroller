# Test Status

## Environment

- Date: 2026-08-07
- Branch: `codex/stage-1-test-environment-verified`
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
| `.\\mvnw.cmd clean verify` (run 1) | PASS — 4 min 09 s |
| `.\\mvnw.cmd clean verify` (run 2) | PASS — 4 min 07 s |

Both clean runs used PostgreSQL 15.13 in Docker project `kontroller-stage1`, volume `kontroller-stage1_pgdata` and host port `55432`. The existing user project container `shift-postgres` on port `5432` was not modified or used.

## Latest test totals

Both clean runs produced the same totals:

- Surefire: 41 discovered, 32 executed, 0 failures, 0 errors, 9 skipped.
- Failsafe: 4 discovered, 2 executed, 0 failures, 0 errors, 2 skipped.
- Unique intentionally disabled tests: 11.
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

1. I1 — Shift can currently accept a total actual inconsistent with hourly values (Stage 0 follow-up before Stage 2).
2. I2 — non-negative interval loss requires unified Reconcile (Stage 4).
3. I3 — FIXED + TEMPO balance requires unified Reconcile (Stage 4).
4. I4 — preserving operator explanation fields requires Stages 2–4.
5. I5 — physical signal idempotency requires Stages 6–7.

These tests are intentionally disabled; Stage 1 does not implement later business stages merely to make them green.

Additional existing tests are explicitly disabled until their approved stages:

- 3 comment-flow tests — Stage 2.
- 3 settings/application-delivery tests — Stage 8.

## Stage 1 status

**COMPLETE.** Two consecutive clean full runs passed with identical totals against the isolated PostgreSQL environment. `IMPLEMENTATION_STATUS.md` records Stage 1 as `DONE`.

## Reproduction

1. Start Docker Desktop or provide an isolated PostgreSQL 15 instance.
2. Export `POSTGRES_SUPERUSER_PASSWORD`, `SHIFT_PROD_DB_PASSWORD`, `SHIFT_TEST_DB_PASSWORD` and run `docker compose --project-name kontroller-stage1 up -d` on a fresh volume. The default host port is `55432` to avoid the existing project database on `5432`.
3. Export `TEST_DB_PASSWORD` and `PROD_DB_URL`, then run `.\\mvnw.cmd clean verify` twice.
4. Confirm the logs contain `shiftcounter_test`/`shift_test` and no `[MODBUS] Initialization` message.

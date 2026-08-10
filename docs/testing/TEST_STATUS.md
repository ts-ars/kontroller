# Test Status

## Environment

- Date: 2026-08-10
- Branch: local `codex/stage-4-unified-reconcile`, based on merged `main` commit `776d82f`
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
| Focused Stage 4 unit/architecture suite | PASS — 29 discovered, 28 executed, 1 skipped (I5) |
| Targeted `StoppageReconcilePersistenceIT` | PASS — 3 PostgreSQL scenarios |
| `.\mvnw.cmd clean verify` | PASS — 83 discovered, 79 executed, 4 skipped, 0 failures/errors; 7:31 |

Stage 4 verification used PostgreSQL 15.13 in the isolated Docker project
`kontroller-stage4`, volume `kontroller-stage4_pgdata` and host port `55434`. The
user project container `shift-postgres` on port `5432` was not modified or used.

## Latest test totals

- Surefire: 72 discovered, 70 executed, 0 failures, 0 errors, 2 skipped.
- Failsafe: 11 discovered, 9 executed, 0 failures, 0 errors, 2 skipped.
- Combined: 83 discovered, 79 executed, 0 failures, 0 errors, 4 intentionally disabled.
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

## Intentionally disabled tests

1. I5 — physical signal idempotency remains assigned to Stages 6–7.
2. `ShiftSettingsApplierIntegrationTest` — settings application remains Stage 8.
3. `ShiftSettingsDeliveryGuaranteeTest` — settings delivery remains Stage 8.
4. `ShiftSettingsIntegrationTest` — settings update integration remains Stage 8.

## Stage 4 status

**COMPLETE under the approved Stage 4 boundary.** One use case now owns all current loss write
triggers; calculation balance, deterministic matching, stable identities, explanation preservation,
idempotence, concurrent serialization and V5 migration are covered and passing. Production-day and
cross-midnight semantics remain Stage 5; physical signal identity/atomicity remains Stages 6–7; and
settings-group recalculation remains Stage 8.

## Reproduction

1. Start Docker Desktop or provide an isolated PostgreSQL 15 instance.
2. Export the three compose passwords and run `docker compose --project-name kontroller-stage4 up -d`
   on a fresh volume with `SHIFT_DB_PORT=55434`.
3. Export `TEST_DB_URL=jdbc:postgresql://localhost:55434/shiftcounter_test`,
   `TEST_DB_USERNAME=shift_test`, the test password and the matching production-isolation variables.
4. Run `.\mvnw.cmd clean verify`.
5. Confirm 83 discovered / 79 executed / 4 skipped, zero failures/errors, Flyway V1–V5 and logs for
   `shiftcounter_test`/`shift_test` with no `[MODBUS] Initialization` message.

# Test Status

Status: **GREEN**
Roadmap stage: **Stage 1 — Stable Test Environment**
Observed: **2026-08-07 (Europe/Warsaw)**

## Environment

- Java: Eclipse Temurin 21.0.12
- Spring Boot: 3.1.5
- Database used by tests: PostgreSQL 14.22
- JDBC target: `jdbc:postgresql://localhost:5433/shiftcounter_test`
- Database user: `shift_test`
- Spring profile: `test`
- Schema management: Flyway V1 and V2; Hibernate `ddl-auto=validate`
- ADAM: disabled by the `prod` profile gate; no ADAM initialization appeared in either full-run log
- Test logging: console only; no files were created under `logs/`

The verification runner did not provide Docker and runs as root. For this run only, `SHIFT_TEST_EMBEDDED=true` selected the test-only embedded PostgreSQL provisioner. A temporary, uncommitted process-identity shim allowed the PostgreSQL binary to run in the restricted root sandbox; the committed test provisioner uses TCP only because this sandbox forbids PostgreSQL Unix sockets. The identity shim is not part of the repository.

## Commands

Canonical full check, with the three `SHIFT_TEST_DB_*` variables supplied by the environment:

```bash
SHIFT_TEST_EMBEDDED=true ./mvnw -B clean verify
```

The observed runs additionally selected the temporary Java 21 installation, local Maven mirror/cache and root-sandbox shim:

```bash
LD_PRELOAD=/tmp/codex-nonroot-identity.so \
SHIFT_TEST_DB_URL=jdbc:postgresql://localhost:5433/shiftcounter_test \
SHIFT_TEST_DB_USER=shift_test \
SHIFT_TEST_DB_PASSWORD='<ephemeral test secret>' \
SHIFT_TEST_EMBEDDED=true \
JAVA_HOME=/tmp/temurin21 \
MAVEN_USER_HOME=/workspace/scratch/012aae219448/.m2 \
./mvnw -B -s /tmp/codex-maven-local-mirror.xml \
  -Dmaven.repo.local=/workspace/scratch/012aae219448/.m2/repository \
  clean verify
```

## Consecutive full-run result

| Run | Surefire | Failsafe E2E | Failures | Errors | Result | Duration |
|---|---:|---:|---:|---:|---|---:|
| 1 | 43 passed, 8 disabled | 3 passed | 0 | 0 | BUILD SUCCESS | 1:11 |
| 2 | 43 passed, 8 disabled | 3 passed | 0 | 0 | BUILD SUCCESS | 56.044 s |

Both clean runs produced the same test counts and result. The database environment integration test also confirmed the active `test` profile, catalog `shiftcounter_test`, user `shift_test`, and failure when the test credentials attempted to open `shiftcounter_prod`.

## Classification

Runnable tests are tagged `unit`, `integration`, `web`, or `e2e`. Surefire runs the normal suite and excludes only `e2e`; Failsafe runs `*E2ETest` with the `e2e` tag during `verify`.

## Explicitly disabled known defects

Disabled means the test describes approved future-stage behavior that the current implementation does not provide. It is counted and documented; it is not an unexplained skipped or red test.

| Test | Future stage | Known defect |
|---|---|---|
| `CommentFlowWebTest.fullCommentFlow` | 2 | Approved `LossExplanation` API and range-query contract are not implemented. |
| `CommentAlertMissingWebTest.alertsShownForMissingComments` | 2 | Legacy alerts aggregate by interval instead of matching approved explanations. |
| `CommentMultiUpdateWebTest.multipleStoppages_independentUpdates` | 2–3 | Independent stoppage explanations require the approved `Stoppage`/`LossExplanation` model. |
| `Stage0InvariantProtectionTest.i1ShiftActualEqualsSumOfHourlyActualValues` | 6 | `Shift` accepts a total actual inconsistent with its hourly actuals. |
| `Stage0InvariantProtectionTest.i2IntervalLossIsNeverNegativeAndUsesFullPlan` | 4 | Current-interval TEMPO is still proportional to elapsed time. |
| `Stage0InvariantProtectionTest.i3FixedPlusTempoEqualsIntervalLoss` | 4 | FIXED is not bounded by total loss before TEMPO is derived. |
| `Stage0InvariantProtectionTest.i4RecalculationPreservesOperatorCategoryCommentAndAllocatedMinutes` | 2–4 | Reconcile has no `LossExplanation` model whose operator-owned fields can be preserved. |
| `Stage0InvariantProtectionTest.i5OnePhysicalSignalCausesOneActualIncrement` | 6 | Signals have no source identity, so duplicate physical input is persisted twice. |

## Test isolation controls

- The `test` environment guard rejects any JDBC database other than `shiftcounter_test` and any user other than `shift_test` before the application context starts.
- Production and test compose profiles use separate PostgreSQL services, ports, volumes, databases and least-privilege roles.
- Central cleanup truncates mutable business tables, resets identities and restores canonical settings after database-backed tests.
- HTTP signal simulation is gated to `test`; ADAM adapters and emitters are gated to `prod`.

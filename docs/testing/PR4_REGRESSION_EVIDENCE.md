# PR 4 — Integrated Regression Evidence

## Scope and base

- Branch: `codex/pr4-regression`.
- Required integration base: `16d755e088806044e8531b818eb9408b7e76b6c0`.
- Original protected ancestor: `7a083371c84dc3bd6aca9ff62ae33b505abafe87`.
- V1–V8 were not edited. V9 is exercised only through tests and rehearsal expectations.
- No production action, production database access, deployment fix, push or PR creation is part of
  this evidence.

## Coverage matrix

| Contract | Executable evidence | Result in this environment |
|---|---|---|
| Six stable sensor IDs; independent Signal and Actual | `Pr4SixSensorRegressionIT`, `Stage6SignalRegistrationIntegrationTest`, `ShiftSensorIsolationTest` | PASS |
| Independent stoppages for Sensors 1–4/6 | `Pr4SixSensorRegressionIT`, existing reconcile persistence tests | PASS |
| Sensor 5 has no own stoppage/explanation workflow | `Pr4SixSensorRegressionIT`, `StoppageReconcilesServiceTest`, `LossExplanationServiceTest` | PASS |
| Sensor 5 aggregates multiple explanations from Sensors 1–4 with source | `Pr4SixSensorRegressionIT`, `StoppageShiftExplanationAdapterTest`, `CommentsReadServiceTest` | PASS |
| Sensor 6 comments and loss remain independent | `Pr4SixSensorRegressionIT`, projection/comments tests | PASS |
| Day and evening slices, including after 23:00 and after midnight | `Pr4SixSensorRegressionIT`, `ShiftSliceTest`, `ShiftProjectionUseCaseTest`, `CommentsPageTest` | PASS |
| Composite Settings route, CSRF and active-shift semantics | `SettingsRestControllerTest`, `SettingsProductionCsrfTest`, `SettingsSnapshotIntegrationTest` | PASS |
| Add/Delete half-tail rotation and reversibility | `SettingsSnapshotTest`, `Pr4SixSensorRegressionIT` | PASS; stored planless Actual tail is preserved after row deletion |
| Report inclusive production-date range and Sensor 5 four-source totals | `ReportQueryUseCaseTest`, `ReportPageMvcTest`, `Pr4SixSensorRegressionIT` | PASS |
| Sensor-scoped WebSocket updates and after-commit/rollback behavior | `CommentsUpdatedListenerTest`, `CommentsAfterCommitPublicationTest`, `ShiftUpdatedListenerTest` | PASS |
| Historical migration through V9 | `StoppageMigrationRehearsalIT`, `SettingsV9MigrationContractTest` | PASS |
| Isolated clean verification | `mvnw clean verify` with `shiftcounter_test` / `shift_test` | PASS — 209 tests, 0 failures/errors/skips; 10:42 |
| Load tests | `Stage10SixSensorLoadTest`, `ShiftProductRegistrarMultiHourLoadTest` | NOT COMPLETED — separate run exceeded 20:04 and emitted no completed report |

The focused database-free command executed 58 tests with zero failures, errors or skips. A subsequent
isolated PostgreSQL 15.13 run in Docker project `kontroller-pr4-postgres` on host port `55442` executed
197 Surefire and 12 Failsafe tests with zero failures, errors or skips. The separate load command was
terminated by the local 20-minute command limit before either scenario emitted a completed report, so
load remains explicitly unverified in this environment.

## Mockup comparison

| Page | Approved reference | Source-level result |
|---|---|---|
| Plan–Fact | `source-faithful-plan-fact.html` | Matches 1632px workspace, two-column sensor grid, 600×190 chart frame, 88px Time/Plan/Actual columns, solid `#3b82f6`, top legend and separate comment lines |
| Comments | `comments-layout.html` | Matches 1400px workspace, editor widths, centered Minutes, readonly Allocated cans and Sensor 5 source grid 2×2 |
| Settings | `settings-layout.html` | Matches 1000px table, shared/derived/independent columns, readonly Sensor 5, totals and tail controls |
| Report | `report-layout.html` | Matches 1400px workspace, Type 26%, 190px charts and two-column Sensor 5 chart layout; PR 4 removed the ordinary-sensor signal chart and chart borders |

The in-app browser refused local `file://` navigation. Therefore this comparison validates source
structure and styling, not computed browser layout, CDN rendering or pixel equivalence. Browser visual
sign-off remains an external/manual check when a permitted render surface is available.

## External Stage 10 gates

Stage 10 remains **IN PROGRESS**. This PR does not satisfy or claim any of these external gates:

- real production backup and restore rehearsal with retained evidence;
- production migration rehearsal on an approved database copy;
- secret provisioning/rotation and least-privilege operational approval;
- network, TLS and production monitoring configuration/evidence;
- six physical ADAM device smoke test;
- counter-running restart/recovery evidence;
- database-outage and recovery validation;
- production artifact deployment, rollback exercise and operational approval.

Deployment gaps for `/app/logs` ownership and Java preview runtime flags remain outside this change set.

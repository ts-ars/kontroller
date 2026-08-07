# Stage 10 вЂ” Production Release

Status: **APPROVED / NOT IMPLEMENTED**

Stage 10 does not change business rules. It prepares a reproducible, recoverable production operation.

## Environment and data

- Production and test profiles, databases and users are separate.
- Production starts explicitly with `prod`; ADAM is enabled, HTTP simulation/debug/test fixtures are absent.
- Startup validates required profile, DB target, secrets and device configuration and fails visibly when required production configuration is missing.
- Secrets are supplied through environment or secret storage and never committed. Any secret previously committed is treated as compromised and rotated; deleting only the current line is insufficient.
- Flyway is the only schema owner; Hibernate uses `validate`; applied migrations are never edited.
- Production migrations are rehearsed on a database copy, preserve comments/explanations and produce a reconciliation report for ambiguous legacy data.

## Counter and recovery

- Persist per-sensor counter state (`lastCounterValue`, last read time and continuity state).
- Restart recovery processes accumulated ADAM delta instead of replacing the baseline.
- Counter discontinuity is logged/monitored and does not apply an invented increment.
- Device connection, timeout, polling, retry and enablement settings are externalized.
- Temporary loss of one ADAM does not crash the whole application; reconnection compares current and persisted counter state.

## Operations and security

- Regular PostgreSQL backups exist and restore is tested into a separate database.
- Deployment, recovery, backup/restore, ADAM and environment procedures are documented without secrets.
- Liveness, readiness and a separate ADAM health view are available; Actuator administration is not publicly exposed.
- Structured logs include sensor/counter/delta/production-date/interval/correlation/result context and exclude secrets.
- Monitor application, DB, each sensor poll, ADAM state, disk, Reconcile and transaction failures.
- External traffic uses HTTPS through a reverse proxy. PostgreSQL, ADAM, internal app and management ports are not exposed publicly.
- At least operator and admin permissions are separated; critical manual changes are auditable.

## Build and release

- CI runs compile, unit, integration, ArchUnit, migration tests and packaging; failures block release.
- Deploy a versioned immutable artifact with version, commit and build timeвЂ”not an ad hoc production source build.
- Release procedure: verified backup в†’ deploy в†’ Flyway в†’ start в†’ health check в†’ controlled smoke test.
- A rollback procedure is known before deploy; migrations are backward-compatible where practical.
- Validate parallel input from six sensors, high concurrency on one sensor, no lost/duplicate increments, deadlocks or duplicate losses.
- Perform application-restart-with-counter-running and temporary-database-outage tests.
- Graceful shutdown stops new work and allows active transactions to finish within the platform's shutdown window.

## Definition of Done

Production/test separation, external secrets, least-privilege DB, Flyway/validate, verified backup and recovery, safe counter restart/discontinuity, disabled production simulation, health/logging/monitoring, network protection/HTTPS, role separation, green CI, immutable artifact, rehearsed migration, six-sensor load test, restart test, smoke test, rollback procedure, operational documents and the final Stage 0вЂ“9 audit are all complete.


# Production Runbook

Stage 10 release operations are approval gates. Never copy values from `.env.example` into production.
Credentials belong in the deployment platform's secret store and must be rotated if they have ever
appeared in Git history, logs or tickets.

## Required topology

- Terminate HTTPS at a maintained reverse proxy. Expose only TCP 443 externally.
- Bind the application and PostgreSQL to private networks. The management listener binds to
  `127.0.0.1:${MANAGEMENT_PORT:-8081}` and must not be forwarded publicly.
- Allow the application host to reach only PostgreSQL and the six configured ADAM endpoints.
- Use the `shift_prod` role only for the application. Keep the PostgreSQL superuser outside the app.

## Release gate

1. Record release version, commit SHA, artifact checksum and approver.
2. Complete a verified backup and restore it into a separate database as described in
   [BACKUP_RESTORE.md](BACKUP_RESTORE.md).
3. Rehearse Flyway against the restored copy. Save the Flyway output and review migration-report
   tables for ambiguous legacy rows. Do not edit an applied migration.
4. Verify required secrets and all six ADAM mappings without printing their values.
5. Deploy the CI-produced immutable JAR/container. Do not build from a production checkout.
6. Start with the explicit `prod` profile. Startup must reject the wrong database, role or missing
   credentials/device configuration.
7. Check `/actuator/health/liveness`, `/actuator/health/readiness` and
   `/actuator/health/adam` through the private management listener.
8. Run one controlled counter increment per sensor and verify exactly one Signal, one Actual increment
   and no duplicate loss. Record the production date and interval.
9. Observe DB, disk, transaction/Reconcile errors, each sensor poll and ADAM state for the agreed
   stabilization window.

## Recovery and rollback

- Application restart: leave counters running. On reconnect, the persisted counter baseline applies
  only the accumulated positive delta. A lower counter is a discontinuity and must be investigated;
  never invent an increment or manually advance the baseline.
- Temporary ADAM outage: the other devices continue polling. Restore connectivity and confirm the
  reconnect delta against the persisted state.
- Temporary database outage: stop release traffic if readiness fails, restore DB connectivity, then
  verify transaction rollback and counter recovery before resuming.
- Application rollback: redeploy the previous immutable artifact only when its code is compatible
  with the current Flyway schema. Database rollback means restore into a separate database and a
  separately approved cutover; never use `flyway clean` or edit migration history.
- If smoke tests fail, stop new work, retain logs/audit evidence, apply the known rollback and repeat
  health checks before reopening traffic.

## Shutdown

Use the platform's normal termination signal. The application stops accepting new work and gives
active transactions 30 seconds to complete. Do not kill the process before that window unless the
incident commander explicitly accepts possible recovery work.

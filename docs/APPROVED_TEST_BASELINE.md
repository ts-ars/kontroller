# Approved test baseline

Recorded: 2026-08-14
Server: `tsar@192.168.1.74`
Application URL: `http://192.168.1.74:8080`
Environment: test only

## Source

- Repository: `ts-ars/kontroller`
- Branch origin: `codex/report-layout-fix`
- Base HEAD: `c60868f1aa734bb984d4eaf5d9ed5869a3635dc3`
- The approved test source contains uncommitted live fixes on top of that HEAD.
- No commit or push was made while recording this baseline.

## Runtime

- Application container: `kontroller-test-app`
- Database container: `kontroller-test-db`
- Approved image: `kontroller-test:c60868f-livefix41-report-delete-dialog`
- Stable baseline tag: `kontroller-test:approved-2026-08-14`
- Source snapshot: `/home/tsar/kontroller-test/baselines/approved-2026-08-14/kontroller-test-source-c60868f-livefix41.tar.gz`
- Source snapshot SHA-256: `b7392249c8b8f08ff62bd36b96f19a3cb43c255e498cd0046cee69e934a16f84`
- Database: `shiftcounter_test` on test PostgreSQL port 55432
- Active profiles: `test,test-adam`
- Application port: 8080
- Flyway: V14 `allow manual stoppage detection`

## Rollback

- Immediate previous application container: `kontroller-test-app-rollback-livefix40-manual-live-preview`
- Previous image: `kontroller-test:c60868f-livefix40-manual-live-preview`
- Test DB backup before manual-stoppage schema/formula correction: `/home/tsar/kontroller-test/backups/pre-manual-save-fix-.dump`
- Earlier signal-state backup: `/home/tsar/kontroller-test/backups/pre-signal-identity-fix37-20260814.dump`

Rollback application procedure:

1. Stop `kontroller-test-app`.
2. Preserve the stopped current container under a failure-specific name.
3. Rename `kontroller-test-app-rollback-livefix40-manual-live-preview` to `kontroller-test-app`.
4. Start `kontroller-test-app`.
5. Verify `/actuator/health`, logs and sensor-5 processing.

Database restore is not part of ordinary application rollback. Restore the test DB only when explicitly required and after preserving the current test DB.

## Required verification

- `/actuator/health` returns `UP` after ADAM reconnects.
- Flyway schema is at V14.
- Test application uses only test DB credentials and configuration.
- Sensor 5 continues accepting physical signals when the device is producing them.
- Report uses the approved column widths and chart grouping.
- Report Delete opens the centered application dialog on the same page.
- Manual stoppage Save succeeds.
- After a MANUAL stoppage is saved, its Add reason button opens the interval selector again so another MANUAL stoppage can be created in a different available interval.
- Add reason on a system stoppage still adds an explanation to that stoppage and does not open manual interval selection.
- Plan 600 / 15 minutes shows and saves 150 lost cans.
- No production container, database, network, secret or data is changed.

The behavioral source of truth is `docs/APPROVED_BEHAVIOR.md`.

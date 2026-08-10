# PostgreSQL Backup and Restore Rehearsal

Run these procedures from an approved administration host. Supply credentials through `.pgpass` or
the platform secret mechanism; never put passwords on a command line, in this document or in logs.

## Backup

```sh
pg_dump --format=custom --no-owner --no-acl --dbname="$PROD_ADMIN_DB_URL" --file=shiftcounter.dump
pg_restore --list shiftcounter.dump > shiftcounter.dump.manifest
sha256sum shiftcounter.dump > shiftcounter.dump.sha256
```

Store the dump, manifest and checksum in encrypted, access-controlled backup storage. Retention and
schedule are deployment decisions; monitoring must alert on a missed or failed backup.

## Restore rehearsal

1. Create a separate, isolated rehearsal database and least-privilege owner. Never target production.
2. Verify the checksum, restore, and stop on the first error:

```sh
sha256sum --check shiftcounter.dump.sha256
pg_restore --exit-on-error --no-owner --no-acl --dbname="$RESTORE_DB_URL" shiftcounter.dump
```

3. Run the release artifact with Flyway against the restored copy, then stop it.
4. Verify Flyway checksums, row counts, explanations/comments, counter states and migration-report
   tables. Run the controlled reconciliation report; do not auto-resolve ambiguous legacy data.
5. Record elapsed restore time, artifact/commit, Flyway version, validation results and approver.
6. Destroy the rehearsal database only after evidence is retained according to policy.

A release is blocked until a recent restore rehearsal passes and its recovery time meets the platform
objective chosen by the operator.

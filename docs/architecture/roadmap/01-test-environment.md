# Stage 1 вЂ” Stable Test Environment

Status: **APPROVED / NOT IMPLEMENTED**

## Target state

- Separate Spring profiles: `prod` and `test`.
- Separate PostgreSQL databases: `shiftcounter_prod` and `shiftcounter_test`.
- Separate least-privilege users: `shift_prod` and `shift_test`.
- Tests cannot connect to the production database.
- Flyway creates the same schema in both environments; Hibernate does not create or update it.
- ADAM beans are disabled in `test`; HTTP simulation exists only in `test`.
- Test data cleanup is centralized and runs are independent of order and previous state.
- Tests are classified as unit, integration, web and E2E.
- Known future-stage defects are explicitly documented/disabled rather than left as unexplained red tests.
- `docs/testing/TEST_STATUS.md` records environment, command, result, passing/failing/disabled tests and known defects.

## Verification

- `clean verify` is the full reproducible check; the normal test command excludes only explicitly classified heavy E2E checks.
- Two consecutive clean full runs must produce the same result.
- Test logs must show the test JDBC target and no ADAM initialization attempts.

## Definition of Done

1. Production DB access from tests is physically denied.
2. All tests run with the test profile and isolated credentials.
3. Flyway owns schema creation; Hibernate validates it.
4. ADAM is absent and HTTP simulation is available only where intended.
5. Tests are deterministic, classified and named consistently; obsolete tests are explicit.
6. Every Stage 0 invariant has a protection test.
7. Two clean full runs match, and `TEST_STATUS.md` reflects the observed result.


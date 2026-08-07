#!/bin/sh
set -eu

psql --set=ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" \
  --set=prod_password="$SHIFT_PROD_DB_PASSWORD" \
  --set=test_password="$SHIFT_TEST_DB_PASSWORD" <<'SQL'
SELECT format('CREATE ROLE shift_prod LOGIN PASSWORD %L', :'prod_password')
WHERE NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'shift_prod') \gexec
SELECT format('CREATE ROLE shift_test LOGIN PASSWORD %L', :'test_password')
WHERE NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'shift_test') \gexec

SELECT 'CREATE DATABASE shiftcounter_prod OWNER shift_prod'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'shiftcounter_prod') \gexec
SELECT 'CREATE DATABASE shiftcounter_test OWNER shift_test'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'shiftcounter_test') \gexec

REVOKE CONNECT ON DATABASE shiftcounter_prod FROM PUBLIC;
GRANT CONNECT ON DATABASE shiftcounter_prod TO shift_prod;
REVOKE CONNECT ON DATABASE shiftcounter_test FROM PUBLIC;
GRANT CONNECT ON DATABASE shiftcounter_test TO shift_test;
SQL

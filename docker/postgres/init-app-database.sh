#!/bin/sh
set -eu

: "${APP_DB:?APP_DB is required}"
: "${APP_USER:?APP_USER is required}"
: "${APP_PASSWORD:?APP_PASSWORD is required}"

psql --set=ON_ERROR_STOP=1 \
    --username "$POSTGRES_USER" \
    --dbname postgres \
    --set=app_database="$APP_DB" \
    --set=app_user="$APP_USER" \
    --set=app_password="$APP_PASSWORD" <<'SQL'
SELECT format('CREATE ROLE %I LOGIN PASSWORD %L', :'app_user', :'app_password')
WHERE NOT EXISTS (SELECT FROM pg_roles WHERE rolname = :'app_user')
\gexec

SELECT format('CREATE DATABASE %I OWNER %I', :'app_database', :'app_user')
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = :'app_database')
\gexec

SELECT format('REVOKE ALL ON DATABASE %I FROM PUBLIC', :'app_database')
\gexec

SELECT format('GRANT CONNECT, TEMPORARY ON DATABASE %I TO %I', :'app_database', :'app_user')
\gexec
SQL

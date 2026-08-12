CREATE TABLE app_user (
    id UUID PRIMARY KEY,
    display_name VARCHAR(120) NOT NULL,
    pin_hash VARCHAR(255) NOT NULL,
    role VARCHAR(16) NOT NULL,
    status VARCHAR(16) NOT NULL,
    failed_attempts INTEGER NOT NULL DEFAULT 0,
    locked_until TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_app_user_display_name UNIQUE (display_name),
    CONSTRAINT ck_app_user_role CHECK (role IN ('USER', 'ADMIN', 'OWNER')),
    CONSTRAINT ck_app_user_status CHECK (status IN ('ACTIVE', 'BLOCKED')),
    CONSTRAINT ck_app_user_failed_attempts CHECK (failed_attempts >= 0)
);

CREATE INDEX ix_app_user_status ON app_user (status);

CREATE TABLE loss_explanations (
    id BIGSERIAL PRIMARY KEY,
    stoppage_id BIGINT NOT NULL,
    category VARCHAR(32) NOT NULL,
    comment TEXT NOT NULL DEFAULT '',
    allocated_minutes INTEGER NOT NULL,
    allocated_cans INTEGER NOT NULL,
    CONSTRAINT fk_loss_explanation_stoppage
        FOREIGN KEY (stoppage_id) REFERENCES stoppages(id) ON DELETE RESTRICT,
    CONSTRAINT ck_loss_explanation_category
        CHECK (category IN ('ORGANIZATION', 'BREAKDOWN', 'MATERIAL', 'QUALITY')),
    CONSTRAINT ck_loss_explanation_minutes CHECK (allocated_minutes >= 0),
    CONSTRAINT ck_loss_explanation_cans CHECK (allocated_cans >= 0)
);

CREATE INDEX ix_loss_explanations_stoppage_id ON loss_explanations(stoppage_id);

CREATE TABLE legacy_loss_explanation_migration_report (
    stoppage_id BIGINT PRIMARY KEY,
    shift_id BIGINT NOT NULL,
    hour_index INTEGER NOT NULL,
    legacy_category VARCHAR(32) NOT NULL,
    legacy_comment TEXT,
    legacy_minutes DOUBLE PRECISION NOT NULL,
    legacy_cans INTEGER NOT NULL,
    issue VARCHAR(128) NOT NULL
);

INSERT INTO legacy_loss_explanation_migration_report (
    stoppage_id, shift_id, hour_index, legacy_category, legacy_comment,
    legacy_minutes, legacy_cans, issue
)
SELECT id, shift_id, hour_index, type, reason, minutes, cans,
       'NO_UNAMBIGUOUS_PARENT_STOPPAGE'
FROM stoppages
WHERE type IN ('ORGANIZATION', 'BREAKDOWN', 'MATERIAL', 'QUALITY');

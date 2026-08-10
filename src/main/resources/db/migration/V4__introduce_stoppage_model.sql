ALTER TABLE stoppages
    ADD COLUMN detection_key UUID,
    ADD COLUMN detection_type VARCHAR(16),
    ADD COLUMN sensor_key VARCHAR(64),
    ADD COLUMN started_at TIMESTAMP,
    ADD COLUMN exact_duration_nanos BIGINT,
    ADD COLUMN rounded_minutes INTEGER,
    ADD COLUMN state VARCHAR(16),
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

ALTER TABLE loss_explanations
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

CREATE TABLE stoppage_model_migration_report (
    stoppage_id BIGINT PRIMARY KEY,
    issue VARCHAR(128) NOT NULL
);

INSERT INTO stoppage_model_migration_report(stoppage_id, issue)
SELECT s.id, 'MISSING_SHIFT_INTERVAL_LABEL'
FROM stoppages s
LEFT JOIN shift_hour_labels label
       ON label.shift_id = s.shift_id AND label.order_index = s.hour_index
WHERE s.type IN ('FIXED', 'TEMPO') AND label.shift_id IS NULL;

WITH interval_labels AS (
    SELECT label.shift_id,
           label.order_index,
           label.label::time AS interval_time,
           first_label.label::time AS first_interval_time
    FROM shift_hour_labels label
    JOIN shift_hour_labels first_label
      ON first_label.shift_id = label.shift_id AND first_label.order_index = 0
)
UPDATE stoppages s
SET detection_key = md5('legacy-stoppage:' || s.id)::uuid,
    detection_type = s.type,
    sensor_key = 'primary',
    started_at = sh.date + labels.interval_time
        + CASE WHEN labels.interval_time < labels.first_interval_time THEN INTERVAL '1 day' ELSE INTERVAL '0 day' END
        + COALESCE(s.minute_offset, 0) * INTERVAL '1 minute',
    exact_duration_nanos = ROUND(s.minutes * 60 * 1000000000)::BIGINT,
    rounded_minutes = FLOOR(s.minutes + 0.5)::INTEGER,
    state = 'ACTIVE'
FROM shift sh
JOIN interval_labels labels ON labels.shift_id = sh.id
WHERE s.shift_id = sh.id
  AND s.hour_index = labels.order_index
  AND s.type IN ('FIXED', 'TEMPO');

ALTER TABLE stoppages
    ADD CONSTRAINT ck_stoppage_detection_type
        CHECK (detection_type IS NULL OR detection_type IN ('FIXED', 'TEMPO')),
    ADD CONSTRAINT ck_stoppage_state
        CHECK (state IS NULL OR state IN ('ACTIVE', 'RESOLVED')),
    ADD CONSTRAINT ck_stoppage_exact_duration
        CHECK (exact_duration_nanos IS NULL OR exact_duration_nanos >= 0),
    ADD CONSTRAINT ck_stoppage_rounded_minutes
        CHECK (rounded_minutes IS NULL OR rounded_minutes >= 0),
    ADD CONSTRAINT ck_stoppage_system_fields
        CHECK ((detection_key IS NULL AND detection_type IS NULL)
            OR (detection_key IS NOT NULL AND detection_type IS NOT NULL
                AND sensor_key IS NOT NULL AND sensor_key <> ''
                AND started_at IS NOT NULL AND exact_duration_nanos IS NOT NULL
                AND rounded_minutes IS NOT NULL AND state IS NOT NULL));

CREATE UNIQUE INDEX ux_stoppages_detection_key
    ON stoppages(detection_key) WHERE detection_key IS NOT NULL;

CREATE INDEX ix_stoppages_active_interval
    ON stoppages(shift_id, sensor_key, hour_index, state)
    WHERE detection_key IS NOT NULL;

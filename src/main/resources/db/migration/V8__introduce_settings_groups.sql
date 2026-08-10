CREATE TABLE settings_groups (
    id VARCHAR(64) PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    enabled BOOLEAN NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE interval_settings (
    settings_group_id VARCHAR(64) NOT NULL,
    order_index INTEGER NOT NULL,
    start_time TIME NOT NULL,
    plan INTEGER NOT NULL,
    PRIMARY KEY (settings_group_id, order_index),
    CONSTRAINT fk_interval_settings_group FOREIGN KEY (settings_group_id)
        REFERENCES settings_groups(id) ON DELETE CASCADE,
    CONSTRAINT ck_interval_settings_order CHECK (order_index >= 0),
    CONSTRAINT ck_interval_settings_plan CHECK (plan >= 0),
    CONSTRAINT ux_interval_settings_time UNIQUE (settings_group_id, start_time)
);

INSERT INTO settings_groups (id, name, enabled) VALUES
    ('settings-group-1', 'Sensors 1-4', TRUE),
    ('settings-group-2', 'Sensors 5-6', TRUE);

DO $$
DECLARE
    hour_count INTEGER;
    plan_count INTEGER;
BEGIN
    SELECT jsonb_array_length(setting_value::jsonb) INTO hour_count
    FROM settings WHERE setting_key = 'hours';
    SELECT jsonb_array_length(setting_value::jsonb) INTO plan_count
    FROM settings WHERE setting_key = 'hourlyPlans';
    IF hour_count IS NULL OR hour_count = 0 OR plan_count IS NULL OR hour_count <> plan_count THEN
        RAISE EXCEPTION 'Legacy Time and Plan settings must have the same non-empty size';
    END IF;
END $$;

WITH legacy_hours AS (
    SELECT value::time AS start_time, ordinality - 1 AS order_index
    FROM settings, jsonb_array_elements_text(setting_value::jsonb) WITH ORDINALITY
    WHERE setting_key = 'hours'
), legacy_plans AS (
    SELECT value::integer AS plan, ordinality - 1 AS order_index
    FROM settings, jsonb_array_elements_text(setting_value::jsonb) WITH ORDINALITY
    WHERE setting_key = 'hourlyPlans'
)
INSERT INTO interval_settings (settings_group_id, order_index, start_time, plan)
SELECT groups.id, hours.order_index, hours.start_time, plans.plan
FROM settings_groups groups
CROSS JOIN legacy_hours hours
JOIN legacy_plans plans ON plans.order_index = hours.order_index;

DROP TABLE settings;

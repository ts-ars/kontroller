CREATE TABLE sensors (
    id VARCHAR(64) PRIMARY KEY,
    settings_group_id VARCHAR(64) NOT NULL,
    CONSTRAINT ck_sensor_settings_group
        CHECK (settings_group_id IN ('settings-group-1', 'settings-group-2'))
);

INSERT INTO sensors (id, settings_group_id) VALUES
    ('sensor-1', 'settings-group-1'),
    ('sensor-2', 'settings-group-1'),
    ('sensor-3', 'settings-group-1'),
    ('sensor-4', 'settings-group-1'),
    ('sensor-5', 'settings-group-2'),
    ('sensor-6', 'settings-group-2');

ALTER TABLE shift ADD COLUMN sensor_id VARCHAR(64);
UPDATE shift SET sensor_id = 'sensor-1' WHERE sensor_id IS NULL;
ALTER TABLE shift ALTER COLUMN sensor_id SET NOT NULL;
ALTER TABLE shift ADD CONSTRAINT fk_shift_sensor
    FOREIGN KEY (sensor_id) REFERENCES sensors(id);
ALTER TABLE shift DROP CONSTRAINT shift_date_key;
ALTER TABLE shift ADD CONSTRAINT ux_shift_date_sensor UNIQUE (date, sensor_id);

ALTER TABLE signals
    ADD COLUMN sensor_id VARCHAR(64),
    ADD COLUMN production_date DATE,
    ADD COLUMN source VARCHAR(64),
    ADD COLUMN source_identity VARCHAR(255);

UPDATE signals
SET sensor_id = 'sensor-1',
    production_date = CASE
        WHEN timestamp::time < TIME '07:00' THEN timestamp::date - 1
        ELSE timestamp::date
    END,
    source = 'LEGACY',
    source_identity = id::text;

ALTER TABLE signals
    ALTER COLUMN sensor_id SET NOT NULL,
    ALTER COLUMN production_date SET NOT NULL,
    ALTER COLUMN source SET NOT NULL,
    ALTER COLUMN source_identity SET NOT NULL;

ALTER TABLE signals ADD CONSTRAINT fk_signal_sensor
    FOREIGN KEY (sensor_id) REFERENCES sensors(id);
ALTER TABLE signals ADD CONSTRAINT ux_signal_source_identity
    UNIQUE (sensor_id, source, source_identity);
CREATE INDEX ix_signals_sensor_time
    ON signals(sensor_id, timestamp);

UPDATE stoppages SET sensor_key = 'sensor-1' WHERE sensor_key = 'primary';
ALTER TABLE stoppages ADD CONSTRAINT fk_stoppage_sensor
    FOREIGN KEY (sensor_key) REFERENCES sensors(id);

CREATE TABLE counter_states (
    sensor_id VARCHAR(64) PRIMARY KEY,
    last_counter_value BIGINT NOT NULL CHECK (last_counter_value >= 0),
    last_read_at TIMESTAMP NOT NULL,
    production_date DATE NOT NULL,
    continuity VARCHAR(32) NOT NULL,
    CONSTRAINT fk_counter_state_sensor FOREIGN KEY (sensor_id) REFERENCES sensors(id),
    CONSTRAINT ck_counter_state_continuity
        CHECK (continuity IN ('CONTINUOUS', 'COUNTER_DISCONTINUITY'))
);

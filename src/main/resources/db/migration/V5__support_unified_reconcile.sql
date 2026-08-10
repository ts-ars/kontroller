ALTER TABLE stoppages
    ADD COLUMN incident_key UUID;

UPDATE stoppages
SET incident_key = detection_key
WHERE detection_key IS NOT NULL;

ALTER TABLE stoppages
    ADD CONSTRAINT ck_stoppage_incident_key
        CHECK (detection_key IS NULL OR incident_key IS NOT NULL);

CREATE INDEX ix_stoppages_incident_key
    ON stoppages(incident_key)
    WHERE incident_key IS NOT NULL;

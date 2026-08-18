ALTER TABLE stoppages
    DROP CONSTRAINT ck_stoppage_detection_type;

ALTER TABLE stoppages
    ADD CONSTRAINT ck_stoppage_detection_type
        CHECK (detection_type IS NULL OR detection_type IN ('FIXED', 'TEMPO', 'MANUAL'));

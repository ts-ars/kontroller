INSERT INTO app_user (id, display_name, pin_hash, role, status, created_at, updated_at, version)
VALUES ('00000000-0000-0000-0000-000000000001', 'Legacy', '{noop}disabled', 'USER', 'BLOCKED',
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

ALTER TABLE loss_explanations
    ADD COLUMN author_user_id UUID,
    ADD COLUMN created_at TIMESTAMP WITH TIME ZONE,
    ADD COLUMN updated_at TIMESTAMP WITH TIME ZONE,
    ADD COLUMN last_modified_by UUID;

UPDATE loss_explanations
SET author_user_id = '00000000-0000-0000-0000-000000000001',
    created_at = CURRENT_TIMESTAMP,
    updated_at = CURRENT_TIMESTAMP;

ALTER TABLE loss_explanations
    ALTER COLUMN author_user_id SET NOT NULL,
    ALTER COLUMN created_at SET NOT NULL,
    ALTER COLUMN updated_at SET NOT NULL,
    ADD CONSTRAINT fk_loss_explanation_author FOREIGN KEY (author_user_id) REFERENCES app_user(id) ON DELETE RESTRICT,
    ADD CONSTRAINT fk_loss_explanation_last_modifier FOREIGN KEY (last_modified_by) REFERENCES app_user(id) ON DELETE RESTRICT;

CREATE INDEX ix_loss_explanations_author_user_id ON loss_explanations(author_user_id);

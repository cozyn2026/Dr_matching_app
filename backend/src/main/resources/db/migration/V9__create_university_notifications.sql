CREATE TABLE university_notifications (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    researcher_account_id BIGINT NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
    laboratory_id BIGINT NOT NULL,
    laboratory_name VARCHAR(160) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    read_at TIMESTAMPTZ
);

CREATE INDEX idx_university_notifications_org_created
    ON university_notifications (organization_id, created_at DESC);

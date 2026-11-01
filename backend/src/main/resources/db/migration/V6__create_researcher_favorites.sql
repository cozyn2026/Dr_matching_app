CREATE TABLE researcher_favorites (
    id BIGSERIAL PRIMARY KEY,
    account_id BIGINT NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
    laboratory_id BIGINT NOT NULL REFERENCES laboratories(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_researcher_favorites_account_laboratory UNIQUE (account_id, laboratory_id)
);

CREATE INDEX idx_researcher_favorites_account ON researcher_favorites (account_id, created_at DESC);

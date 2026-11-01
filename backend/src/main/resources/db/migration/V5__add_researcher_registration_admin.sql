CREATE TABLE administrators (
    id BIGSERIAL PRIMARY KEY,
    display_name VARCHAR(120) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO administrators (display_name) VALUES ('ローカル開発管理者');

CREATE TABLE membership_reviews (
    id BIGSERIAL PRIMARY KEY,
    membership_id BIGINT NOT NULL REFERENCES memberships(id),
    administrator_id BIGINT NOT NULL REFERENCES administrators(id),
    decision VARCHAR(24) NOT NULL CHECK (decision IN ('approved', 'rejected')),
    reviewed_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_membership_reviews_membership ON membership_reviews (membership_id, reviewed_at);

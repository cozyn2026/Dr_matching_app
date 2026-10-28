CREATE TABLE laboratories (
    id BIGSERIAL PRIMARY KEY,
    university_name VARCHAR(120) NOT NULL,
    graduate_school_name VARCHAR(160) NOT NULL,
    laboratory_name VARCHAR(160) NOT NULL,
    supervisor_name VARCHAR(120) NOT NULL,
    research_field VARCHAR(100) NOT NULL,
    research_summary TEXT NOT NULL,
    accepts_working_professionals BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_laboratories_name ON laboratories (laboratory_name);

CREATE TABLE audit_events (
    id                 UUID PRIMARY KEY,
    user_id            UUID REFERENCES users (id) ON DELETE SET NULL,
    type               VARCHAR(64)  NOT NULL,
    robot_profile_id   UUID REFERENCES robot_profiles (id) ON DELETE SET NULL,
    payload_json       JSONB        NOT NULL DEFAULT '{}'::jsonb,
    created_at         TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_events_created_at ON audit_events (created_at);
CREATE INDEX idx_audit_events_type ON audit_events (type);
CREATE INDEX idx_audit_events_user ON audit_events (user_id);

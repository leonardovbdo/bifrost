CREATE TABLE telemetry_samples (
    id                 UUID PRIMARY KEY,
    robot_profile_id   UUID         NOT NULL REFERENCES robot_profiles (id) ON DELETE CASCADE,
    metric             VARCHAR(64)  NOT NULL CHECK (metric IN ('battery', 'pose')),
    value_json         JSONB        NOT NULL DEFAULT '{}'::jsonb,
    captured_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_telemetry_samples_captured_at ON telemetry_samples (captured_at DESC);
CREATE INDEX idx_telemetry_samples_profile_metric_captured
    ON telemetry_samples (robot_profile_id, metric, captured_at DESC);

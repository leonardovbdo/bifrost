-- Robot profiles (antes de users.last_active por FK)
CREATE TABLE robot_profiles (
    id              UUID PRIMARY KEY,
    slug            VARCHAR(100) NOT NULL UNIQUE,
    display_name    VARCHAR(200) NOT NULL,
    project         VARCHAR(100) NOT NULL,
    prefix          VARCHAR(100) NOT NULL,
    environment     VARCHAR(20)  NOT NULL,
    technology      VARCHAR(100) NOT NULL,
    capabilities    JSONB        NOT NULL DEFAULT '[]'::jsonb,
    topics          JSONB        NOT NULL DEFAULT '{}'::jsonb,
    frames          JSONB        NOT NULL DEFAULT '{}'::jsonb,
    rosbridge_url   VARCHAR(500) NOT NULL,
    video_base_url  VARCHAR(500) NOT NULL,
    active          BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT robot_profiles_environment_chk CHECK (environment IN ('sim', 'physical'))
);

CREATE INDEX idx_robot_profiles_active ON robot_profiles (active);

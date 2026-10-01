CREATE TABLE users (
    id                       UUID PRIMARY KEY,
    username                 VARCHAR(100) NOT NULL UNIQUE,
    email                    VARCHAR(255) UNIQUE,
    password_hash            VARCHAR(255) NOT NULL,
    role                     VARCHAR(20)  NOT NULL,
    active                   BOOLEAN      NOT NULL DEFAULT TRUE,
    last_active_profile_id   UUID REFERENCES robot_profiles (id) ON DELETE SET NULL,
    created_at               TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at               TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT users_role_chk CHECK (role IN ('admin', 'operator', 'viewer'))
);

CREATE INDEX idx_users_active ON users (active);

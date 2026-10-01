CREATE TABLE user_profile_access (
    user_id           UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    robot_profile_id  UUID NOT NULL REFERENCES robot_profiles (id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, robot_profile_id)
);

CREATE INDEX idx_upa_profile ON user_profile_access (robot_profile_id);

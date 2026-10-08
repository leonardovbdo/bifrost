CREATE TABLE parameters (
    id          UUID PRIMARY KEY,
    scope       VARCHAR(20)  NOT NULL,
    scope_id    UUID,
    key         VARCHAR(200) NOT NULL,
    value_json  JSONB        NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT parameters_scope_chk CHECK (scope IN ('global', 'user')),
    CONSTRAINT parameters_scope_id_chk CHECK (
        (scope = 'global' AND scope_id IS NULL) OR
        (scope = 'user' AND scope_id IS NOT NULL)
    )
);

CREATE UNIQUE INDEX uq_parameters_global_key
    ON parameters (key) WHERE scope = 'global';

CREATE UNIQUE INDEX uq_parameters_user_key
    ON parameters (scope_id, key) WHERE scope = 'user';

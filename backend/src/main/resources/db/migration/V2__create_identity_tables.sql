-- V2: identity tables. See ADR-010 (organization_id), ADR-013 (soft delete), ADR-014 (RBAC).

-- V1 already ran and is never edited, so organizations gets its new columns here.
ALTER TABLE identity.organizations
    ADD COLUMN updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    ADD COLUMN deleted_at TIMESTAMPTZ;

CREATE TABLE identity.users (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES identity.organizations (id),
    email           TEXT NOT NULL,
    password_hash   TEXT NOT NULL,
    enabled         BOOLEAN NOT NULL DEFAULT TRUE,
    last_login_at   TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at      TIMESTAMPTZ,
    deleted_by      UUID REFERENCES identity.users (id),
    CONSTRAINT users_email_not_blank CHECK (length(trim(email)) > 0)
);

-- One active account per email, case insensitive. Soft deleted rows do not count.
CREATE UNIQUE INDEX ux_users_email_active
    ON identity.users (lower(email))
    WHERE deleted_at IS NULL;
CREATE INDEX ix_users_organization ON identity.users (organization_id);

CREATE TABLE identity.roles (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES identity.organizations (id),
    name            TEXT NOT NULL,
    description     TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at      TIMESTAMPTZ,
    deleted_by      UUID REFERENCES identity.users (id),
    CONSTRAINT roles_name_not_blank CHECK (length(trim(name)) > 0)
);

CREATE UNIQUE INDEX ux_roles_name_active
    ON identity.roles (organization_id, lower(name))
    WHERE deleted_at IS NULL;

-- A global catalog of single actions. Only migrations change it, so no soft delete.
CREATE TABLE identity.permissions (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        TEXT NOT NULL UNIQUE,
    description TEXT,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Link tables: plain rows, removed physically when a grant is revoked.
CREATE TABLE identity.user_roles (
    user_id     UUID NOT NULL REFERENCES identity.users (id),
    role_id     UUID NOT NULL REFERENCES identity.roles (id),
    assigned_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (user_id, role_id)
);
CREATE INDEX ix_user_roles_role ON identity.user_roles (role_id);

CREATE TABLE identity.role_permissions (
    role_id       UUID NOT NULL REFERENCES identity.roles (id),
    permission_id UUID NOT NULL REFERENCES identity.permissions (id),
    PRIMARY KEY (role_id, permission_id)
);
CREATE INDEX ix_role_permissions_permission ON identity.role_permissions (permission_id);

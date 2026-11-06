CREATE SCHEMA IF NOT EXISTS identity;
CREATE SCHEMA IF NOT EXISTS workflows;
CREATE SCHEMA IF NOT EXISTS ai;
CREATE SCHEMA IF NOT EXISTS monitoring;

-- Every core table references this one through organization_id (ADR-010).
CREATE TABLE identity.organizations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Single tenant for now: exactly one organization.
INSERT INTO identity.organizations (name) VALUES ('Default Organization');

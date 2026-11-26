CREATE TABLE projects (
    id UUID PRIMARY KEY,
    name TEXT NOT NULL,
    description TEXT,
    created_by UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE project_memberships (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL REFERENCES projects (id),
    user_id UUID NOT NULL,
    role VARCHAR(32) NOT NULL CHECK (role IN ('PROJECT_MANAGER', 'STAKEHOLDER', 'DEVELOPER')),
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_project_membership_user UNIQUE (project_id, user_id)
);

CREATE INDEX ix_project_memberships_user_id ON project_memberships (user_id);

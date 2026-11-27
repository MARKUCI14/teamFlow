CREATE TABLE submissions (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL REFERENCES projects (id),
    author_id UUID NOT NULL,
    title TEXT NOT NULL,
    description TEXT NOT NULL,
    type VARCHAR(32) NOT NULL CHECK (type IN ('BUG', 'SUGGESTION')),
    status VARCHAR(32) NOT NULL CHECK (status IN ('PENDING', 'NEEDS_CLARIFICATION', 'ACCEPTED', 'REJECTED')),
    clarification_message TEXT,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX ix_submissions_project_author_created
    ON submissions (project_id, author_id, created_at DESC);

CREATE TABLE issues (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL REFERENCES projects (id),
    submission_id UUID NOT NULL REFERENCES submissions (id),
    created_by UUID NOT NULL,
    assignee_id UUID,
    title TEXT NOT NULL,
    description TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX ix_issues_submission_id ON issues (submission_id);

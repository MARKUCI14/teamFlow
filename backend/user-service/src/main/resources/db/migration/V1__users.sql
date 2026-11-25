CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(320) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    global_role VARCHAR(32),
    created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE app_user
(
    id            UUID                     NOT NULL PRIMARY KEY,
    name          VARCHAR(255)             NOT NULL,
    email         VARCHAR(255)             NOT NULL,
    password_hash VARCHAR(255)             NOT NULL,
    role          VARCHAR(32)              NOT NULL,
    created_at    TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at    TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_app_user_email UNIQUE (email)
);

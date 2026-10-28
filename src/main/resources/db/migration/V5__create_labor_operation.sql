CREATE TABLE labor_operation
(
    id                         UUID                     NOT NULL PRIMARY KEY,
    name                       VARCHAR(255)             NOT NULL,
    description                TEXT,
    price                      NUMERIC(12, 2)           NOT NULL,
    estimated_duration_minutes INTEGER                  NOT NULL,
    active                     BOOLEAN                  NOT NULL DEFAULT TRUE,
    created_at                 TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at                 TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_labor_operation_name UNIQUE (name),
    CONSTRAINT ck_labor_operation_price CHECK (price >= 0),
    CONSTRAINT ck_labor_operation_estimated_duration CHECK (estimated_duration_minutes > 0)
);

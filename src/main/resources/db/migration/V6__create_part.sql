CREATE TABLE part
(
    id                 UUID                     NOT NULL PRIMARY KEY,
    code               VARCHAR(50)              NOT NULL,
    name               VARCHAR(255)             NOT NULL,
    type               VARCHAR(16)              NOT NULL,
    unit_of_measure    VARCHAR(10)              NOT NULL,
    unit_price         NUMERIC(12, 2)           NOT NULL,
    available_quantity NUMERIC(12, 3)           NOT NULL DEFAULT 0,
    reserved_quantity  NUMERIC(12, 3)           NOT NULL DEFAULT 0,
    minimum_stock      NUMERIC(12, 3)           NOT NULL DEFAULT 0,
    active             BOOLEAN                  NOT NULL DEFAULT TRUE,
    version            BIGINT                   NOT NULL DEFAULT 0,
    created_at         TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at         TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_part_code UNIQUE (code),
    CONSTRAINT ck_part_type CHECK (type IN ('PART', 'SUPPLY')),
    CONSTRAINT ck_part_unit_price CHECK (unit_price >= 0),
    CONSTRAINT ck_part_available_quantity CHECK (available_quantity >= 0),
    CONSTRAINT ck_part_reserved_quantity CHECK (reserved_quantity >= 0),
    CONSTRAINT ck_part_minimum_stock CHECK (minimum_stock >= 0)
);

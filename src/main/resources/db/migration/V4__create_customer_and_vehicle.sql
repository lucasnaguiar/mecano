CREATE TABLE customer
(
    id         UUID                     NOT NULL PRIMARY KEY,
    name       VARCHAR(255)             NOT NULL,
    tax_id     VARCHAR(14)              NOT NULL,
    email      VARCHAR(255),
    phone      VARCHAR(20),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_customer_tax_id UNIQUE (tax_id),
    -- CPF (11 dígitos) ou CNPJ (14 posições, alfanumérico desde 07/2026), sem pontuação.
    CONSTRAINT ck_customer_tax_id CHECK (tax_id ~ '^([0-9]{11}|[0-9A-Z]{12}[0-9]{2})$')
);

CREATE TABLE vehicle
(
    id            UUID                     NOT NULL PRIMARY KEY,
    customer_id   UUID                     NOT NULL,
    license_plate VARCHAR(7)               NOT NULL,
    brand         VARCHAR(100)             NOT NULL,
    model         VARCHAR(100)             NOT NULL,
    model_year    SMALLINT                 NOT NULL,
    created_at    TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at    TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_vehicle_customer FOREIGN KEY (customer_id) REFERENCES customer (id),
    CONSTRAINT uk_vehicle_license_plate UNIQUE (license_plate),
    -- Formato antigo (ABC1234) ou Mercosul (ABC1D23), sem hífen.
    CONSTRAINT ck_vehicle_license_plate CHECK (license_plate ~ '^[A-Z]{3}[0-9][A-Z0-9][0-9]{2}$'),
    CONSTRAINT ck_vehicle_model_year CHECK (model_year >= 1900)
);

CREATE INDEX idx_vehicle_customer_id ON vehicle (customer_id);

-- Número legível da OS usado pela equipe (ex.: OS 1000).
CREATE SEQUENCE work_order_number_seq START WITH 1000;

CREATE TABLE work_order
(
    id                  UUID                     NOT NULL PRIMARY KEY,
    number              BIGINT                   NOT NULL DEFAULT nextval('work_order_number_seq'),
    tracking_code       VARCHAR(32)              NOT NULL,
    customer_id         UUID                     NOT NULL,
    vehicle_id          UUID                     NOT NULL,
    status              VARCHAR(32)              NOT NULL,
    problem_description TEXT                     NOT NULL,
    diagnosis_notes     TEXT,
    total_amount        NUMERIC(12, 2)           NOT NULL DEFAULT 0,
    version             BIGINT                   NOT NULL DEFAULT 0,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_work_order_number UNIQUE (number),
    CONSTRAINT uk_work_order_tracking_code UNIQUE (tracking_code),
    CONSTRAINT fk_work_order_customer FOREIGN KEY (customer_id) REFERENCES customer (id),
    CONSTRAINT fk_work_order_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicle (id),
    CONSTRAINT ck_work_order_status CHECK (status IN ('RECEIVED', 'IN_DIAGNOSIS', 'AWAITING_APPROVAL',
                                                      'IN_PROGRESS', 'COMPLETED', 'DELIVERED')),
    CONSTRAINT ck_work_order_total_amount CHECK (total_amount >= 0)
);

ALTER SEQUENCE work_order_number_seq OWNED BY work_order.number;

CREATE INDEX idx_work_order_status ON work_order (status);
CREATE INDEX idx_work_order_customer_id ON work_order (customer_id);
CREATE INDEX idx_work_order_vehicle_id ON work_order (vehicle_id);

-- Serviço do catálogo incluído na OS, com descrição e preço copiados no momento da inclusão.
CREATE TABLE labor_item
(
    id                 UUID           NOT NULL PRIMARY KEY,
    work_order_id      UUID           NOT NULL,
    labor_operation_id UUID           NOT NULL,
    description        VARCHAR(255)   NOT NULL,
    unit_price         NUMERIC(12, 2) NOT NULL,
    quantity           INTEGER        NOT NULL DEFAULT 1,
    CONSTRAINT fk_labor_item_work_order FOREIGN KEY (work_order_id) REFERENCES work_order (id) ON DELETE CASCADE,
    CONSTRAINT fk_labor_item_labor_operation FOREIGN KEY (labor_operation_id) REFERENCES labor_operation (id),
    CONSTRAINT ck_labor_item_unit_price CHECK (unit_price >= 0),
    CONSTRAINT ck_labor_item_quantity CHECK (quantity > 0)
);

CREATE INDEX idx_labor_item_work_order_id ON labor_item (work_order_id);

-- Peça ou insumo incluído na OS, com descrição, unidade e preço copiados no momento da inclusão.
CREATE TABLE part_item
(
    id              UUID           NOT NULL PRIMARY KEY,
    work_order_id   UUID           NOT NULL,
    part_id         UUID           NOT NULL,
    description     VARCHAR(255)   NOT NULL,
    unit_of_measure VARCHAR(10)    NOT NULL,
    unit_price      NUMERIC(12, 2) NOT NULL,
    quantity        NUMERIC(12, 3) NOT NULL,
    CONSTRAINT fk_part_item_work_order FOREIGN KEY (work_order_id) REFERENCES work_order (id) ON DELETE CASCADE,
    CONSTRAINT fk_part_item_part FOREIGN KEY (part_id) REFERENCES part (id),
    CONSTRAINT ck_part_item_unit_price CHECK (unit_price >= 0),
    CONSTRAINT ck_part_item_quantity CHECK (quantity > 0)
);

CREATE INDEX idx_part_item_work_order_id ON part_item (work_order_id);

-- Orçamento enviado ao cliente. Uma OS pode ter vários (reorçamento), mas só um pendente por vez.
CREATE TABLE estimate
(
    id            UUID                     NOT NULL PRIMARY KEY,
    work_order_id UUID                     NOT NULL,
    status        VARCHAR(16)              NOT NULL,
    total_amount  NUMERIC(12, 2)           NOT NULL,
    sent_at       TIMESTAMP WITH TIME ZONE NOT NULL,
    responded_at  TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_estimate_work_order FOREIGN KEY (work_order_id) REFERENCES work_order (id) ON DELETE CASCADE,
    CONSTRAINT ck_estimate_status CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
    CONSTRAINT ck_estimate_total_amount CHECK (total_amount >= 0),
    CONSTRAINT ck_estimate_responded_at CHECK ((status = 'PENDING') = (responded_at IS NULL))
);

CREATE INDEX idx_estimate_work_order_id ON estimate (work_order_id);
CREATE UNIQUE INDEX uk_estimate_pending_per_work_order ON estimate (work_order_id) WHERE status = 'PENDING';

-- Histórico de status da OS. changed_by nulo indica ação do cliente ou do sistema.
CREATE TABLE status_transition
(
    id            UUID                     NOT NULL PRIMARY KEY,
    work_order_id UUID                     NOT NULL,
    from_status   VARCHAR(32),
    to_status     VARCHAR(32)              NOT NULL,
    changed_by    UUID,
    occurred_at   TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_status_transition_work_order FOREIGN KEY (work_order_id) REFERENCES work_order (id) ON DELETE CASCADE,
    CONSTRAINT fk_status_transition_changed_by FOREIGN KEY (changed_by) REFERENCES app_user (id),
    CONSTRAINT ck_status_transition_from_status CHECK (from_status IN ('RECEIVED', 'IN_DIAGNOSIS', 'AWAITING_APPROVAL',
                                                                       'IN_PROGRESS', 'COMPLETED', 'DELIVERED')),
    CONSTRAINT ck_status_transition_to_status CHECK (to_status IN ('RECEIVED', 'IN_DIAGNOSIS', 'AWAITING_APPROVAL',
                                                                   'IN_PROGRESS', 'COMPLETED', 'DELIVERED'))
);

CREATE INDEX idx_status_transition_work_order_to_status ON status_transition (work_order_id, to_status);

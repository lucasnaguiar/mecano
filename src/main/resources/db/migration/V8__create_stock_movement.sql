-- Extrato de movimentações do estoque. Os saldos consolidados ficam em part.
CREATE TABLE stock_movement
(
    id            UUID                     NOT NULL PRIMARY KEY,
    part_id       UUID                     NOT NULL,
    work_order_id UUID,
    user_id       UUID,
    type          VARCHAR(16)              NOT NULL,
    quantity      NUMERIC(12, 3)           NOT NULL,
    occurred_at   TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_stock_movement_part FOREIGN KEY (part_id) REFERENCES part (id),
    CONSTRAINT fk_stock_movement_work_order FOREIGN KEY (work_order_id) REFERENCES work_order (id),
    CONSTRAINT fk_stock_movement_user FOREIGN KEY (user_id) REFERENCES app_user (id),
    CONSTRAINT ck_stock_movement_type CHECK (type IN ('ENTRY', 'RESERVATION', 'RELEASE', 'WITHDRAWAL')),
    CONSTRAINT ck_stock_movement_quantity CHECK (quantity > 0),
    -- Reserva, liberação e baixa sempre pertencem a uma OS; só a entrada pode não ter.
    CONSTRAINT ck_stock_movement_work_order CHECK (type = 'ENTRY' OR work_order_id IS NOT NULL)
);

CREATE INDEX idx_stock_movement_part_occurred_at ON stock_movement (part_id, occurred_at);
CREATE INDEX idx_stock_movement_work_order_id ON stock_movement (work_order_id);

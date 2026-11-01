-- Cancelamento de OS pela equipe antes do início da execução: novo status final CANCELED na OS e no histórico,
-- orçamento pendente encerrado como CANCELED e motivo obrigatório gravado na OS.
ALTER TABLE work_order ADD COLUMN cancellation_reason TEXT;

ALTER TABLE work_order DROP CONSTRAINT ck_work_order_status;
ALTER TABLE work_order ADD CONSTRAINT ck_work_order_status CHECK (status IN ('RECEIVED', 'IN_DIAGNOSIS',
    'AWAITING_APPROVAL', 'IN_PROGRESS', 'COMPLETED', 'DELIVERED', 'CANCELED'));
ALTER TABLE work_order ADD CONSTRAINT ck_work_order_cancellation_reason
    CHECK ((status = 'CANCELED') = (cancellation_reason IS NOT NULL));

ALTER TABLE status_transition DROP CONSTRAINT ck_status_transition_from_status;
ALTER TABLE status_transition ADD CONSTRAINT ck_status_transition_from_status CHECK (from_status IN ('RECEIVED',
    'IN_DIAGNOSIS', 'AWAITING_APPROVAL', 'IN_PROGRESS', 'COMPLETED', 'DELIVERED', 'CANCELED'));
ALTER TABLE status_transition DROP CONSTRAINT ck_status_transition_to_status;
ALTER TABLE status_transition ADD CONSTRAINT ck_status_transition_to_status CHECK (to_status IN ('RECEIVED',
    'IN_DIAGNOSIS', 'AWAITING_APPROVAL', 'IN_PROGRESS', 'COMPLETED', 'DELIVERED', 'CANCELED'));

ALTER TABLE estimate DROP CONSTRAINT ck_estimate_status;
ALTER TABLE estimate ADD CONSTRAINT ck_estimate_status CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED',
    'CANCELED'));

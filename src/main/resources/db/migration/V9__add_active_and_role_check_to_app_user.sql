-- Usuários podem ser desativados (sem login) mantendo o histórico de ações.
ALTER TABLE app_user ADD COLUMN active BOOLEAN NOT NULL DEFAULT TRUE;

ALTER TABLE app_user
    ADD CONSTRAINT ck_app_user_role CHECK (role IN ('ADMIN', 'ATTENDANT', 'MECHANIC', 'STOCK_MANAGER'));

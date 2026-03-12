CALL ADD_COLUMN('invoice', 'version', 'BIGINT NOT NULL DEFAULT 0');
CALL ADD_COLUMN('invoice_item', 'version', 'BIGINT NOT NULL DEFAULT 0');
CALL ADD_COLUMN('payment', 'version', 'BIGINT NOT NULL DEFAULT 0');
CALL ADD_COLUMN('subscription', 'version', 'BIGINT NOT NULL DEFAULT 0');
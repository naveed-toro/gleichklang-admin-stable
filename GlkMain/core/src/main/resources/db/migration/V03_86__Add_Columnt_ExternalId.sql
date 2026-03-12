CALL ADD_COLUMN('payment', 'external_id', 'VARCHAR(255)');
CALL CREATE_UNIQUE_INDEX('payment_external_id', 'payment', 'external_id');
CALL RENAME_TABLE('heidelpay_event', 'heidelpay_transaction');

CALL CHANGE_COLUMN('heidelpay_transaction', 'post_validation', 'result', 'VARCHAR(255) NOT NULL');
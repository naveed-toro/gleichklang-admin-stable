CALL ADD_COLUMN('payment', 'bank_account_id', 'BIGINT');
CALL ADD_FOREIGN_KEY('payment', 'bank_account_id', 'bank_account', 'id');

CALL DROP_COLUMN('payment', "created_subscription_id");
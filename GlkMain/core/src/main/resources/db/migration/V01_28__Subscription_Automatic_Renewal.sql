CALL ADD_COLUMN('subscription', 'automatic_renewal', 'BIT NOT NULL');

CALL ADD_COLUMN('subscription', 'external_id', 'VARCHAR(255) NULL');

CALL DROP_COLUMN('subscription', 'external_account_id');




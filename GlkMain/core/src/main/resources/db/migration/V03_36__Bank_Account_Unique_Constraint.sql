CALL ADD_COLUMN('bank_account', 'active', 'BIT');

-- GLS Bank is the active bank account
UPDATE bank_account SET active = TRUE WHERE UPPER(bank_name) LIKE '%GLS%';

-- Only one active bank account per country is allowed
CALL CREATE_UNIQUE_INDEX('bank_account_country_id', 'bank_account', 'active, country_id');

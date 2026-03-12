-- *** Migration of Unknown Bank ***
-- See also: V03_84__Migrate_Payments

DELETE FROM bank_account WHERE iban = 'Unknown';

INSERT INTO bank_account (create_date, holder, bank_name, bank_number, account_number, iban, bic, country_id)
VALUES (CURDATE(), 'Unknown', 'Unknown', 'Unknown', 'Unknown', 'Unknown', 'Unknown', 53);
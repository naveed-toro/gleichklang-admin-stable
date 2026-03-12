-- WARNING: This migration contains a wrong order of "bank_number" and "account_number" columns.
-- Fixed with the migration "V03_58__Fix_BankAccounts_And_Prepayments.sql".
INSERT INTO bank_account (create_date, holder, bank_name, account_number,
                          bank_number, iban, bic, country)
  SELECT DISTINCT
    CURRENT_DATE()                            AS create_date,

    connector_account_holder                  AS holder,

    IF(connector_account_bankname IS NULL OR connector_account_bankname = '',
       'Unknown', connector_account_bankname) AS bank_name,
    connector_account_bank                    AS bank_number,
    connector_account_number                  AS account_number,

    REPLACE(connector_account_iban, ' ', '')  AS iban,
    connector_account_bic                     AS bic,

    IF(connector_account_country IS NULL, 'DE', connector_account_country)
                                              AS country
  FROM comppayment_transaction
  WHERE method = 'PP'
        AND ((connector_account_iban IS NOT NULL AND connector_account_iban <> '')
          AND (connector_account_bank IS NOT NULL AND connector_account_bank <> ''
             AND connector_account_number IS NOT NULL AND connector_account_number <> ''))
        AND connector_account_bankname NOT LIKE '(%)'
  GROUP BY bank_number, account_number;
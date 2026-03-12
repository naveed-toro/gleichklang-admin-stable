-- *** Migration of Bank Accounts ***

DELETE FROM bank_account;

INSERT INTO bank_account (create_date, holder, bank_name, bank_number,
                          account_number, iban, bic, country_id)
  SELECT DISTINCT
    CURRENT_DATE()                            AS create_date,
    connector_account_holder                  AS holder,
    IF(connector_account_bankname IS NULL OR connector_account_bankname = '',
       'Unknown', connector_account_bankname) AS bank_name,
    connector_account_bank                    AS bank_number,
    connector_account_number                  AS account_number,
    REPLACE(connector_account_iban, ' ', '')  AS iban,
    connector_account_bic                     AS bic,
    l.id                                      AS country_id
  FROM comppayment_transaction
    JOIN locatable l ON IF(connector_account_country IS NULL, 'DE',
                           connector_account_country) = l.i18n_key AND
                        l.DTYPE = 'Country'
  WHERE method = 'PP' AND
        ((connector_account_iban IS NOT NULL AND connector_account_iban <> '')
         AND (connector_account_bank IS NOT NULL AND connector_account_bank <> ''
          AND connector_account_number IS NOT NULL AND connector_account_number <> ''))
        AND connector_account_bankname NOT LIKE '(%)'
  GROUP BY bank_number, account_number;


UPDATE bank_account SET active = TRUE WHERE UPPER(bank_name) LIKE '%GLS%';
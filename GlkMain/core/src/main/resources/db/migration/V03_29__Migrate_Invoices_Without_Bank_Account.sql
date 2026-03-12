-- *** Migration of Invoices without Connector Account Bank ***
--
-- See also:
--  V01_76__Invoice_Migration
--  V03_90__Migrate_Invoice_Items_From_Scratch

DELETE FROM invoice WHERE EXISTS(
    SELECT 1
    FROM comppayment_transaction t
    WHERE t.no = invoice.legacy_id AND t.connector_account_bank IS NULL
);

INSERT INTO invoice (legacy_id, change_date,
                     create_date, user_id)
  SELECT
    t.no         AS legacy_id,
    t.changedate AS change_date,
    t.createdate AS create_date,
    u.id         AS user_id
  FROM comppayment_transaction t
    JOIN user_ u ON t.participant_id = u.legacy_id
  WHERE t.connector_account_bank IS NULL;
CALL CREATE_INDEX('legacy_id_index', 'invoice', 'legacy_id');
CALL CREATE_INDEX('legacy_id_index', 'subscription', 'legacy_id');
CALL CREATE_INDEX('legacy_id_index', 'payment', 'legacy_id');

CALL DROP_COLUMN('payment', "created_subscription_id");

ALTER TABLE invoice_item CHANGE COLUMN subscription_id subscription_id BIGINT NULL;

INSERT INTO invoice(legacy_id, change_date, create_date, user_id)
  SELECT
    t.no         AS legacy_id,
    t.changedate AS change_date,
    t.createdate AS create_date,
    u.id         AS user_id
  FROM comppayment_transaction t
    JOIN user_ u ON t.participant_id = u.legacy_id
  WHERE t.connector_account_bank IS NOT NULL;

-- Invoice items are migrated separately (see migration V03_90__Migrate_Invoice_Items_From_Scratch)
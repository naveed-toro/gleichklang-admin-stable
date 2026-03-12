-- *** Migration of Payments ***
-- Pending payments of service offers are excluded on request.

DELETE item FROM invoice_item item
  JOIN invoice i ON item.invoice_id = i.id
  JOIN comppayment_transaction t ON i.legacy_id = t.no
WHERE t.protocolstate IN ('initial', 'wait_receipt') AND
      (UPPER(t.offer_name) LIKE 'KOSTEN_%' OR UPPER(t.offer_name) LIKE 'OPTIMIERUNG_%');

DELETE i FROM invoice i
  JOIN comppayment_transaction t ON i.legacy_id = t.no
WHERE t.protocolstate IN ('initial', 'wait_receipt') AND
      (UPPER(t.offer_name) LIKE 'KOSTEN_%' OR UPPER(t.offer_name) LIKE 'OPTIMIERUNG_%');

DELETE FROM affiliate_payment_state;
DELETE FROM heidelpay_transaction;
DELETE FROM payment;

SET @unknown_bank = (SELECT id FROM bank_account WHERE iban = 'Unknown');

INSERT INTO payment (legacy_id, change_date, create_date, DTYPE, state, user_id,
                     bank_account_id, external_reference_id, invoice_id, method,
                     amount, currency, reminder_count)
  SELECT
    t.no                                               AS legacy_id,
    t.changedate                                       AS change_date,
    t.createdate                                       AS create_date,
    'Prepayment'                                       AS DTYPE,
    CASE t.protocolstate
      WHEN 'initial' THEN 'PENDING'
      WHEN 'wait_receipt' THEN 'PENDING'
      WHEN 'chargeback' THEN 'CANCELED'
      WHEN 'canceled' THEN 'CANCELED'
      WHEN 'closed' THEN 'PAID'
      WHEN 'paid' THEN 'PAID'
    END                                                AS state,
    u.id                                               AS user_id,
    IFNULL(b.id, @unknown_bank)                        AS bank_account_id,
    t.identification_shortid                           AS external_reference_id,
    i.id                                               AS invoice_id,
    'PREPAYMENT'                                       AS method,
    IFNULL(NULLIF(REPLACE(t.amount, ',', '.'), ''), 0) AS amount,
    'EUR', -- assumes EUR, which prevents a join with product where the product name isn't unique
    100
  FROM comppayment_transaction t
    JOIN user_ u ON t.participant_id = u.legacy_id
    JOIN invoice i ON t.no = i.legacy_id
    LEFT JOIN bank_account b
      ON (REPLACE(t.connector_account_iban, ' ', '') = b.iban OR
          (t.connector_account_bank = b.bank_number AND
           t.connector_account_number = b.account_number))
  WHERE
    t.method = 'PP' AND
    (t.identification_shortid IS NOT NULL AND t.identification_shortid <> '') AND
    (t.protocolstate IN ('paid', 'closed', 'canceled', 'chargeback') OR
     (t.protocolstate IN ('initial', 'wait_receipt') AND
      UPPER(t.offer_name) NOT LIKE 'KOSTEN_%' AND
      UPPER(t.offer_name) NOT LIKE 'OPTIMIERUNG_%') AND
      t.createdate BETWEEN NOW() - INTERVAL 30 DAY AND NOW());

INSERT INTO payment (legacy_id, change_date, create_date, DTYPE, state, user_id,
                     external_reference_id, invoice_id,method, amount, currency)
  SELECT
    t.no                                               AS legacy_id,
    t.changedate                                       AS change_date,
    t.createdate                                       AS create_date,
    'ExternalPayment'                                  AS DTYPE,
    CASE t.protocolstate
      WHEN 'initial' THEN 'PENDING'
      WHEN 'wait_receipt' THEN 'PENDING'
      WHEN 'chargeback' THEN 'CANCELED'
      WHEN 'canceled' THEN 'CANCELED'
      WHEN 'closed' THEN 'PAID'
      WHEN 'paid' THEN 'PAID'
    END                                                AS state,
    u.id                                               AS user_id,
    t.identification_transactionid                     AS external_reference_id,
    i.id                                               AS invoice_id,
    CASE t.method
      WHEN 'CC' THEN 'CREDIT_CARD'
      WHEN 'DD' THEN 'DIRECT_DEBIT'
    END                                                AS method,
    IFNULL(NULLIF(REPLACE(t.amount, ',', '.'), ''), 0) AS amount,
    'EUR' -- assumes EUR, which prevents a join with product where the product name isn't unique
  FROM comppayment_transaction t
    JOIN user_ u ON t.participant_id = u.legacy_id
    JOIN invoice i ON t.no = i.legacy_id
  WHERE
    t.method IN ('CC', 'DD') AND
    (t.protocolstate IN ('paid', 'closed', 'canceled', 'chargeback') OR
     (t.protocolstate IN ('initial', 'wait_receipt') AND
      UPPER(t.offer_name) NOT LIKE 'KOSTEN_%' AND
      UPPER(t.offer_name) NOT LIKE 'OPTIMIERUNG_%') AND
      t.createdate BETWEEN NOW() - INTERVAL 30 DAY AND NOW());


-- update current flag
UPDATE payment p1 SET p1.current = NULL;

UPDATE payment p1
  JOIN (SELECT max(create_date), id FROM payment GROUP BY user_id) p2
    ON p1.id = p2.id
SET p1.current = 1;


INSERT INTO affiliate_payment_state (affiliate_partner, external_id,
                                     create_date, paid_to_partner, payment_id)
  SELECT
    'SUPERCLIX'    AS affiliate_partner,
    t.superclix_id AS external_id,
    t.createdate   AS create_date,
    IF(t.superclix_provision_paid IS NULL, 0, t.superclix_provision_paid)
                   AS paid_to_partner,
    payment.id     AS payment_id
  FROM comppayment_transaction t
    JOIN payment ON t.no = payment.legacy_id
  WHERE t.superclix_id IS NOT NULL AND t.superclix_id <> '';
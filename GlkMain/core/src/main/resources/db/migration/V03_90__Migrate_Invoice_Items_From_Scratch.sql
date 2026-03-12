CALL CREATE_INDEX('invoice_legacy_id', 'invoice', 'legacy_id');
CALL CREATE_INDEX('product_name', 'product', 'name');

-- Delete invoices for not existing products
DELETE i FROM invoice i
  JOIN comppayment_transaction t ON i.legacy_id = t.no
  LEFT JOIN product p ON t.offer_name = p.name
WHERE p.id IS NULL;

DELETE FROM invoice_item;

INSERT INTO invoice_item (legacy_id, change_date, create_date, invoice_id,
                          subscription_id, product_id, amount, currency)
  SELECT
    t.no                                               AS legacy_id,
    t.changedate                                       AS change_date,
    t.createdate                                       AS create_date,
    i.id                                               AS invoice_id,
    s.id                                               AS subscription_id,
    p.id                                               AS product_id,
    IFNULL(NULLIF(REPLACE(t.amount, ',', '.'), ''), 0) AS amount,
    p.currency                                         AS currency
  FROM comppayment_transaction t
    JOIN invoice i ON i.legacy_id = t.no
    JOIN product p ON t.offer_name = p.name
    LEFT JOIN subscription s ON s.legacy_id = t.no
  -- left join because not all transactions are in state paid
  WHERE t.protocolstate IN ('paid', 'closed', 'canceled', 'chargeback') OR
        (t.protocolstate IN ('initial', 'wait_receipt') AND
         UPPER(t.offer_name) NOT LIKE 'KOSTEN_%' AND
         UPPER(t.offer_name) NOT LIKE 'OPTIMIERUNG_%');

-- Clean up
CALL DROP_INDEX('product_name', 'product');
CALL DROP_INDEX('invoice_legacy_id', 'invoice');
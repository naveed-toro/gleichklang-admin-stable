UPDATE invoice_item i
SET i.subscription_id = (SELECT s.id
                         FROM subscription s
                         WHERE s.legacy_id = i.legacy_id
                         ORDER BY s.create_date DESC
                         LIMIT 1)
WHERE i.subscription_id IS NULL;
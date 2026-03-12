DELETE FROM invoice_item WHERE subscription_id IS NOT NULL;
DELETE FROM subscription;

-- Index to product.name to speed up the migration
CALL CREATE_INDEX('product_name', 'product', 'name');


-- *** I. Migrate direct subscriptions (initial and renewal offers) ***

INSERT INTO subscription (legacy_id, change_date, create_date, user_id, `begin`,
                          `end`, expiration_date, offer_id, automatic_renewal)
  SELECT
    t.no                                          AS legacy_id,
    t.changedate                                  AS change_date,
    t.createdate                                  AS create_date,
    u.id                                          AS user_id,
    t.subscription_begin                          AS `begin`,
    t.subscription_end                            AS `end`,
    DATE_ADD(t.subscription_end, INTERVAL 14 DAY) AS expiration_date,
    p.id                                          AS offer_id,
    t.automatic_renewal                           AS automatic_renewal
  FROM comppayment_transaction t
    JOIN user_ u ON u.legacy_id = t.participant_id
    JOIN product p ON p.name = t.offer_name AND
                      (p.DTYPE = 'InitialSubscriptionOffer' OR
                       p.DTYPE = 'RenewalOffer')
  WHERE t.counselling = 0 AND
        t.protocolstate IN ('paid', 'closed') AND
        t.subscription_begin IS NOT NULL AND
        t.subscription_end IS NOT NULL;



-- *** II. Migrate indirect subscriptions (donations) ***
-- When a user has no life long subscription yet, but should get it afterwards.
-- It sets automatic_renewal to be true because all life long subcriptions have automatic renewal.

UPDATE subscription s
  JOIN comppayment_transaction t ON s.legacy_id = t.no
SET s.offer_id = (SELECT p.id
                  FROM comppayment_transaction donation
                    JOIN product p ON p.name = donation.offer_name AND p.DTYPE = 'UpgradeOffer'
                  WHERE donation.participant_id = t.participant_id
                        AND donation.offer_name LIKE 'DONAT%'
                        AND donation.protocolstate = 'paid'
                        AND donation.amount > 90
                  ORDER BY donation.createdate DESC
                  LIMIT 1),
  s.automatic_renewal = TRUE
WHERE t.counselling = 0
      AND t.offer_name NOT LIKE 'ERWEIT%'
      AND t.offer_name NOT LIKE 'DONAT%'
      AND t.offer_name NOT LIKE 'OPTIMIER%'
      AND t.offer_name NOT LIKE 'KOSTEN%'
      AND t.offer_name NOT LIKE '%1EUR%'
      AND t.subscription_begin IS NOT NULL
      AND t.subscription_end IS NOT NULL
      -- a donation transaction exists
      AND EXISTS(SELECT 1
                 FROM comppayment_transaction donation
                 WHERE donation.participant_id = t.participant_id
                       AND donation.offer_name LIKE 'DONAT%'
                       AND donation.protocolstate = 'paid'
                       AND donation.amount > 90)
      -- and there are now other normal transactions after
      AND NOT EXISTS(SELECT 1
                     FROM comppayment_transaction normal
                     WHERE normal.participant_id = t.participant_id
                           AND normal.createdate > t.createdate
                           AND normal.counselling = 0
                           AND normal.offer_name NOT LIKE 'ERWEIT%'
                           AND normal.offer_name NOT LIKE 'DONAT%'
                           AND normal.offer_name NOT LIKE 'OPTIMIER%'
                           AND normal.offer_name NOT LIKE 'KOSTEN%'
                           AND normal.subscription_begin IS NOT NULL
                           AND normal.subscription_end IS NOT NULL
                           AND normal.protocolstate IN ('paid', 'closed'));

-- Clean up
CALL DROP_INDEX('product_name', 'product');

-- Update duplicate entries
-- for each duplicated entry (where same end date exists on subscriptions for the same user, increase the time on the entry to prevent the issue (by 1 second)
UPDATE subscription st SET end = DATE_ADD(end, INTERVAL 1 SECOND) WHERE st.id IN
(
  SELECT t_inner.max_sub_id FROM
    (SELECT max(sdel.id) max_sub_id FROM (SELECT count(id) ct, user_id,end FROM subscription s GROUP BY user_id, end) tb
      INNER JOIN subscription sdel ON sdel.user_id = tb.user_id AND sdel.end = tb.end WHERE tb.ct > 1 GROUP BY tb.user_id
    ) t_inner
);

-- *** III. Activate the last subscription of each user ***
-- (expired subscriptions will be deactivated by scheduler automatically)
UPDATE subscription sub_update
SET sub_update.current = TRUE
WHERE sub_update.id IN
(
  SELECT id FROM
    (
      SELECT s1.id FROM
        subscription s1
      WHERE NOT EXISTS
      (
          SELECT 1 FROM
            subscription s2
          WHERE s2.end > s1.end and s1.user_id = s2.user_id
      ) AND s1.expiration_date >= NOW()
    ) as tmp
);

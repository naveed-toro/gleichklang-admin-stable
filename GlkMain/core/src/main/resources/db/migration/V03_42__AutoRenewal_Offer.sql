-- Re-enable renewal offers
UPDATE product
  JOIN comppayment_offer legacy_prod ON product.legacy_id = legacy_prod.no
SET end = legacy_prod.offer_valid_end
WHERE end = '2016-01-01 00:00:00';


-- Set auto-renewal offers for renewal offers on themselves
UPDATE product AS prod
SET prod.auto_renewal_offer_id = prod.id
WHERE prod.DTYPE = 'RenewalOffer' AND prod.auto_renewal_offer_id IS NULL;


-- MySQL doesn't allow to use updatable table in the subquery, therefore we need this hack here
DROP TABLE IF EXISTS product_clone;
CREATE TABLE product_clone LIKE product;
INSERT product_clone SELECT * FROM product;


-- Set auto-renewal offers for initial offers
UPDATE product prod
  JOIN product_clone prod_clone ON prod_clone.id = prod.id
  JOIN product renewal_prod ON renewal_prod.id = (SELECT id
                                                  FROM product_clone
                                                  WHERE DTYPE = 'RenewalOffer' AND
                                                        UPPER(product_clone.name) LIKE 'VER%' AND
                                                        product_clone.end IS NULL AND
                                                        product_clone.amount = prod_clone.amount AND
                                                        product_clone.duration = prod_clone.duration AND
                                                        product_clone.duration_unit = prod_clone.duration_unit
                                                  ORDER BY product_clone.begin DESC
                                                  LIMIT 1)
SET prod.auto_renewal_offer_id = renewal_prod.id
WHERE prod.DTYPE = 'InitialSubscriptionOffer'
      AND prod.auto_renewal_offer_id IS NULL;


-- Copy InitialSubscriptionOffers into RenewalOffers ans set them auto renewal offers
UPDATE product prod
  RIGHT JOIN product renewal_prod ON renewal_prod.legacy_id LIKE 'copy_%'
SET prod.auto_renewal_offer_id = NULL
WHERE prod.DTYPE = 'InitialSubscriptionOffer';

DELETE FROM product WHERE legacy_id LIKE 'copy_%';


-- Insert copies of initial offers as renewal offers for missing auto renewals
INSERT INTO product (legacy_id, change_date, create_date, DTYPE, name, i18n_key,
                     amount, currency, begin, end, for_method, action_code,
                     duration, duration_unit, tariff, auto_renewal_offer_id,
                     upgrade_type, additional)
  (SELECT CONCAT('copy_', legacy_id), change_date, create_date, 'RenewalOffer',
     CONCAT('Verlängerung ', name), CONCAT('copy_', i18n_key), amount,
     currency, begin, end,for_method, action_code, duration, duration_unit,
     tariff, NULL, upgrade_type, additional
   FROM product prod
   WHERE prod.DTYPE = 'InitialSubscriptionOffer' AND
         prod.auto_renewal_offer_id IS NULL);


-- Insert product categories
INSERT INTO subscription_offer_category
(create_date, subscription_offer_id, category)
  SELECT
    NOW()        AS create_date,
    s.id         AS subscription_offer_id,
    "FRIENDSHIP" AS category
  FROM comppayment_offer o
    JOIN product s ON s.legacy_id = CONCAT('copy_', o.no)
  WHERE o.assumed_forms LIKE '%;f;%' AND s.DTYPE = 'RenewalOffer';

INSERT INTO subscription_offer_category
(create_date, subscription_offer_id, category)
  SELECT
    NOW()        AS create_date,
    s.id         AS subscription_offer_id,
    "PARTNERSHIP" AS category
  FROM comppayment_offer o
    JOIN product s ON s.legacy_id = CONCAT('copy_', o.no)
  WHERE o.assumed_forms LIKE '%;p;%' AND s.DTYPE = 'RenewalOffer';


-- Insert product translations
INSERT INTO i18n (legacy_id, change_date, create_date, language, i18n_key,
                  i18n_value, base_name)
  SELECT
    i18n.legacy_id,
    i18n.change_date,
    i18n.create_date,
    i18n.language,
    CONCAT('copy_', i18n.i18n_key) AS i18n_key,
    i18n.i18n_value,
    i18n.base_name
  FROM i18n
    JOIN product prod ON prod.i18n_key = i18n.i18n_key
  WHERE prod.DTYPE = 'InitialSubscriptionOffer' AND
        prod.auto_renewal_offer_id IS NULL;

-- Set auto renewal offers for new inserted offers to themselves
UPDATE product AS prod
SET prod.auto_renewal_offer_id = prod.id
WHERE prod.DTYPE = 'RenewalOffer' AND legacy_id LIKE 'copy_%';

-- Set auto renewal offers for new inserted offers to initial offers
UPDATE product prod
  RIGHT JOIN product renewal_prod ON renewal_prod.legacy_id = CONCAT('copy_', prod.legacy_id)
SET prod.auto_renewal_offer_id = renewal_prod.id
WHERE prod.DTYPE = 'InitialSubscriptionOffer';


-- Set auto-renewal offers for upgrade offers (by price)
UPDATE product AS prod
  JOIN product_clone prod_clone ON prod_clone.id = prod.id
  JOIN product renewal_prod ON renewal_prod.id = (SELECT id
                                                  FROM product_clone
                                                  WHERE DTYPE = 'RenewalOffer' AND
                                                        UPPER(product_clone.name) LIKE 'VER%' AND
                                                        product_clone.end IS NULL AND
                                                        product_clone.amount = prod_clone.amount AND
                                                        product_clone.duration = prod_clone.duration AND
                                                        product_clone.duration_unit = prod_clone.duration_unit
                                                  ORDER BY product_clone.begin DESC
                                                  LIMIT 1)
SET prod.auto_renewal_offer_id = renewal_prod.id
WHERE prod.DTYPE = 'UpgradeOffer' AND UPPER(prod.name) LIKE 'ERWEIT_%'
  AND renewal_prod.DTYPE = 'RenewalOffer' AND UPPER(renewal_prod.name) LIKE 'VER%'
  AND renewal_prod.end IS NULL;


-- Remove temp table
DROP TABLE IF EXISTS product_clone;


-- Set an auto-renewal offer for donation offers: 12-Monate 1EUR PFV120504.
-- Set an upgrade-type donation.
UPDATE product AS prod
  JOIN product renewal_prod ON renewal_prod.legacy_id = '5b810a0225'
SET prod.auto_renewal_offer_id = renewal_prod.id, prod.upgrade_type = 'DONATION'
WHERE prod.DTYPE = 'UpgradeOffer' AND UPPER(prod.name) LIKE 'DONATION_%';

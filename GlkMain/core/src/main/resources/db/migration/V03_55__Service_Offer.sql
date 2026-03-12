-- Idempotence
DELETE FROM i18n
WHERE i18n_key LIKE 'Service_%' OR i18n_key LIKE 'OPTIMIERUNG_%';
DELETE FROM subscription_offer_category
WHERE subscription_offer_id IN (SELECT id FROM product
                                WHERE name LIKE 'Service_%' OR name LIKE 'OPTIMIERUNG_%');
DELETE FROM product
WHERE DTYPE = 'ServiceOffer';

-- Insert service offers
INSERT INTO product (legacy_id, change_date, create_date, DTYPE, name, i18n_key,
                     amount, currency, begin, end)
  SELECT
    no                      AS legacy_id,
    changedate              AS change_date,
    createdate              AS create_date,
    'ServiceOffer'          AS DTYPE,
    offer_name 						  AS name,
    CONCAT(IFNULL(offergroup, "Service"), "_", no)
                            AS i18n_key,
    IF(offer_amount IS NULL,
       IF(offer_minamount IS NULL,
          IF(offer_diffamount IS NULL, 1.00, offer_diffamount),
          offer_minamount),
       offer_amount)           AS amount,
    offer_currency          AS currency,
    IF(offer_valid_begin IS NULL, createdate, offer_valid_begin)
      AS begin,
    offer_valid_end         AS end
  FROM comppayment_offer
  -- Kosten_ are chargeback invoices
  WHERE (counselling = 1 OR UPPER(offer_name) LIKE 'OPTIMIERUNG_%');


-- Insert service offers categories
INSERT INTO subscription_offer_category
(create_date, subscription_offer_id, category)
  SELECT
    NOW()        AS create_date,
    p.id         AS subscription_offer_id,
    "FRIENDSHIP" AS category
  FROM comppayment_offer o
    JOIN product p ON p.legacy_id = o.no
  WHERE p.DTYPE = "ServiceOffer" AND
        (counselling = 1 OR UPPER(offer_name) LIKE 'OPTIMIERUNG_%') AND
        o.assumed_forms LIKE '%;f;%';

INSERT INTO subscription_offer_category
(create_date, subscription_offer_id, category)
  SELECT
    NOW()        AS create_date,
    p.id         AS subscription_offer_id,
    "PARTNERSHIP" AS category
  FROM comppayment_offer o
    JOIN product p ON p.legacy_id = o.no
  WHERE p.DTYPE = "ServiceOffer" AND
        (counselling = 1 OR UPPER(offer_name) LIKE 'OPTIMIERUNG_%') AND
        o.assumed_forms LIKE '%;p;%';


-- Insert service offers translations
INSERT INTO i18n
(legacy_id, change_date, create_date, language, i18n_key, i18n_value, base_name)
  SELECT
    o.no AS legacy_id,
    o.changedate AS change_date,
    o.createdate AS create_date,
    "DE" AS language,
    CONCAT(IFNULL(o.offergroup, "Service"), "_", o.no) AS i18n_key,
    REPLACE(REPLACE(o.offer_description, "[", ""), "]", "") AS i18n_value,
    "PRODUCT_DESCRIPTION" AS base_name
  FROM comppayment_offer o
  WHERE o.offer_description IS NOT NULL AND
        (counselling = 1 OR UPPER(offer_name) LIKE 'OPTIMIERUNG_%');
-- Idempotence
DELETE i18n FROM i18n
  JOIN product ON i18n.i18n_key = product.i18n_key
WHERE product.DTYPE ='UpgradeOffer';

DELETE subscription_offer_category
FROM subscription_offer_category
  JOIN product ON subscription_offer_category.subscription_offer_id = product.id
WHERE product.DTYPE ='UpgradeOffer';

DELETE upgrade_offer_subscription_offer
FROM upgrade_offer_subscription_offer
  JOIN product ON upgrade_offer_subscription_offer.upgrade_offer_id = product.id
WHERE product.DTYPE ='UpgradeOffer';

DELETE FROM product WHERE DTYPE = 'UpgradeOffer';


-- Insert products
INSERT INTO product (legacy_id, change_date, create_date, DTYPE, name, i18n_key,
                     amount, currency, duration, duration_unit, begin, end,
                     action_code, tariff)
SELECT
    no                      AS legacy_id,
    changedate              AS change_date,
    createdate              AS create_date,
    'UpgradeOffer'          AS DTYPE,
    offer_name 						  AS name,
    CONCAT(IFNULL(offergroup, "Standard"), "_", no)
                            AS i18n_key,
    IF(offer_amount IS NULL,
       IF(offer_minamount IS NULL,
          IF(offer_diffamount IS NULL, 1.00, offer_diffamount),
        offer_minamount),
    offer_amount)           AS amount,
    offer_currency          AS currency,
    IF(offer_duration IS NULL,
       IF(UPPER(offer_name) LIKE 'DONATION_%', 6, 12),
    offer_duration)         AS duration,
    'MONTHS'                AS duration_unit,
    IF(offer_valid_begin IS NULL, createdate, offer_valid_begin)
                            AS begin,
    offer_valid_end         AS end,
    actionnumber            AS action_code,
    IF(is_sozialtarif = 1, 'SOCIAL', IF(is_ermaessigungstarif = 1, 'REDUCED', 'STANDARD'))
                            AS tariff
FROM comppayment_offer
-- counselling = 1 means service offers
-- Kosten_ are chargeback invoices
WHERE counselling = 0 AND
      UPPER(offer_name) NOT LIKE 'KOSTEN_%' AND
      UPPER(offer_name) NOT LIKE 'OPTIMIERUNG_%' AND
      (offer_duration IS NULL OR offer_amount IS NULL);


-- Insert product categories.
-- Donation offers don't contain categories explicitly, but should be available for both.
INSERT INTO subscription_offer_category
(create_date, subscription_offer_id, category)
  SELECT
    NOW()        AS create_date,
    s.id         AS subscription_offer_id,
    "FRIENDSHIP" AS category
  FROM comppayment_offer o
    JOIN product s ON s.legacy_id = o.no
  WHERE s.DTYPE = "UpgradeOffer" AND
        counselling = 0 AND
        UPPER(offer_name) NOT LIKE 'KOSTEN_%' AND
        UPPER(offer_name) NOT LIKE 'OPTIMIERUNG_%' AND
        (offer_duration IS NULL OR offer_amount IS NULL) AND
        (o.assumed_forms LIKE '%;f;%' OR o.offer_name LIKE 'DONATION_%');

INSERT INTO subscription_offer_category
(create_date, subscription_offer_id, category)
  SELECT
    NOW()        AS create_date,
    s.id         AS subscription_offer_id,
    "PARTNERSHIP" AS category
  FROM comppayment_offer o
    JOIN product s ON s.legacy_id = o.no
  WHERE s.DTYPE = "UpgradeOffer" AND
        counselling = 0 AND
        UPPER(offer_name) NOT LIKE 'KOSTEN_%' AND
        UPPER(offer_name) NOT LIKE 'OPTIMIERUNG_%' AND
        (offer_duration IS NULL OR offer_amount IS NULL) AND
        (o.assumed_forms LIKE '%;p;%' OR o.offer_name LIKE 'DONATION_%');


-- Insert product translations
INSERT INTO i18n
(legacy_id, change_date, create_date, language, i18n_key, i18n_value, base_name)
  SELECT
    o.no AS legacy_id,
    o.changedate AS change_date,
    o.createdate AS create_date,
    "DE" AS language,
    CONCAT(IFNULL(o.offergroup, "Standard"), "_", o.no) AS i18n_key,
    REPLACE(REPLACE(o.offer_description, "[", ""), "]", "") AS i18n_value,
    "PRODUCT_DESCRIPTION" AS base_name
  FROM comppayment_offer o
    JOIN product p ON p.legacy_id = o.no
  WHERE o.offer_description IS NOT NULL AND
        o.counselling = 0 AND
        UPPER(offer_name) NOT LIKE 'KOSTEN_%' AND
        UPPER(offer_name) NOT LIKE 'OPTIMIERUNG_%' AND
        (o.offer_duration IS NULL OR o.offer_amount IS NULL);
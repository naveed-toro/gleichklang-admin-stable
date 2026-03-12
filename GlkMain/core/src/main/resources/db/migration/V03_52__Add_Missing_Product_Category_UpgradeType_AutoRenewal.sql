SET @renewal_offer_standard = (SELECT p.id FROM product p WHERE p.name = 'VERL PF150629-12-2');
SET @renewal_offer_social = (SELECT p.id FROM product p WHERE p.name = 'VERL PV150316-12-1');

-- Missing categories
CREATE TEMPORARY TABLE IF NOT EXISTS products_with_missing_data
SELECT product.id FROM product
LEFT JOIN subscription_offer_category sc ON product.id = sc.subscription_offer_id
WHERE (product.DTYPE = "UpgradeOffer"
       OR product.DTYPE = "InitialSubscriptionOffer"
       OR product.DTYPE = "RenewalOffer") AND sc.id IS NULL;

INSERT INTO subscription_offer_category
(create_date, subscription_offer_id, category)
  SELECT
    NOW()         AS create_date,
    p.id          AS subscription_offer_id,
    "PARTNERSHIP" AS category
  FROM product p
  WHERE p.id IN (SELECT * FROM products_with_missing_data);

INSERT INTO subscription_offer_category
(create_date, subscription_offer_id, category)
  SELECT
    NOW()         AS create_date,
    p.id          AS subscription_offer_id,
    "FRIENDSHIP" AS category
  FROM product p
  WHERE p.id IN (SELECT * FROM products_with_missing_data);


-- Missing upgrade types
UPDATE product p SET upgrade_type = 'TARIFF_CHANGE' WHERE p.upgrade_type IS NULL;

-- Missing auto renewals
UPDATE product SET auto_renewal_offer_id = @renewal_offer_social WHERE name = 'ERWEIT_54_20150407';

-- Clon missing product
INSERT INTO product (legacy_id, change_date, create_date, DTYPE, name, i18n_key,
                     amount, currency, begin, end, for_method, action_code,
                     duration, duration_unit, tariff, auto_renewal_offer_id,
                     upgrade_type, additional)
  (SELECT CONCAT('copy_', legacy_id), change_date, create_date, DTYPE,
     CONCAT(name, '_KE'), CONCAT('copy_', i18n_key), amount,
     currency, begin, end,for_method, action_code, duration, duration_unit,
     tariff, @renewal_offer_standard, 'CATEGORY_EXTENSION', additional
   FROM product p
   WHERE p.name = 'ERWEIT_84_20150407');

INSERT INTO subscription_offer_category
(create_date, subscription_offer_id, category)
  SELECT
    NOW()        AS create_date,
    p.id         AS subscription_offer_id,
    "PARTNERSHIP" AS category
  FROM product p
  WHERE p.name = 'ERWEIT_84_20150407_KE';

INSERT INTO subscription_offer_category
(create_date, subscription_offer_id, category)
  SELECT
    NOW()        AS create_date,
    p.id         AS subscription_offer_id,
    "FRIENDSHIP" AS category
  FROM product p
  WHERE p.name = 'ERWEIT_84_20150407_KE';

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
    JOIN product p ON p.i18n_key = i18n.i18n_key
  WHERE p.name = 'ERWEIT_84_20150407';

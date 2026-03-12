UPDATE product p, comppayment_offer o
SET p.i18n_key = CONCAT(IFNULL(o.offergroup, "Standard"), "_", o.no)
WHERE p.legacy_id = o.no;

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
  WHERE o.offer_description IS NOT NULL;

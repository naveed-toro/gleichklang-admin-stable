-- Add PARTNERSHIP for all products without partnership category
INSERT INTO subscription_offer_category(create_date, subscription_offer_id, category)
  SELECT
    NOW()        AS create_date,
    p.id         AS subscription_offer_id,
    'PARTNERSHIP' AS category
  FROM product p
  WHERE NOT EXISTS
  (SELECT 1 FROM subscription_offer_category cat
    WHERE cat.subscription_offer_id = p.id AND cat.category = 'PARTNERSHIP');

-- Add FRIENDSHIP for all products without friendship category
INSERT INTO subscription_offer_category(create_date, subscription_offer_id, category)
  SELECT
    NOW()        AS create_date,
    p.id         AS subscription_offer_id,
    'FRIENDSHIP' AS category
  FROM product p
  WHERE NOT EXISTS
  (SELECT 1 FROM subscription_offer_category cat
    WHERE cat.subscription_offer_id = p.id AND cat.category = 'FRIENDSHIP');

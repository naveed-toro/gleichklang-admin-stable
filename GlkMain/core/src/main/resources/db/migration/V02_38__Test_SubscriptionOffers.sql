INSERT INTO product
(
  `create_date`, `DTYPE`, `name`, `i18n_key`,
  `amount`, `currency`,
  `begin`, `end`,
  `initial_offer`, `action_code`,
  `duration`, `duration_unit`,
  `tariff`
)

  VALUES
  (
    NOW(),'SubscriptionOffer','Test P+F','Test_Test P+F',
    25.00,'EUR',
    '2015-11-11 00:00:00', NULL,
    1, 'TEST-ME!',
    1, 'MINUTES',
    'STANDARD'
  ),
  (
    NOW(),'SubscriptionOffer','Test P+F Auto','Test_P+F_Auto',
    1.00,'EUR',
    '2015-11-11 00:00:00', NULL,
    0, 'TEST-ME!',
    1, 'HOURS',
    'STANDARD'
  );

UPDATE product p, product a
SET p. auto_renewal_offer_id = a.id
WHERE p.name = 'Test P+F' AND a.name = 'Test P+F Auto';

INSERT INTO subscription_offer_category
(`create_date`, `subscription_offer_id`, `category`)

  SELECT NOW(), id,'FRIENDSHIP'

  FROM product p WHERE p.name IN('Test P+F', 'Test P+F Auto')
  ORDER BY p.id;

INSERT INTO subscription_offer_category
(`create_date`, `subscription_offer_id`, `category`)

  SELECT NOW(), id,'PARTNERSHIP'

  FROM product p WHERE p.name IN('Test P+F', 'Test P+F Auto')
  ORDER BY p.id;

INSERT INTO i18n
(
  `create_date`, `language`, `i18n_key`, `i18n_value`, `base_name`
)

  VALUES
    (NOW(),'DE','Test_Test P+F','<b>Test Produkt</b>, Laufzeit 1 Minute für 25.00 EUR','PRODUCT_DESCRIPTION'),
    (NOW(),'DE','Test_P+F_Auto','<b>Test Verlängerung</b>, Laufzeit 1 Stunde für 1.00 EUR','PRODUCT_DESCRIPTION');

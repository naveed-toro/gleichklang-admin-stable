INSERT INTO product
(
  `create_date`, `DTYPE`, `name`, `i18n_key`,
  `amount`, `currency`,
  `begin`, `end`,
  `action_code`,
  `duration`, `duration_unit`,
  `tariff`,
  `upgrade_type`
)

  VALUES
  (
    NOW(),'InitialSubscriptionOffer','Test P','Test_Test_P',
    25.00,'EUR',
    '2016-05-11 00:00:00', NULL,
    'TEST-ME!',
    4, 'HOURS',
    'STANDARD',
    NULL
  ),
  (
    NOW(),'RenewalOffer','Test P Auto','Test_P_Auto',
    1.00,'EUR',
    '2016-05-11 00:00:00', NULL,
    'TEST-ME!',
    4, 'HOURS',
    'STANDARD',
    NULL
  ),
  (
    NOW(),'UpgradeOffer','Test Upgrade P+F','Test_Upgrade_P+F',
    25.00,'EUR',
    '2016-05-11 00:00:00', NULL,
    'TEST-ME!',
    4, 'HOURS',
    'STANDARD',
    'CATEGORY_EXTENSION'
  );

UPDATE product p, product a
SET p. auto_renewal_offer_id = a.id
WHERE p.name = 'Test P' AND a.name = 'Test P Auto';

UPDATE product p, product a
SET p. auto_renewal_offer_id = a.id
WHERE p.name = 'Test Upgrade P+F' AND a.name = 'Test P+F Auto';

INSERT INTO upgrade_offer_subscription_offer
(`upgrade_offer_id`, `subscription_offer_id`)
  SELECT u.id, s.id FROM product u, product s
  WHERE u.name = 'Test Upgrade P+F' AND s.name IN ('Test P', 'Test P Auto');

INSERT INTO subscription_offer_category
(`create_date`, `subscription_offer_id`, `category`)

  SELECT NOW(), id,'PARTNERSHIP'

  FROM product p WHERE p.name IN('Test P', 'Test P Auto', 'Test Upgrade P+F')
  ORDER BY p.id;

INSERT INTO subscription_offer_category
(`create_date`, `subscription_offer_id`, `category`)

  SELECT NOW(), id,'FRIENDSHIP'

  FROM product p WHERE p.name IN('Test Upgrade P+F')
  ORDER BY p.id;

INSERT INTO i18n
(
  `create_date`, `language`, `i18n_key`, `i18n_value`, `base_name`
)

  VALUES
    (NOW(),'DE','Test_Test_P','<b>Test Produkt F</b>, Laufzeit 1 Minute für 25.00 EUR','PRODUCT_DESCRIPTION'),
    (NOW(),'DE','Test_P_Auto','<b>Test Verlängerung F</b>, Laufzeit 1 Stunde für 1.00 EUR','PRODUCT_DESCRIPTION'),
    (NOW(),'DE','Test_Upgrade_P+F','<b>Test Upgrade P+F</b>, Laufzeit 1 Stunde für 25.00 EUR','PRODUCT_DESCRIPTION');

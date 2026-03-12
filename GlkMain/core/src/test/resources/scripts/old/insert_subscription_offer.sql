DELETE FROM subscription_offer_category WHERE subscription_offer_id >= 10000;
DELETE FROM subscription WHERE id >= 10000;
DELETE FROM product WHERE id >= 10000;
DELETE FROM user_ WHERE id >= 10000;

INSERT INTO user_ (id, legacy_id, change_date, create_date, alias, birth_date, confirmation_code, email, first_name, last_name, memberStatus, new_mail, password, status_message)
VALUES (10000, '6e32c77836', '2014-12-31 14:15:05', '2014-12-31 13:48:29', 'test_user', '1969-03-10', NULL, 'test@example.com', NULL, NULL, 'ACTIVE', NULL, 'test', NULL);

INSERT INTO product
(id, legacy_id, change_date, create_date, DTYPE, name, i18n_key, amount, currency, begin, end, for_method, initial_offer, action_code, duration, duration_unit, tariff, auto_renewal_offer_id)
VALUES
  (10000, 'test', '2015-04-01 08:52:59', '2015-04-01 08:52:59', 'SubscriptionOffer', 'Normal F36N150405', '3Spezial_7009283bc6', 131.20, 'EUR', '2015-04-05 00:00:00', '2015-04-07 11:00:00', NULL, TRUE, NULL, 36, 'MONTHS', 'STANDARD', 2798);

INSERT INTO subscription (id, legacy_id, change_date, create_date, user_id, begin, end, offer_id, automatic_renewal, version, active)
VALUES (10000, 'test', NOW(), NOW(), 10000, '2013-01-05 13:12:08', '2018-01-05 13:12:08', 10000, TRUE, 0, TRUE);

INSERT INTO subscription_offer_category (subscription_offer_id, category)
VALUES (10000, 'FRIENDSHIP'), (10000, 'PARTNERSHIP');

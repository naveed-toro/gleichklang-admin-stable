SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS offer_payment_option;
DROP TABLE IF EXISTS offer_search_domain;

DROP TABLE IF EXISTS subscription;
DROP TABLE IF EXISTS offer;

CREATE TABLE IF NOT EXISTS subscription
(
  id                  BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  legacy_id           VARCHAR(255),
  change_date         DATETIME,
  create_date         DATETIME,

  user_id             BIGINT   NOT NULL,

  `begin`             DATETIME NULL,
  `end`               DATETIME NULL,

  offer_id            BIGINT   NOT NULL,

  automatic_renewal   BIT      NOT NULL,

  FOREIGN KEY fk_user_id (user_id) REFERENCES user_ (id),
  FOREIGN KEY fk_offer_id (offer_id) REFERENCES product (id)
);

CREATE TABLE IF NOT EXISTS product
(
  id                    BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  legacy_id             VARCHAR(255),
  change_date           DATETIME,
  create_date           DATETIME,

  DTYPE                 VARCHAR(31)  NOT NULL,

  name                  VARCHAR(255) NOT NULL,
  i18n_key              VARCHAR(255),

  amount                NUMERIC(5,2) NOT NULL,
  currency              VARCHAR(3)   NOT NULL,

  begin                 DATETIME NOT NULL,
  end                   DATETIME,

  for_method            VARCHAR(255),

  initial_offer         BIT,
  action_code           VARCHAR(255),

  duration              INT,
  duration_unit         VARCHAR(255),

  tariff                VARCHAR(255),

  auto_renewal_offer_id BIGINT,

  FOREIGN KEY (auto_renewal_offer_id) REFERENCES product (id)
);

CREATE TABLE IF NOT EXISTS subscription_offer_category
(
  subscription_offer_id BIGINT NOT NULL,
  category              VARCHAR(255) NOT NULL,

  FOREIGN KEY (subscription_offer_id) REFERENCES product (id)
);

CREATE TABLE IF NOT EXISTS subscription_offer_update_offer
(
  update_offer_id   BIGINT NOT NULL,
  updates_offer_id  BIGINT NOT NULL,

  FOREIGN KEY (update_offer_id) REFERENCES product (id),
  FOREIGN KEY (updates_offer_id) REFERENCES product (id)
);

CALL CREATE_INDEX('product_legacy_id', 'product', 'legacy_id');
CALL CREATE_INDEX('offer_renewalcode_source', 'comppayment_offer', 'offer_renewalcode_source');
CALL CREATE_INDEX('offer_renewalcode_destination', 'comppayment_offer', 'offer_renewalcode_destination');

INSERT INTO product
	(
		legacy_id, change_date, create_date,

	 	DTYPE,

	 	name, initial_offer, amount, currency,

	 	duration, duration_unit,

	 	begin, end,

	 	action_code,

	 	tariff
	)
	SELECT
	    no                                          AS legacy_id,
    	changedate                                  AS change_date,
    	createdate                                  AS create_date,

    	'SubscriptionOffer'                         AS DTYPE,

		offer_name 						            AS name,
        0                                           AS initial_offer,
    	offer_amount                                AS amount,
    	offer_currency                              AS currency,

        offer_duration                              AS duration,
        'MONTHS'                                    AS duration_unit,

        IF(offer_valid_begin IS NULL, createdate, offer_valid_begin)
                                                    AS begin,
        offer_valid_end                             AS end,

        actionnumber                                AS action_code,

        IF(is_sozialtarif = 1, 'SOCIAL', IF(is_ermaessigungstarif = 1, 'REDUCED', 'STANDARD'))
        			AS tariff
	FROM comppayment_offer
    WHERE counselling = 0 AND UPPER(offer_name) NOT LIKE 'KOSTEN_%' AND offer_duration IS NOT NULL AND offer_amount IS NOT NULL;

INSERT INTO product
	(
		legacy_id, change_date, create_date,

	 	DTYPE,

	 	name, amount, currency,

        for_method,

	 	begin, end
	)
	SELECT
	    no                                          AS legacy_id,
    	changedate                                  AS change_date,
    	createdate                                  AS create_date,

    	'Chargeback'                                AS DTYPE,

		offer_name 						            AS name,
    	offer_amount                                AS amount,
    	offer_currency                              AS currency,

        IF(offer_name LIKE '%_DD', 'DIRECT_DEBIT', 'CREDIT_CARD')
                                                    AS for_method,

        IF(offer_valid_begin IS NULL, MAKEDATE(1974, 1), offer_valid_begin)
                                                    AS begin,
        offer_valid_end                             AS end
	FROM comppayment_offer
    WHERE counselling = 0 AND UPPER(offer_name) LIKE 'KOSTEN_%';

UPDATE product dp
    JOIN comppayment_offer d ON dp.legacy_id = d.no AND d.is_renewal_offer = 1
    JOIN comppayment_offer s ON
        d.offer_renewalcode_destination = s.offer_renewalcode_source
        AND s.assumed_searchdomains = d.offer_searchdomains
    JOIN product sp ON sp.legacy_id = s.no

SET dp.auto_renewal_offer_id = sp.id;

UPDATE product dp
    JOIN comppayment_offer d ON dp.legacy_id = d.no AND d.is_renewal_offer = 1
    JOIN comppayment_offer s ON
        d.offer_renewalcode_destination IS NULL AND s.offer_renewalcode_source IS NULL
        AND s.assumed_searchdomains = d.offer_searchdomains
    	AND s.is_sozialtarif = d.is_sozialtarif AND s.is_ermaessigungstarif = d.is_ermaessigungstarif
    JOIN product sp ON sp.legacy_id = s.no

SET dp.auto_renewal_offer_id = sp.id;

SET FOREIGN_KEY_CHECKS = 1;


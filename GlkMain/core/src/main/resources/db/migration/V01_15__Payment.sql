CREATE TABLE IF NOT EXISTS offer
(
  id                  BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  legacy_id           VARCHAR(255),
  change_date         DATETIME,
  create_date         DATETIME,
  
  name                VARCHAR(255)             NOT NULL,
  type                VARCHAR(255)             NOT NULL,
  tariff              VARCHAR(255)             NOT NULL,
  group_              VARCHAR(255),

  description         VARCHAR(2048),
  automatic_renewal   BIT                      NOT NULL,

  valid_from          DATETIME,
  valid_to            DATETIME,
  
  action_code         VARCHAR(255),

  amount              NUMERIC(5,2)             NOT NULL,
  currency            VARCHAR(3)               NOT NULL,
  
  duration_amount     BIGINT,
  duration_unit       VARCHAR(255),
  
  prolongation_amount BIGINT,
  prolongation_unit   VARCHAR(255)
);
CREATE TABLE IF NOT EXISTS offer_search_domain
(
  offer_id            BIGINT                  NOT NULL,
  search_domain       VARCHAR(255)             NOT NULL,

  FOREIGN KEY (offer_id) REFERENCES offer (id)
);
CREATE TABLE IF NOT EXISTS offer_payment_option
(
  offer_id            BIGINT                   NOT NULL,
  payment_option      VARCHAR(255)             NOT NULL,

  FOREIGN KEY (offer_id) REFERENCES offer (id)
);
CREATE TABLE IF NOT EXISTS subscription
(
  id                  BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  legacy_id           VARCHAR(255),
  change_date         DATETIME,
  create_date         DATETIME,
  
  user_id             VARCHAR(255)             NOT NULL,
  
  `begin`             DATETIME                 NOT NULL,
  `end`               DATETIME                 NOT NULL,
  
  offer_id            BIGINT                   NOT NULL,
  
  state               VARCHAR(255)             NOT NULL,
  
  FOREIGN KEY (offer_id) REFERENCES offer (id)
);
CREATE TABLE IF NOT EXISTS payment
(
  id                  BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  legacy_id           VARCHAR(255),
  change_date         DATETIME,
  create_date         DATETIME,
  
  subscription_id     BIGINT                   NOT NULL,
  external_id         VARCHAR(255)             NOT NULL,
  
  method              VARCHAR(255)             NOT NULL,
  
  amount              NUMERIC(10,2)            NOT NULL,
  currency            VARCHAR(255)             NOT NULL,
  
  FOREIGN KEY (subscription_id) REFERENCES subscription (id)
);
CREATE TABLE IF NOT EXISTS payment_event
(
  id                  BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  legacy_id           VARCHAR(255),
  change_date         DATETIME,
  create_date         DATETIME,
  
  subscription_id     BIGINT,
  external_id         VARCHAR(255)             NOT NULL,
  
  result_code         VARCHAR(255)             NOT NULL,
  result_message      VARCHAR(255)             NOT NULL,
  
  FOREIGN KEY (subscription_id) REFERENCES subscription (id)
);

INSERT INTO offer
	(
		legacy_id, change_date, create_date,
		type, tariff,
	 	name, amount, currency, description,
	 	duration_amount, duration_unit,
	 	valid_from, valid_to,
	 	automatic_renewal, action_code,
	 	prolongation_amount, prolongation_unit,
	 	group_
	)
	SELECT
	    no                              AS legacy_id,
    	changedate                      AS change_date,
    	createdate                      AS create_date,

		IF(is_renewal_offer = 1, 'RENEWAL',
			IF(UPPER(offer_name) LIKE 'ERWEIT_%', 'EXTENSION',
				IF(UPPER(offer_name) LIKE 'DONATION_%', 'DONATION',
					IF(UPPER(offer_name) LIKE 'KOSTEN_%', 'CHARGE_BACK', 'INITIAL')
				)
			)
		)
		    AS type,
		IF(is_sozialtarif = 1, 'SOCIAL', IF(is_ermaessigungstarif = 1, 'REDUCED', 'NORMAL'))
			AS tariff,
		offer_name 						AS name,
    	IF(offer_amount IS NULL, 0.0, offer_amount)
    	    AS amount,
    	offer_currency                  AS currency,
        offer_description               AS description,
        offer_duration                  AS duration_amount,
        CONCAT(offer_duration_unit, 'S')
            AS duration_unit,
        offer_valid_begin               AS valid_from,
        offer_valid_end                 AS valid_to,
        offer_automatic_renewal         AS automatic_renewal,
        actionnumber                    AS action_code,
        offer_prolongation_amount       AS prolongation_amount,
        CONCAT(offer_prolongation_unit, 'S')
            AS prolongation_unit,
        offergroup                      AS group_
	FROM comppayment_offer
    WHERE counselling = 0;

INSERT INTO offer_payment_option
	(offer_id, payment_option)
SELECT
	id AS offer_id,
	'CREDIT_CARD' AS payment_option
FROM comppayment_offer, offer
WHERE offer_payment_types LIKE '%CC%' AND BINARY no = BINARY legacy_id;

INSERT INTO offer_payment_option
	(offer_id, payment_option)
SELECT
	id AS offer_id,
	'DIRECT_DEBIT' AS payment_option
FROM comppayment_offer, offer
WHERE offer_payment_types LIKE '%DD%' AND BINARY no = BINARY legacy_id;

INSERT INTO offer_payment_option
	(offer_id, payment_option)
SELECT
	id AS offer_id,
	'PRE_PAYMENT' AS payment_option
FROM comppayment_offer, offer
WHERE offer_payment_types LIKE '%PP%' AND BINARY no = BINARY  legacy_id;
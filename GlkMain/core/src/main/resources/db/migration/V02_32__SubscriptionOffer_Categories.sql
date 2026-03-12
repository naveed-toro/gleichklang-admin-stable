DROP TABLE IF EXISTS subscription_offer_category;

CREATE TABLE IF NOT EXISTS subscription_offer_category (
  id                       BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  legacy_id                VARCHAR(255),
  change_date              DATETIME,
  create_date              DATETIME,

  subscription_offer_id    BIGINT NOT NULL,
  category                 VARCHAR(255) NOT NULL,

  FOREIGN KEY (subscription_offer_id) REFERENCES product (id),
  UNIQUE KEY subscription_offer_category_subscription_offer_id_category (subscription_offer_id, category)
);


INSERT INTO subscription_offer_category
(create_date, subscription_offer_id, category)
  SELECT
      NOW()        AS create_date,
      s.id         AS subscription_offer_id,
	  "FRIENDSHIP" AS category
  FROM comppayment_offer o
  JOIN product s ON s.legacy_id = o.no
  WHERE o.assumed_forms LIKE '%;f;%' AND s.DTYPE = "SubscriptionOffer";

INSERT INTO subscription_offer_category
(create_date, subscription_offer_id, category)
  SELECT
      NOW()        AS create_date,
      s.id         AS subscription_offer_id,
	  "PARTNERSHIP" AS category
  FROM comppayment_offer o
  JOIN product s ON s.legacy_id = o.no
  WHERE o.assumed_forms LIKE '%;p;%' AND s.DTYPE = "SubscriptionOffer";

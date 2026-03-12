CREATE TABLE IF NOT EXISTS heidelpay_registration
(
  id                BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  change_date       DATETIME,
  create_date       DATETIME,
  unique_id         VARCHAR(255) NOT NULL,
  transaction_id    VARCHAR(255),
  active            BIT          NOT NULL,
  email             VARCHAR(255),
  first_name        VARCHAR(255),
  last_name         VARCHAR(255),
  heidelpay_date    DATETIME,
  KEY(unique_id)
);
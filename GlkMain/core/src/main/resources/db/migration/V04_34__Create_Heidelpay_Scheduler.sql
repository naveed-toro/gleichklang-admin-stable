CREATE TABLE IF NOT EXISTS heidelpay_scheduler
(
  id                BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  change_date       DATETIME,
  create_date       DATETIME,
  unique_id         VARCHAR(255) NOT NULL,
  ref_unique_id     VARCHAR(255),
  transaction_id    VARCHAR(255),
  email             VARCHAR(255),
  first_name        VARCHAR(255),
  last_name         VARCHAR(255),
  heidelpay_date    DATETIME,
  KEY(unique_id),
  KEY(ref_unique_id)
);
DROP TABLE IF EXISTS payment_event;

CREATE TABLE IF NOT EXISTS heidelpay_event
(
  id                  BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  legacy_id           VARCHAR(255),
  change_date         DATETIME,
  create_date         DATETIME,

  payment_id          BIGINT NOT NULL,

  return_code         VARCHAR(255)             NOT NULL,
  return_message      VARCHAR(255)             NOT NULL,
  post_validation     VARCHAR(255)             NOT NULL,

  FOREIGN KEY (payment_id) REFERENCES payment (id)
);

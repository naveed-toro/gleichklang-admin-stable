-- See also: V03_84__Migrate_Payments

CREATE TABLE IF NOT EXISTS affiliate_payment_state
(
  id                BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
  change_date       TIMESTAMP    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  create_date       DATETIME,
  legacy_id         VARCHAR(255),
  affiliate_partner VARCHAR(255) NOT NULL,
  external_id       VARCHAR(255) NOT NULL,
  paid_to_partner   BIT          NOT NULL DEFAULT 0,
  payment_id        BIGINT(20)   NOT NULL,

  FOREIGN KEY (payment_id) REFERENCES payment (id)
);
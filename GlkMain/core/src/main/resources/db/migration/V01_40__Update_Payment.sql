CALL ADD_COLUMN('payment', 'DTYPE', 'VARCHAR(31) NOT NULL');

CALL ADD_COLUMN('payment', 'state', 'VARCHAR(31) NOT NULL');

CALL DROP_FOREIGN_KEY('payment', 'payment_ibfk_1');

ALTER TABLE payment
    CHANGE COLUMN subscription_id created_subscription_id BIGINT NOT NULL;

ALTER TABLE payment
    CHANGE COLUMN external_id external_id VARCHAR(255) NULL;

CALL ADD_COLUMN('payment', 'user_id', 'BIGINT(20) NOT NULL');

CALL ADD_FOREIGN_KEY('payment', 'user_id', 'user_', 'id');

CREATE TABLE IF NOT EXISTS bank_account
(
  id             BIGINT AUTO_INCREMENT PRIMARY KEY NOT NULL,
  legacy_id      VARCHAR(255),
  change_date    DATETIME,
  create_date    DATETIME,
  holder         VARCHAR(255) NOT NULL,
  bank_name      VARCHAR(255) NOT NULL,
  account_number VARCHAR(255) NOT NULL,
  bank_number    VARCHAR(255) NOT NULL,
  iban           VARCHAR(255) NOT NULL,
  bic            VARCHAR(255) NOT NULL,
  country        VARCHAR(255) NOT NULL
);

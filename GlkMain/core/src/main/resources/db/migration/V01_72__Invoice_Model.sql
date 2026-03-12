CREATE TABLE IF NOT EXISTS invoice
(
  id                  BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  legacy_id           VARCHAR(255),
  change_date         DATETIME,
  create_date         DATETIME,
  user_id             BIGINT NOT NULL,
  FOREIGN KEY fk_user_id (user_id) REFERENCES user_ (id)
);

CREATE TABLE IF NOT EXISTS invoice_item
(
  id                  BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  legacy_id           VARCHAR(255),
  change_date         DATETIME,
  create_date         DATETIME,
  invoice_id          BIGINT NOT NULL,
  product_id          BIGINT NOT NULL,
  subscription_id     BIGINT NOT NULL,
  amount              NUMERIC(8, 2) NOT NULL,
  currency            VARCHAR(3) NOT NULL,
  FOREIGN KEY fk_invoice_id (invoice_id) REFERENCES invoice (id),
  FOREIGN KEY fk_subscription_id (subscription_id) REFERENCES subscription (id),
  FOREIGN KEY fk_product_id (product_id) REFERENCES product (id)
);

ALTER TABLE payment
ADD COLUMN invoice_id BIGINT NOT NULL;

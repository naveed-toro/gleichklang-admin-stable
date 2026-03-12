-- drop is save here since we don't have any migrated data yet
DROP TABLE IF EXISTS external_payment_registration;

CREATE TABLE IF NOT EXISTS external_payment_registration
(
  id                  BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  legacy_id           VARCHAR(255),
  change_date         DATETIME,
  create_date         DATETIME,

  version             BIGINT NOT NULL DEFAULT 0,

  last_used_date      DATETIME NOT NULL,
  registration_id     VARCHAR(255) NOT NULL,
  user_id             BIGINT   NOT NULL,

  FOREIGN KEY fk_user_id (user_id) REFERENCES user_ (id),
  UNIQUE KEY external_payment_registration_user_id (user_id),
  UNIQUE KEY external_payment_registration_registration_id (registration_id)
);
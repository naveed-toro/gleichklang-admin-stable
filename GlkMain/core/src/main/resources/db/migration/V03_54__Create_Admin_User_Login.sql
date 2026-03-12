CREATE TABLE IF NOT EXISTS admin_user_login
(
  id                BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  change_date       DATETIME,
  create_date       DATETIME,
  legacy_id         VARCHAR(255),
  user_id           BIGINT(20) NOT NULL UNIQUE,
  admin_id          BIGINT(20) NOT NULL UNIQUE,
  password          VARCHAR(255) NOT NULL,

  FOREIGN KEY (user_id) REFERENCES user_ (id),
  FOREIGN KEY (admin_id) REFERENCES user_ (id)
);
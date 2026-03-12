CREATE TABLE IF NOT EXISTS email_link
(
  id                BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  change_date       DATETIME,
  create_date       DATETIME,
  legacy_id         VARCHAR(255),
  user_id           BIGINT(20) NOT NULL,
  unique_token      VARCHAR(255) NOT NULL UNIQUE,

  FOREIGN KEY (user_id) REFERENCES user_ (id)
);
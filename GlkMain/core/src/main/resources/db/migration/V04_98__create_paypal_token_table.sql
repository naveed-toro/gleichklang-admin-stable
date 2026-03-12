CREATE TABLE IF NOT EXISTS paypal_token
(
  id                BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  create_date       DATETIME,
  change_date       DATETIME,
  token              VARCHAR(500),
  user_id         BIGINT(20),
  FOREIGN KEY (user_id) REFERENCES user_(id)
);
commit;
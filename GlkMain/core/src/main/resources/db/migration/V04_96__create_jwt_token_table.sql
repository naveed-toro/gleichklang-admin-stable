CREATE TABLE IF NOT EXISTS jwt_token
(
  id                BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  create_date       DATETIME,
  change_date       DATETIME,
  user_id         BIGINT(20),
  token              VARCHAR(500),
  FOREIGN KEY (user_id) REFERENCES user_(id)
);
commit;
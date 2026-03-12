CREATE TABLE IF NOT EXISTS scamming
(
  id                     BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  change_date            DATETIME,
  create_date            DATETIME,
  user_id                BIGINT NOT NULL,
  FOREIGN KEY (user_id) REFERENCES user_ (id)
);

CALL CREATE_UNIQUE_INDEX('scamming_user_id', 'scamming', 'user_id');
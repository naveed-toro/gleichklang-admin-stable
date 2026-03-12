CREATE TABLE IF NOT EXISTS admin_work_item
(
  id                BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  change_date       DATETIME,
  create_date       DATETIME,
  legacy_id         VARCHAR(255),
  admin_id          BIGINT(20),
  message_id        BIGINT(20) NOT NULL,
  work_item_status  VARCHAR(255) NOT NULL,

  FOREIGN KEY (message_id) REFERENCES message (id),
  FOREIGN KEY (admin_id) REFERENCES user_(id)
);
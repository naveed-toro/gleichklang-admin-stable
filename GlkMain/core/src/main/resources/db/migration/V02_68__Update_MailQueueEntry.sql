DROP TABLE IF EXISTS mail_queue_entry;

CREATE TABLE IF NOT EXISTS mail_queue_entry
(
  id                BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  change_date       DATETIME,
  create_date       DATETIME,
  legacy_id         VARCHAR(255),

  recipient_email           VARCHAR(255) NOT NULL,
  mail_template             VARCHAR(255) NOT NULL,
  send_count                INT(11) NOT NULL,
  delivery_status           VARCHAR(255) NOT NULL,
  undeliverable_mail_reason VARCHAR(255) NOT NULL
);

CALL CREATE_INDEX('delivery_status', 'mail_queue_entry', 'delivery_status');
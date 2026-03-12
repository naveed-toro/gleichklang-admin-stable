CREATE TABLE IF NOT EXISTS admin_reminder
(
  id                BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  due_date          DATETIME,
  create_date       DATETIME,
  change_date       DATETIME,
  admin_id          BIGINT(20),
  reminder_title     VARCHAR(255) NOT NULL,
  reminder_text     VARCHAR(255) NOT NULL,
  reminder_status   VARCHAR(50) DEFAULT 'NEW',
  reminder_recurrence    VARCHAR(50) DEFAULT 'EVERYONE',
  FOREIGN KEY (admin_id) REFERENCES user_(id)
);
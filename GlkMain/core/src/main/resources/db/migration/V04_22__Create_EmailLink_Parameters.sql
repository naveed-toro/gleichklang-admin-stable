CREATE TABLE IF NOT EXISTS email_link_parameter
(
  id                BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  change_date       DATETIME,
  create_date       DATETIME,
  legacy_id         VARCHAR(255),
  email_link_id     BIGINT(20) NOT NULL,
  parameter_type    VARCHAR(255) NOT NULL,
  parameter_value   VARCHAR(255) NOT NULL,

  FOREIGN KEY (email_link_id) REFERENCES email_link (id)
);

DELETE FROM email_link;
CALL ADD_COLUMN('email_link', 'context', 'VARCHAR(255) NOT NULL');
CALL ADD_COLUMN('user_', 'new_email', 'VARCHAR(255)');
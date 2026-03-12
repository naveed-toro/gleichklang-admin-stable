CREATE TABLE IF NOT EXISTS newsletter
(
  id                     BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  change_date            DATETIME,
  create_date            DATETIME,
  email                  VARCHAR(255) NOT NULL UNIQUE,
  confirmation_ip        VARCHAR(255),
  confirmation_date      DATETIME
);
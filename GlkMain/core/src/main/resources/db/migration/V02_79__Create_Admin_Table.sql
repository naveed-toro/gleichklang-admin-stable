DROP TABLE IF EXISTS admin, user_roles, admin_roles;
CREATE TABLE IF NOT EXISTS admin
(
  id               BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
  legacy_id        VARCHAR(255) ,
  change_date      TIMESTAMP             DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  create_date      DATETIME              DEFAULT NULL,
  alias            VARCHAR(255) NOT NULL,
  email            VARCHAR(255) NOT NULL,
  first_name        VARCHAR(255) DEFAULT NULL,
  last_name         VARCHAR(255) DEFAULT NULL,
  password         VARCHAR(35)  NOT NULL,
  reset_password bit(1) DEFAULT b'0',
  email_confirmed bit(1) DEFAULT b'0',
  UNIQUE KEY `admin_alias` (`alias`),
  UNIQUE KEY `admin_email` (`email`)
);


CREATE TABLE IF NOT EXISTS admin_roles
(
  admin_id BIGINT NOT NULL,
  roles   VARCHAR(225)
);

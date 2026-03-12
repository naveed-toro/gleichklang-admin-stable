CREATE TABLE IF NOT EXISTS active_inactive(
 id                BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 subscription_id   BIGINT(20),
 type              VARCHAR(255),
 user_id           BIGINT(20),
 alias             VARCHAR (255),
 user_type         varchar(255),
 create_date       DATETIME,
 change_date       DATETIME,
 FOREIGN KEY (subscription_id) REFERENCES subscription(id)
 );

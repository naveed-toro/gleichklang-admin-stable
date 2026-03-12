CREATE TABLE IF NOT EXISTS match_
(
    id                       BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    legacy_id                VARCHAR(255),
    change_date              DATETIME,
    create_date              DATETIME,
    source_user_id           BIGINT NOT NULL,
    target_user_id           BIGINT NOT NULL,
    strictness               VARCHAR(255) NOT NULL,
    count                    INT NOT NULL,
    category                 VARCHAR(255) NOT NULL,
    FOREIGN KEY (source_user_id)   REFERENCES user_ (id),
    FOREIGN KEY (target_user_id)   REFERENCES user_ (id)
);
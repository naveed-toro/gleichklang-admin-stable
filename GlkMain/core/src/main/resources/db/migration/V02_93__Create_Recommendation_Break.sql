CREATE TABLE IF NOT EXISTS recommendation_break
(
    id                      BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    legacy_id               VARCHAR(255),
    change_date             DATETIME,
    create_date             DATETIME,

    user_id                 BIGINT NOT NULL,
    category                VARCHAR(255) NOT NULL,
    end_date                DATE,
    FOREIGN KEY (user_id)   REFERENCES user_ (id),
    UNIQUE KEY user_id_category (user_id, category)
);
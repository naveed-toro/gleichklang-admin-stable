CREATE TABLE IF NOT EXISTS media_gallery
(
    id                      BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    legacy_id               VARCHAR(255),
    change_date             DATETIME,
    create_date             DATETIME,

    name                    VARCHAR(255),
    author_id               BIGINT NOT NULL,
    visible_category        VARCHAR(255),
    visible_affiliation     VARCHAR(255),
    FOREIGN KEY (author_id) REFERENCES user_ (id)
);

CREATE TABLE IF NOT EXISTS media
(
    id                             BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    legacy_id                      VARCHAR(255),
    change_date                    DATETIME,
    create_date                    DATETIME,

    file_id                        BIGINT NOT NULL,
    media_gallery_id               BIGINT NOT NULL,

    FOREIGN KEY (file_id)          REFERENCES file (id),
    FOREIGN KEY (media_gallery_id) REFERENCES media_gallery (id)
);
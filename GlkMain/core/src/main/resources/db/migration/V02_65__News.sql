CREATE TABLE IF NOT EXISTS news
(
  id                BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  title             VARCHAR(255),
  teaser_text       TEXT,
  text              TEXT,
  valid_from        DATETIME,
  valid_to          DATETIME,
  active            BIT                      NOT NULL,
  legacy_id         VARCHAR(255),
  change_date       DATETIME,
  create_date       DATETIME,
  language          VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS user_news
(
  id           BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  hide         BIT,
  user_id      BIGINT                  NOT NULL,
  news_id      BIGINT                  NOT NULL,
  legacy_id    VARCHAR(255),
  change_date  DATETIME,
  create_date  DATETIME,
  FOREIGN KEY (user_id) REFERENCES user_ (id),
  FOREIGN KEY (news_id) REFERENCES news (id)
);
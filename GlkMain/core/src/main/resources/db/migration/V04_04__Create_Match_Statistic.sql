CREATE TABLE IF NOT EXISTS match_statistic
(
  id                BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  change_date       DATETIME,
  create_date       DATETIME,
  legacy_id         VARCHAR(255),
  matching_scope    VARCHAR(255) NOT NULL,
  start_date        DATETIME NOT NULL,
  end_date          DATETIME
);

CREATE TABLE IF NOT EXISTS match_statistic_entry
(
  id                               BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  change_date                      DATETIME,
  create_date                      DATETIME,
  legacy_id                        VARCHAR(255),
  log_area                         VARCHAR(255) NOT NULL,
  duration                         BIGINT NOT NULL,
  number                           BIGINT NOT NULL,
  match_statistic_id               BIGINT NOT NULL,

  FOREIGN KEY (match_statistic_id) REFERENCES match_statistic (id)
);

CREATE TABLE IF NOT EXISTS area_match_statistic_entry
(
  id                               BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  change_date                      DATETIME,
  create_date                      DATETIME,
  legacy_id                        VARCHAR(255),
  log_match_area                   VARCHAR(255) NOT NULL,
  duration                         BIGINT NOT NULL,
  number                           BIGINT NOT NULL,
  match_statistic_id               BIGINT NOT NULL,

  FOREIGN KEY (match_statistic_id) REFERENCES match_statistic (id)
);

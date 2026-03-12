DROP TABLE audio;

commit;
CREATE TABLE IF NOT EXISTS audio
(
  id                BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  create_date       DATETIME,
  change_date       DATETIME,
  deleted           BIT(1),
  author_id         BIGINT(20),
  name              VARCHAR(255) NOT NULL,
  path              VARCHAR(500),
  for_partnership   boolean DEFAULT FALSE,
  for_friendship    boolean DEFAULT FALSE,
  FOREIGN KEY (author_id) REFERENCES user_(id)
);
commit;
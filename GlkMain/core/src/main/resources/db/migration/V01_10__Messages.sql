DROP TABLE IF EXISTS envelope;
DROP TABLE IF EXISTS message;

CREATE TABLE IF NOT EXISTS message
(
  id         VARCHAR(255) PRIMARY KEY NOT NULL,
  changeDate DATETIME,
  createDate DATETIME,
  subject    VARCHAR(255)             NOT NULL,
  body       VARCHAR(255)             NOT NULL,
  sent       BIT                      NOT NULL,
  send_date  DATETIME,
  category   VARCHAR(255)             NOT NULL
);

CREATE TABLE IF NOT EXISTS envelope
(
  DTYPE      VARCHAR(31)              NOT NULL,
  id         VARCHAR(255) PRIMARY KEY NOT NULL,
  changeDate DATETIME,
  createDate DATETIME,
  user_id    BIGINT(20),
  message_id VARCHAR(255),
  sender_id  VARCHAR(255),
  deleted    BIT                      NOT NULL,
  `read`     BIT,
  #   FOREIGN KEY (user_id) REFERENCES user_ (legacy_id),
  FOREIGN KEY (message_id) REFERENCES message (id),
  FOREIGN KEY (sender_id) REFERENCES envelope (id),
  FOREIGN KEY (user_id) REFERENCES user_(id)
);
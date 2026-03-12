CREATE TABLE IF NOT EXISTS client_information
(
  id                      BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  legacy_id               VARCHAR(255),
  change_date             DATETIME,
  create_date             DATETIME,

  user_id                 BIGINT NOT NULL,
  browser                 VARCHAR(255) NOT NULL,
  browser_major_version   INT NOT NULL,
  browser_minor_version   INT NOT NULL,
  browser_outdated        BIT,
  browser_height          INT,
  browser_width           INT,
  country                 VARCHAR(255),
  touch_device            BIT,
  language                VARCHAR(255),
  layout                  VARCHAR(255),
  screen_height           INT,
  screen_width            INT,
  timezone_offset         INT,
  os                      VARCHAR(255) NOT NULL,

  FOREIGN KEY (user_id)   REFERENCES user_ (id)
);

CALL CREATE_UNIQUE_INDEX('user_id_browser_browser_minor_version_browser_major_version_os', 'client_information', 'user_id, browser, browser_minor_version, browser_major_version, os');
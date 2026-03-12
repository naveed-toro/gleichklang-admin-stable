DROP TABLE IF EXISTS after_cancel;
DROP TABLE IF EXISTS cancel_reason;
DROP TABLE IF EXISTS address;
DROP TABLE IF EXISTS file;
DROP TABLE IF EXISTS user_roles;
DROP TABLE IF EXISTS user_settings;
DROP TABLE IF EXISTS user_;


CREATE TABLE IF NOT EXISTS address
(
  id               BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
  legacy_id        VARCHAR(255),
  change_date      TIMESTAMP             DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  create_date      DATETIME              DEFAULT NULL,
  city             VARCHAR(255),
  country          VARCHAR(255),
  streetWithNumber VARCHAR(255),
  zip              VARCHAR(255),
  user_id          BIGINT
);

CREATE TABLE IF NOT EXISTS file
(
  id               BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
  legacy_id        VARCHAR(255) DEFAULT NULL,
  change_date      TIMESTAMP             DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  create_date      DATETIME              DEFAULT NULL,
  name                 VARCHAR(255),
  relationshipCategory VARCHAR(255),
  type                 VARCHAR(5)
);

CREATE TABLE IF NOT EXISTS user_
(
  id               BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
  legacy_id        VARCHAR(255) ,
  change_date      TIMESTAMP             DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  create_date      DATETIME              DEFAULT NULL,
  alias            VARCHAR(255) NULL,
  birthDate        DATE         NULL,
  confirmationCode VARCHAR(255),
  email            VARCHAR(255),
  firstName        VARCHAR(255),
  lastName         VARCHAR(255),
  memberStatus     VARCHAR(255),
  newMail          VARCHAR(255),
  password         VARCHAR(35)  NOT NULL,
  sex              VARCHAR(10)  NULL,
  statusMessage    VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS user_roles
(
  user_id VARCHAR(255) NOT NULL,
  roles   VARCHAR(25)
);

CREATE TABLE IF NOT EXISTS user_settings
(
  id               BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
  legacy_id        VARCHAR(255),
  change_date      TIMESTAMP             DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  create_date      DATETIME              DEFAULT NULL,
  cancellationPolicyAccepted BIT DEFAULT FALSE,
  communityRulesAccepted     BIT DEFAULT FALSE,
  disableAds                 BIT DEFAULT FALSE,
  disableFurtherInfo         BIT DEFAULT FALSE,
  generalTermsAccepted       BIT DEFAULT FALSE,
  mailBlocked                BIT DEFAULT FALSE,
  matchmakingSuccess         BIT DEFAULT FALSE ,
  privacyPolicyAccepted      BIT DEFAULT FALSE,
  satisfactionGleichklang    VARCHAR(25) NULL,
  satisfactionOther          VARCHAR(25) NULL,
  sourcePage                 VARCHAR(500),
  sourceText                 VARCHAR(255),
  talkToPress                BIT DEFAULT FALSE ,
  disableRecommendationNotifications BIT(1) DEFAULT b'0' NOT NULL,
  disableCipherMessageNotifications BIT(1) DEFAULT b'0' NOT NULL,
  disablePositiveRankingNotifications BIT(1) DEFAULT b'0' NOT NULL,
  disableInfoMails BIT(1) DEFAULT b'0' NOT NULL,
  user_id BIGINT(20) NOT NULL,
  CONSTRAINT user_settings_ibfk_1 FOREIGN KEY (user_id) REFERENCES user_ (id)
);

CREATE TABLE IF NOT EXISTS cancel_reason
(
  user_id       BIGINT(20) NOT NULL,
  cancelReasons VARCHAR(255),
  FOREIGN KEY (user_id) REFERENCES user_ (id)
);

CREATE TABLE IF NOT EXISTS after_cancel
(
  user_id     BIGINT(20) NOT NULL,
  afterCancel VARCHAR(255),
  FOREIGN KEY (user_id) REFERENCES user_ (id)
);



CREATE INDEX user_id ON user_settings (user_id);

CALL ADD_FOREIGN_KEY('address', 'user_id', 'user_', 'id');
CALL CREATE_INDEX('FK_7rod8a71yep5vxasb0ms3osbg', 'address', 'user_id');
CALL CREATE_UNIQUE_INDEX('user_alias', 'user_', 'alias');
CALL CREATE_UNIQUE_INDEX('user_email', 'user_', 'email');

CREATE TABLE IF NOT EXISTS schema_version_history
(
  id             BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
  installed_rank INT(11)       NOT NULL,
  version        VARCHAR(50)   NOT NULL,
  description    VARCHAR(200)  NOT NULL,
  type           VARCHAR(20)   NOT NULL,
  script         VARCHAR(1000) NOT NULL,
  checksum       INT(11)                DEFAULT NULL,
  installed_by   VARCHAR(100)  NOT NULL,
  installed_on   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  execution_time INT(11)       NOT NULL,
  success        TINYINT(1)    NOT NULL,

  svn_revision   VARCHAR(255)  NOT NULL,
  build_number   VARCHAR(255)  NOT NULL
);

-- we run this script before any flyway invocation, we have to make sure
-- that if running for the first time that schema_version exists
CREATE TABLE IF NOT EXISTS schema_version
(
  installed_rank INT(11)       NOT NULL,
  version        VARCHAR(50)   NOT NULL,
  description    VARCHAR(200)  NOT NULL,
  type           VARCHAR(20)   NOT NULL,
  script         VARCHAR(1000) NOT NULL,
  checksum       INT(11)                DEFAULT NULL,
  installed_by   VARCHAR(100)  NOT NULL,
  installed_on   TIMESTAMP     NOT NULL DEFAULT current_timestamp,
  execution_time INT(11)       NOT NULL,
  success        TINYINT(1)    NOT NULL,
  PRIMARY KEY (`version`)
);

CREATE TABLE IF NOT EXISTS stat_history
(TABLE_NAME  VARCHAR(255),
 ROW_COUNT   INT(11),
 change_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

INSERT INTO schema_version_history
(
  installed_rank,

  version,
  description,

  type, script, checksum,

  installed_by, installed_on,

  execution_time,

  success,

  svn_revision, build_number
)
  SELECT
    installed_rank,

    version,
    description,

    type,
    script,
    checksum,

    installed_by,
    installed_on,

    execution_time,

    success,

    "${buildNumber}"          AS svn_revision,
    -- the maven plugin stores the svn revision as buildNumber
    "${jenkins.build_number}" AS build_number -- the jenkins build number must be passed as maven property -Djenkins.build_number=${BUILD_NUMBER}
  FROM schema_version;
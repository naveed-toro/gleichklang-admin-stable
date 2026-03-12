-- migrate new with_relocation column
CALL ADD_COLUMN('question', 'with_relocation', 'BIT');

UPDATE question q
LEFT JOIN question_group qg ON q.question_group_id = qg.id
LEFT JOIN questionnaire qn ON qg.questionnaire_id = qn.id
SET with_relocation = (qn.recommendation_category = 'PARTNERSHIP') WHERE dtype = 'RegionQuestion';

-- recreate for idempotent
CREATE TABLE IF NOT EXISTS locatable_search_request
(
  id            BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  DTYPE         VARCHAR(31),
  change_date   DATETIME,
  create_date   DATETIME,
  legacy_id     VARCHAR(255),
  answer_id     BIGINT,
  center_zip_id BIGINT,
  distance      INT,
  locatable_id  BIGINT,

  FOREIGN KEY (center_zip_id) REFERENCES locatable (id),
  FOREIGN KEY (answer_id) REFERENCES answer (id)
);

CREATE TABLE IF NOT EXISTS locatable_search_request_restriction
(
  locatable_search_request_id   BIGINT NOT NULL,
  locatable_id                  BIGINT NOT NULL,

  PRIMARY KEY (locatable_search_request_id,locatable_id),
  FOREIGN KEY (locatable_search_request_id) REFERENCES locatable_search_request (id),
  FOREIGN KEY (locatable_id) REFERENCES locatable (id)
);

-- create new tables
CREATE TABLE IF NOT EXISTS proximity_search_request
(
  id                BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  change_date       DATETIME,
  create_date       DATETIME,
  legacy_id         VARCHAR(255),
  answer_id         BIGINT NOT NULL,
  center_zip_id     BIGINT NOT NULL,
  distance          INT NOT NULL,
  restrict_country  BIT NOT NULL,

  FOREIGN KEY (answer_id) REFERENCES answer (id),
  FOREIGN KEY (center_zip_id) REFERENCES locatable (id)
);

CREATE TABLE IF NOT EXISTS region_search_request
(
  id                BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  change_date       DATETIME,
  create_date       DATETIME,
  legacy_id         VARCHAR(255),
  answer_id         BIGINT NOT NULL,
  continent_id      BIGINT,
  country_id        BIGINT,

  FOREIGN KEY (answer_id) REFERENCES answer (id),
  FOREIGN KEY (continent_id) REFERENCES locatable (id),
  FOREIGN KEY (country_id) REFERENCES locatable (id)
);

CREATE TABLE IF NOT EXISTS region_search_request_restriction
(
  region_search_request_id   BIGINT NOT NULL,
  locatable_id               BIGINT NOT NULL,

  PRIMARY KEY (region_search_request_id, locatable_id),
  FOREIGN KEY (region_search_request_id) REFERENCES region_search_request (id),
  FOREIGN KEY (locatable_id) REFERENCES locatable (id)
);


-- migrate new proximity_search_request
INSERT INTO proximity_search_request (change_date, create_date, legacy_id, answer_id, center_zip_id, distance, restrict_country)
    SELECT
    change_date,
    create_date,
    legacy_id,
    answer_id,
    center_zip_id,
    distance,
    EXISTS(SELECT 1 FROM locatable_search_request_restriction WHERE locatable_search_request_id = id)
    FROM locatable_search_request WHERE dtype = 'ProximityLocatableSearchRequest';
-- UPDATE locatable_search_request SET restrict_country = true WHERE id IN (SELECT locatable_id FROM locatable_search_request_restriction) AND dtype = 'ProximitySearchRequest';
-- UPDATE locatable_search_request SET restrict_country = false WHERE restrict_country IS NULL AND dtype = 'ProximitySearchRequest';

-- migrate new region_search_request
CALL ADD_COLUMN('region_search_request', 'temp_id', 'BIGINT');
CALL ADD_FOREIGN_KEY('region_search_request', 'temp_id', 'locatable_search_request', 'id');

INSERT INTO region_search_request (change_date, create_date, legacy_id, answer_id, temp_id)
    SELECT
    change_date,
    create_date,
    legacy_id,
    answer_id,
    id
    FROM locatable_search_request WHERE dtype = 'LocatableSearchRequest';

UPDATE region_search_request lsr
LEFT JOIN locatable_search_request_restriction lsrr ON lsr.temp_id = lsrr.locatable_search_request_id
LEFT JOIN locatable l ON l.id = lsrr.locatable_id
LEFT JOIN locatable l_parent ON l_parent.id = l.parent_id
SET lsr.country_id = l_parent.id, lsr.continent_id = l_parent.parent_id WHERE l.dtype = 'Region';

UPDATE region_search_request lsr
LEFT JOIN locatable_search_request_restriction lsrr ON lsr.temp_id = lsrr.locatable_search_request_id
LEFT JOIN locatable l ON l.id = lsrr.locatable_id
SET lsr.country_id = l.id, lsr.continent_id = l.parent_id WHERE l.dtype = 'Country';

UPDATE region_search_request lsr
LEFT JOIN locatable_search_request_restriction lsrr ON lsr.temp_id = lsrr.locatable_search_request_id
LEFT JOIN locatable l ON l.id = lsrr.locatable_id
SET lsr.continent_id = l.id WHERE l.dtype = 'Continent';

INSERT INTO region_search_request_restriction (region_search_request_id, locatable_id)
    SELECT
    rsr.id,
    lsrr.locatable_id
    FROM locatable_search_request_restriction lsrr LEFT JOIN locatable l ON lsrr.locatable_id = l.id LEFT JOIN region_search_request rsr ON lsrr.locatable_search_request_id = rsr.temp_id
    WHERE l.dtype = 'Region';

CALL DROP_FOREIGN_KEY('region_search_request', 'region_search_request_ibfk_4');
CALL DROP_COLUMN('region_search_request', 'temp_id');

-- remove old tables
DROP TABLE locatable_search_request_restriction;
DROP TABLE locatable_search_request;

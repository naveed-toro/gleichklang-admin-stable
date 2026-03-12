UPDATE locatable
SET legacy_id = 'DE-BR'
WHERE legacy_id = 'DE-BB';

DROP TABLE IF EXISTS migration_locatable;
CREATE TABLE migration_locatable
(
  locatable_id              BIGINT(20) NOT NULL DEFAULT '0',
  locatable_key             VARCHAR(255),
  parent_locatable_id       BIGINT(20),
  parent_locatable_key      VARCHAR(255),
  grandparent_locatable_id  BIGINT(20),
  grandparent_locatable_key VARCHAR(255),
  DTYPE                     VARCHAR(20)
);

INSERT INTO migration_locatable (locatable_id, locatable_key, parent_locatable_id, parent_locatable_key, grandparent_locatable_id, grandparent_locatable_key, DTYPE)
  SELECT
    l1.id,
    l1.legacy_id,
    l1.parent_id,
    l2.i18n_key,
    l3.id,
    l3.i18n_key,
    l1.DTYPE
  FROM locatable AS l1
    LEFT JOIN locatable AS l2
      ON l1.parent_id = l2.id
    LEFT JOIN locatable AS l3
      ON l2.parent_id = l3.id
  WHERE
    (l1.i18n_key REGEXP '^DE|^AT|^CH' AND l1.DTYPE = 'Region')
    OR
    (l1.i18n_key IN ('DE', 'AT', 'CH') AND l1.DTYPE = 'Country')
    OR l1.i18n_key = 'EU';

# get users which have regional restrictions
DROP TABLE IF EXISTS migration_user_with_regions;
CREATE TABLE migration_user_with_regions
(
  type      VARCHAR(255),
  user_id   BIGINT(20),
  legacy_id VARCHAR(75)
);

INSERT INTO migration_user_with_regions
  SELECT
    'partner' AS type,
    u.id,
    u.legacy_id
  FROM user_ u
  WHERE u.id NOT IN
        (
          SELECT u.id AS user_id
          FROM comppartner AS cp
            JOIN user_ AS u
              ON cp.owner = u.legacy_id
          WHERE
            cp.p_region = ';;'
            OR cp.p_region IS NULL
            OR cp.p_region REGEXP ';INT;')
        AND u.legacy_id IS NOT NULL;

INSERT INTO migration_user_with_regions
  SELECT
    'friend' AS type,
    u.id,
    u.legacy_id
  FROM user_ u
  WHERE u.id NOT IN
        (
          SELECT u.id AS user_id
          FROM compfreund AS cp
            JOIN user_ AS u
              ON cp.owner = u.legacy_id
          WHERE
            cp.f_region = ';;'
            OR cp.f_region IS NULL
            OR cp.f_region REGEXP ';INT;')
        AND u.legacy_id IS NOT NULL;

DROP TABLE IF EXISTS answer_region_temp;
CREATE TABLE answer_region_temp (
  DTYPE                     VARCHAR(12) NOT NULL DEFAULT '',
  legacy_id                 VARCHAR(75) NOT NULL DEFAULT '',
  change_date               DATETIME,
  create_date               DATETIME,
  p_region                  LONGTEXT,
  question_id               BIGINT(20)  NOT NULL DEFAULT '0',
  user_id                   BIGINT(20)  NOT NULL DEFAULT '0',
  locatable_key             VARCHAR(255),
  locatable_id              BIGINT(20)  NOT NULL DEFAULT '0',
  parent_locatable_id       BIGINT(20),
  parent_locatable_key      VARCHAR(255),
  grandparent_locatable_id  BIGINT(20),
  grandparent_locatable_key VARCHAR(255),
  deleted                   TINYINT(1)           DEFAULT '0'
);

CALL CREATE_INDEX('locatable_legacy_idx', 'locatable', 'legacy_id');

CALL CREATE_INDEX('migration_locatable_locatable_id_idx', 'migration_locatable', 'locatable_id');
CALL CREATE_INDEX('migration_locatable_locatable_key_idx', 'migration_locatable', 'locatable_key');
CALL CREATE_INDEX('migration_locatable_parent_locatable_id_idx', 'migration_locatable', 'parent_locatable_id');
CALL CREATE_INDEX('migration_locatable_parent_locatable_key_idx', 'migration_locatable', 'parent_locatable_key');
CALL CREATE_INDEX('migration_locatable_grandparent_locatable_id_idx', 'migration_locatable',
                  'grandparent_locatable_id');
CALL CREATE_INDEX('migration_locatable_grandparent_locatable_key_idx', 'migration_locatable', 
                  'grandparent_locatable_key');
CALL CREATE_INDEX('migration_locatable_DTYPE_idx', 'migration_locatable', 'DTYPE');

CALL CREATE_INDEX('migration_user_with_regions_type', 'migration_user_with_regions',
                  'type');
CALL CREATE_INDEX('migration_user_with_regions_user_id', 'migration_user_with_regions',
                  'user_id');
CALL CREATE_INDEX('migration_user_with_regions_legacy_id', 'migration_user_with_regions', 'legacy_id');

CALL CREATE_INDEX('answer_region_temp_DTYPE', 'answer_region_temp', 'DTYPE');
CALL CREATE_INDEX('answer_region_temp_legacy_id', 'answer_region_temp', 'legacy_id');
CALL CREATE_INDEX('answer_region_temp_question_id', 'answer_region_temp', 'question_id');
CALL CREATE_INDEX('answer_region_temp_locatable_key', 'answer_region_temp', 'locatable_key');
CALL CREATE_INDEX('answer_region_temp_locatable_id', 'answer_region_temp', 'locatable_id');
CALL CREATE_INDEX('answer_region_temp_parent_locatable_id', 'answer_region_temp', 'parent_locatable_id');
CALL CREATE_INDEX('answer_region_temp_parent_locatable_key', 'answer_region_temp', 'parent_locatable_key');
CALL CREATE_INDEX('answer_region_temp_parent_grandparent_locatable_id', 'answer_region_temp',
                  'grandparent_locatable_id');
CALL CREATE_INDEX('answer_region_temp_grandparent_locatable_key', 'answer_region_temp', 'grandparent_locatable_key');

SET @start := NOW();

SELECT q.id
INTO @question_id
FROM question q
WHERE q.i18n_key = 'partner.region_question';

INSERT INTO answer_region_temp (DTYPE, legacy_id, change_date, create_date, p_region, question_id, user_id, locatable_key, locatable_id, parent_locatable_id, parent_locatable_key, grandparent_locatable_id, grandparent_locatable_key)
  SELECT
    'RegionAnswer'              AS DTYPE,
    cp.no                       AS legacy_id,
    cp.changedate               AS change_date,
    cp.createdate               AS create_date,
    cp.p_region,
    @question_id                AS question_id,
    u.user_id                   AS user_id,
    l.locatable_key             AS locatable_key,
    l.locatable_id              AS locatable_id,
    l.parent_locatable_id       AS parent_locatable_id,
    l.parent_locatable_key      AS parent_locatable_key,
    l.grandparent_locatable_id  AS grandparent_locatable_id,
    l.grandparent_locatable_key AS grandparent_locatable_key
  FROM comppartner AS cp
    JOIN migration_user_with_regions AS u
      ON cp.owner = u.legacy_id AND u.type = 'partner'
    JOIN migration_locatable AS l
      ON cp.p_region LIKE (CONCAT('%;', l.locatable_key, ';%'));

CALL TRACK_QUERY('1.91', 'insert into answer_region_temp partner', @start);

SET @start := NOW();

SELECT q.id
INTO @question_id
FROM question q
WHERE q.i18n_key = 'friend.region_question';

INSERT INTO answer_region_temp (DTYPE, legacy_id, change_date, create_date, p_region, question_id, user_id, locatable_key, locatable_id, parent_locatable_id, parent_locatable_key, grandparent_locatable_id, grandparent_locatable_key)
  SELECT
    'RegionAnswer'              AS DTYPE,
    cf.no                       AS legacy_id,
    cf.changedate               AS change_date,
    cf.createdate               AS create_date,
    cf.f_region,
    @question_id                AS question_id,
    u.user_id                   AS user_id,
    l.locatable_key             AS locatable_key,
    l.locatable_id              AS locatable_id,
    l.parent_locatable_id       AS parent_locatable_id,
    l.parent_locatable_key      AS parent_locatable_key,
    l.grandparent_locatable_id  AS grandparent_locatable_id,
    l.grandparent_locatable_key AS grandparent_locatable_key
  FROM compfreund AS cf
    JOIN migration_user_with_regions AS u
      ON cf.owner = u.legacy_id AND u.type = 'friend'
    JOIN migration_locatable AS l
      ON cf.f_region LIKE (CONCAT('%;', l.locatable_key, ';%'));

CALL TRACK_QUERY('1.91', 'insert into answer_region_temp friend', @start);


SET @start := NOW();

-- this query mark values of a lower granularity as deleted if parent exist in the request
-- to turn it otherwise set art2.deleted = true
UPDATE answer_region_temp AS art
  JOIN answer_region_temp AS art2
    ON art.legacy_id = art2.legacy_id AND
       (art.locatable_id = art2.parent_locatable_id OR
        art.locatable_id = art2.grandparent_locatable_id)
SET art2.deleted = TRUE;


CALL TRACK_QUERY('1.91', 'mark values of a lower granularity as deleted if parent exist in the request', @start);

DELETE FROM locatable_search_request_restriction;
DELETE FROM locatable_search_request;
DELETE FROM answer
WHERE DTYPE = 'RegionAnswer';

SET @start := NOW();

INSERT INTO answer (DTYPE, legacy_id, change_date, create_date, question_id, user_id)
  SELECT
    'RegionAnswer' AS DTYPE,
    legacy_id,
    change_date,
    create_date,
    question_id,
    user_id
  FROM answer_region_temp
  WHERE deleted IS FALSE
  GROUP BY user_id, question_id;

CALL TRACK_QUERY('1.91', 'insert into answer', @start);

SET @start := NOW();

# group regions by parent
INSERT INTO locatable_search_request (change_date, create_date, legacy_id, answer_id, DTYPE)
  SELECT
    art.change_date,
    art.create_date,
    art.legacy_id,
    a.id                     AS answer_id,
    'LocatableSearchRequest' AS DTYPE
  FROM answer AS a
    JOIN answer_region_temp AS art
      ON a.legacy_id = art.legacy_id
  WHERE art.deleted IS FALSE
        AND a.DTYPE = 'RegionAnswer'
        AND art.locatable_key LIKE 'states.%'
  GROUP BY a.id, art.parent_locatable_id;

CALL TRACK_QUERY('1.91', 'insert into locatable_search_request grouped by parent', @start);


SET @start := NOW();
INSERT INTO locatable_search_request_restriction (locatable_search_request_id, locatable_id)
  SELECT
    lsr.id AS locatable_search_request_id,
    art.locatable_id
  FROM answer_region_temp AS art
    JOIN locatable_search_request AS lsr
      ON art.legacy_id = lsr.legacy_id
  WHERE art.deleted IS FALSE
  GROUP BY lsr.id, art.locatable_id;

CALL TRACK_QUERY('1.91', 'start insert into locatable_search_request_restriction', @start);


CALL ADD_COLUMN('locatable_search_request', 'locatable_id', 'BIGINT NULL');

SET @start := NOW();
INSERT INTO locatable_search_request (change_date, create_date, legacy_id, answer_id, DTYPE, locatable_id)
  SELECT
    art.change_date,
    art.create_date,
    art.legacy_id,
    a.id                     AS answer_id,
    'LocatableSearchRequest' AS DTYPE,
    art.locatable_id
  FROM answer AS a
    JOIN answer_region_temp AS art
      ON a.legacy_id = art.legacy_id
  WHERE art.deleted IS FALSE
        AND a.DTYPE = 'RegionAnswer'
        AND art.locatable_key NOT LIKE 'states.%';

CALL TRACK_QUERY('1.91', 'start insert into locatable_search_request with locatable_id', @start);

SET @start := NOW();
INSERT INTO locatable_search_request_restriction (locatable_search_request_id, locatable_id)
  SELECT
    lsr.id AS locatable_search_request_id,
    art.locatable_id
  FROM answer_region_temp AS art
    JOIN locatable_search_request AS lsr
      ON art.legacy_id = lsr.legacy_id AND art.locatable_id = lsr.locatable_id
  WHERE art.deleted IS FALSE
  GROUP BY lsr.id, art.locatable_id;

CALL TRACK_QUERY('1.91', 'start insert into locatable_search_request_restriction with locatable_id', @start);

# DROP TABLE IF EXISTS answer_region_temp;
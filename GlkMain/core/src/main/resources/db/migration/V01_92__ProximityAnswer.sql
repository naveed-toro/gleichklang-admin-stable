-- Proximity Answer
DROP TABLE IF EXISTS answer_proximity_temp;

CALL CREATE_INDEX('p_land_1_idx', 'comppartner', 'p_land_1');
CALL CREATE_INDEX('p_land_2_idx', 'comppartner', 'p_land_2');
CALL CREATE_INDEX('p_land_3_idx', 'comppartner', 'p_land_3');

CALL CREATE_INDEX('p_plz_1_idx', 'comppartner', 'p_plz_1');
CALL CREATE_INDEX('p_plz_2_idx', 'comppartner', 'p_plz_2');
CALL CREATE_INDEX('p_plz_3_idx', 'comppartner', 'p_plz_3');

CALL CREATE_INDEX('f_land_1_idx', 'compfreund', 'f_land_1');
CALL CREATE_INDEX('f_land_2_idx', 'compfreund', 'f_land_2');
CALL CREATE_INDEX('f_land_3_idx', 'compfreund', 'f_land_3');

CALL CREATE_INDEX('f_plz_1_idx', 'compfreund', 'f_plz_1');
CALL CREATE_INDEX('f_plz_2_idx', 'compfreund', 'f_plz_2');
CALL CREATE_INDEX('f_plz_3_idx', 'compfreund', 'f_plz_3');

CALL CREATE_INDEX('locatable_zip_idx', 'locatable', 'zip');

CREATE TABLE answer_proximity_temp (
  n           VARCHAR(3)  NOT NULL DEFAULT '',
  legacy_id   VARCHAR(75) NOT NULL DEFAULT '',
  change_date DATETIME,
  create_date DATETIME,
  zip         VARCHAR(255),
  country     VARCHAR(255),
  distance    MEDIUMINT(9),
  user_id     BIGINT(20)  NOT NULL DEFAULT '0',
  zip_id      BIGINT(20)  NOT NULL DEFAULT '0',
  question_id BIGINT(20)  NOT NULL DEFAULT '0'
);

CALL CREATE_INDEX('answer_proximity_temp_legacy_id', 'answer_proximity_temp', 'legacy_id');
CALL CREATE_INDEX('answer_proximity_temp_zip', 'answer_proximity_temp', 'zip');
CALL CREATE_INDEX('answer_proximity_temp_country', 'answer_proximity_temp', 'country');
CALL CREATE_INDEX('answer_proximity_temp_user_id', 'answer_proximity_temp', 'user_id');
CALL CREATE_INDEX('answer_proximity_temp_zip_id', 'answer_proximity_temp', 'zip_id');
CALL CREATE_INDEX('answer_proximity_temp_question_id', 'answer_proximity_temp', 'question_id');

SET @start := NOW();

INSERT INTO answer_proximity_temp (n, legacy_id, change_date, create_date, zip, country, distance, user_id, zip_id, question_id)
  SELECT
    'p_1'         AS n,
    cp.no         AS legacy_id,
    cp.changedate AS change_date,
    cp.createdate AS create_date,
    cp.p_plz_1    AS zip,
    cp.p_land_1   AS country,
    cp.p_radius_1 AS distance,
    u.id          AS user_id,
    l1.id         AS zip_id,
    q.id          AS question_id
  FROM comppartner AS cp
    JOIN user_ AS u
      ON cp.owner = u.legacy_id
    JOIN locatable AS l1
      ON l1.zip = cp.p_plz_1
    JOIN locatable AS l2
      ON l1.parent_id = l2.id AND l2.legacy_id = cp.p_land_1
    JOIN question AS q
  WHERE q.i18n_key = 'partner.region_question' AND cp.p_radius_1 IS NOT NULL;

CALL TRACK_QUERY('1.92', 'start insert p_1 partner.region_question', @start);

SET @start := NOW();

INSERT INTO answer_proximity_temp (n, legacy_id, change_date, create_date, zip, country, distance, user_id, zip_id, question_id)
  SELECT
    'p_2'         AS n,
    cp.no         AS legacy_id,
    cp.changedate AS change_date,
    cp.createdate AS create_date,
    cp.p_plz_2    AS zip,
    cp.p_land_2   AS country,
    cp.p_radius_2 AS distance,
    u.id          AS user_id,
    l1.id         AS zip_id,
    q.id          AS question_id
  FROM comppartner AS cp
    JOIN user_ AS u
      ON cp.owner = u.legacy_id
    JOIN locatable AS l1
      ON l1.zip = cp.p_plz_2
    JOIN locatable AS l2
      ON l1.parent_id = l2.id AND l2.legacy_id = cp.p_land_2
    JOIN question AS q
  WHERE q.i18n_key = 'partner.region_question' AND cp.p_radius_2 IS NOT NULL;

CALL TRACK_QUERY('1.92', 'start insert p_2 partner.region_question', @start);

SET @start := NOW();

INSERT INTO answer_proximity_temp (n, legacy_id, change_date, create_date, zip, country, distance, user_id, zip_id, question_id)
  SELECT
    'p_3'         AS n,
    cp.no         AS legacy_id,
    cp.changedate AS change_date,
    cp.createdate AS create_date,
    cp.p_plz_3    AS zip,
    cp.p_land_3   AS country,
    cp.p_radius_3 AS distance,
    u.id          AS user_id,
    l1.id         AS zip_id,
    q.id          AS question_id
  FROM comppartner AS cp
    JOIN user_ AS u
      ON cp.owner = u.legacy_id
    JOIN locatable AS l1
      ON l1.zip = cp.p_plz_3
    JOIN locatable AS l2
      ON l1.parent_id = l2.id AND l2.legacy_id = cp.p_land_3
    JOIN question AS q
  WHERE q.i18n_key = 'partner.region_question' AND cp.p_radius_3 IS NOT NULL;

CALL TRACK_QUERY('1.92', 'start insert p_3 partner.region_question', @start);

SET @start := NOW();

INSERT INTO answer_proximity_temp (n, legacy_id, change_date, create_date, zip, country, distance, user_id, zip_id, question_id)
  SELECT
    'f_1'         AS n,
    cf.no         AS legacy_id,
    cf.changedate AS change_date,
    cf.createdate AS create_date,
    cf.f_plz_1    AS zip,
    cf.f_land_1   AS country,
    cf.f_radius_1 AS distance,
    u.id          AS user_id,
    l1.id         AS zip_id,
    q.id          AS question_id
  FROM compfreund AS cf
    JOIN user_ AS u
      ON cf.owner = u.legacy_id
    JOIN locatable AS l1
      ON l1.zip = cf.f_plz_1
    JOIN locatable AS l2
      ON l1.parent_id = l2.id AND l2.legacy_id = cf.f_land_1
    JOIN question AS q
  WHERE q.i18n_key = 'friend.region_question' AND cf.f_radius_1 IS NOT NULL;

CALL TRACK_QUERY('1.92', 'start insert f_1 friend.region_question', @start);

SET @start := NOW();

INSERT INTO answer_proximity_temp (n, legacy_id, change_date, create_date, zip, country, distance, user_id, zip_id, question_id)
  SELECT
    'f_2'         AS n,
    cf.no         AS legacy_id,
    cf.changedate AS change_date,
    cf.createdate AS create_date,
    cf.f_plz_2    AS zip,
    cf.f_land_2   AS country,
    cf.f_radius_2 AS distance,
    u.id          AS user_id,
    l1.id         AS zip_id,
    q.id          AS question_id
  FROM compfreund AS cf
    JOIN user_ AS u
      ON cf.owner = u.legacy_id
    JOIN locatable AS l1
      ON l1.zip = cf.f_plz_2
    JOIN locatable AS l2
      ON l1.parent_id = l2.id AND l2.legacy_id = cf.f_land_2
    JOIN question AS q
  WHERE q.i18n_key = 'friend.region_question' AND cf.f_radius_2 IS NOT NULL;

CALL TRACK_QUERY('1.92', 'start insert f_2 friend.region_question', @start);

SET @start := NOW();

INSERT INTO answer_proximity_temp (n, legacy_id, change_date, create_date, zip, country, distance, user_id, zip_id, question_id)
  SELECT
    'f_3'         AS n,
    cf.no         AS legacy_id,
    cf.changedate AS change_date,
    cf.createdate AS create_date,
    cf.f_plz_3    AS zip,
    cf.f_land_3   AS country,
    cf.f_radius_3 AS distance,
    u.id          AS user_id,
    l1.id         AS zip_id,
    q.id          AS question_id
  FROM compfreund AS cf
    JOIN user_ AS u
      ON cf.owner = u.legacy_id
    JOIN locatable AS l1
      ON l1.zip = cf.f_plz_3
    JOIN locatable AS l2
      ON l1.parent_id = l2.id AND l2.legacy_id = cf.f_land_3
    JOIN question AS q
  WHERE q.i18n_key = 'friend.region_question' AND cf.f_radius_3 IS NOT NULL;

CALL TRACK_QUERY('1.92', 'start insert f_3 friend.region_question', @start);

SET @start := NOW();

INSERT INTO answer (DTYPE, legacy_id, change_date, create_date, question_id, user_id)
  SELECT
    'RegionAnswer',
    legacy_id,
    change_date,
    create_date,
    question_id,
    user_id
  FROM answer_proximity_temp
  WHERE NOT EXISTS(SELECT legacy_id
                   FROM answer);

CALL TRACK_QUERY('1.92', 'start insert into answer', @start);

DELETE FROM locatable_search_request
WHERE DTYPE = 'ProximityLocatableSearchRequest';

SET @start := NOW();

INSERT INTO locatable_search_request (change_date, create_date, legacy_id, answer_id, distance, center_zip_id, DTYPE)
  SELECT
    apt.change_date,
    apt.create_date,
    apt.legacy_id,
    a.id                              AS answer_id,
    apt.distance,
    apt.zip_id,
    'ProximityLocatableSearchRequest' AS DTYPE
  FROM answer_proximity_temp AS apt
    JOIN answer AS a
      ON apt.legacy_id = a.legacy_id
  WHERE a.DTYPE = 'RegionAnswer';

CALL TRACK_QUERY('1.92', 'start insert into answer', @start);

DROP TABLE IF EXISTS answer_proximity_temp;
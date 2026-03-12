# search_category is not necessary any more because we map sex via questionnaires
DROP TABLE IF EXISTS search_category;

# Reverting for Idempotence
DELETE ca FROM choice_answer AS ca
WHERE ca.answer_id IN (
  SELECT id
  FROM answer
  WHERE legacy_id = 'geschlecht');

DELETE FROM answer
WHERE legacy_id = 'geschlecht';

# Sex answers migrations

INSERT INTO answer (DTYPE, legacy_id, change_date, create_date, question_id, user_id)
  SELECT
    'ChoiceAnswer',
    'sex',
    NOW(),
    NOW(),
    (
      SELECT id
      FROM question
      WHERE i18n_key = 'stamm.sex'),
    u.id
  FROM user_ u
  WHERE u.sex IS NOT NULL;

INSERT INTO choice_answer (answer_id, choice_id)
  SELECT
    a.id,
    c.id
  FROM answer AS a
    JOIN user_ AS u
      ON u.id = a.user_id AND a.legacy_id = 'sex'
    JOIN choice AS c
      ON u.sex = c.legacy_id AND c.i18n_key LIKE 'sex_%';

# Search Sex answers migration
DELETE ca FROM choice_answer AS ca
  JOIN answer AS a
    ON a.id = ca.answer_id
WHERE a.legacy_id = 'sex_search';
DELETE a FROM answer AS a
WHERE a.legacy_id = 'sex_search';

SELECT *
FROM choice
WHERE i18n_key LIKE 'search_sex%';

DROP TABLE IF EXISTS intersex_user;
CREATE TABLE intersex_user AS
  SELECT a.user_id FROM choice_group AS cg
    JOIN choice AS c ON c.choice_group_id = cg.id JOIN question AS q ON q.choice_group_id = cg.id
    JOIN choice_answer ca ON c.id = ca.choice_id
    JOIN answer a ON ca.answer_id = a.id
  WHERE q.i18n_key = 'partner.p_intersex' AND c.sort_order > 2;


DROP TABLE IF EXISTS sex_search;
CREATE TABLE sex_search
(
  `user_id`                 BIGINT(20),
  `suche`                   VARCHAR(255),
  `i18n_key`                VARCHAR(255),
  `choice_id`               BIGINT(20),
  `question_id`             BIGINT(20),
  `recommendation_category` VARCHAR(255)
);

# partner m
INSERT INTO sex_search (user_id, suche, i18n_key, choice_id, question_id, recommendation_category)
  SELECT
    u.id          AS user_id,
    cu.suche,
    'search_sex_m',
    (SELECT id FROM choice WHERE i18n_key = 'search_sex_m') AS choice_id,
    q.id AS question_id,
    'PARNTERSHIP'
      AS recommendation_category
  FROM compuser AS cu
    JOIN user_ u
      ON u.legacy_id = cu.no
    JOIN question q ON q.i18n_key IN ('partner.sex')
  WHERE cu.suche REGEXP 'pm' AND cu.suche NOT REGEXP 'pw|pp' AND u.id NOT IN (SELECT user_id FROM intersex_user);

# freund m
INSERT INTO sex_search (user_id, suche, i18n_key, choice_id, question_id, recommendation_category)
  SELECT
    u.id          AS user_id,
    cu.suche,
    'search_sex_m',
    (SELECT id FROM choice WHERE i18n_key = 'search_sex_m') AS choice_id,
    q.id AS question_id,
    'FRIENDSHIP'
      AS recommendation_category
  FROM compuser AS cu
    JOIN user_ u
      ON u.legacy_id = cu.no
    JOIN question q ON q.i18n_key IN ('freund.sex')
  WHERE cu.suche REGEXP 'fm' AND cu.suche NOT REGEXP 'fw';

# partner w
INSERT INTO sex_search (user_id, suche, i18n_key, choice_id, question_id, recommendation_category)
  SELECT
    u.id          AS user_id,
    cu.suche,
    'search_sex_w',
    (SELECT id FROM choice WHERE i18n_key = 'search_sex_w') AS choice_id,
    q.id AS question_id,
    'PARNTERSHIP'
      AS recommendation_category
  FROM compuser AS cu
    JOIN user_ u
      ON u.legacy_id = cu.no
    JOIN question q ON q.i18n_key IN ('partner.sex')
  WHERE cu.suche REGEXP 'pw' AND cu.suche NOT REGEXP 'pm|pp' AND u.id NOT IN (SELECT user_id FROM intersex_user);

# freund w
INSERT INTO sex_search (user_id, suche, i18n_key, choice_id, question_id, recommendation_category)
  SELECT
    u.id          AS user_id,
    cu.suche,
    'search_sex_w',
    (SELECT id FROM choice WHERE i18n_key = 'search_sex_w') AS choice_id,
    q.id AS question_id,
    'FRIENDSHIP'
      AS recommendation_category
  FROM compuser AS cu
    JOIN user_ u
      ON u.legacy_id = cu.no
    JOIN question q ON q.i18n_key IN ('freund.sex')
  WHERE cu.suche REGEXP 'fw' AND cu.suche NOT REGEXP 'fm';

# partner mw
INSERT INTO sex_search (user_id, suche, i18n_key, choice_id, question_id, recommendation_category)
  SELECT
    u.id          AS user_id,
    cu.suche,
    'search_sex_mw',
    (SELECT id FROM choice WHERE i18n_key = 'search_sex_mw') AS choice_id,
    q.id AS question_id,
    'PARNTERSHIP'
      AS recommendation_category
  FROM compuser AS cu
    JOIN user_ u
      ON u.legacy_id = cu.no
    JOIN question q ON q.i18n_key IN ('partner.sex')
  WHERE cu.suche REGEXP 'pm' AND cu.suche REGEXP 'pw' AND cu.suche NOT REGEXP 'pp' AND u.id NOT IN (SELECT user_id FROM intersex_user);

# freund mw
INSERT INTO sex_search (user_id, suche, i18n_key, choice_id, question_id, recommendation_category)
  SELECT
    u.id          AS user_id,
    cu.suche,
    'search_sex_mw',
    (SELECT id FROM choice WHERE i18n_key = 'search_sex_mw') AS choice_id,
    q.id AS question_id,
    'FRIENDSHIP'
      AS recommendation_category
  FROM compuser AS cu
    JOIN user_ u
      ON u.legacy_id = cu.no
    JOIN question q ON q.i18n_key IN ('freund.sex')
  WHERE cu.suche REGEXP 'fm' AND cu.suche REGEXP 'fw';

# partner p
INSERT INTO sex_search (user_id, suche, i18n_key, choice_id, question_id, recommendation_category)
  SELECT
    u.id          AS user_id,
    cu.suche,
    'search_sex_p',
    (SELECT id FROM choice WHERE i18n_key = 'search_sex_p') AS choice_id,
    q.id AS question_id,
    'PARTNERSHIP'
      AS recommendation_category
  FROM compuser AS cu
    JOIN user_ u
      ON u.legacy_id = cu.no
    JOIN question q ON q.i18n_key IN ('partner.sex')
  WHERE cu.suche REGEXP 'pp';

# partner mi
INSERT INTO sex_search (user_id, suche, i18n_key, choice_id, question_id, recommendation_category)
  SELECT
    u.id          AS user_id,
    cu.suche,
    'search_sex_mi',
    (SELECT id FROM choice WHERE i18n_key = 'search_sex_mi') AS choice_id,
    q.id AS question_id,
    'PARNTERSHIP'
      AS recommendation_category
  FROM compuser AS cu
    JOIN user_ u
      ON u.legacy_id = cu.no
    JOIN question q ON q.i18n_key IN ('partner.sex')
  WHERE cu.suche REGEXP 'pm' AND cu.suche NOT REGEXP 'pw|pp' AND u.id IN (SELECT user_id FROM intersex_user);

# partner wi
INSERT INTO sex_search (user_id, suche, i18n_key, choice_id, question_id, recommendation_category)
  SELECT
    u.id          AS user_id,
    cu.suche,
    'search_sex_wi',
    (SELECT id FROM choice WHERE i18n_key = 'search_sex_wi') AS choice_id,
    q.id AS question_id,
    'PARNTERSHIP'
      AS recommendation_category
  FROM compuser AS cu
    JOIN user_ u
      ON u.legacy_id = cu.no
    JOIN question q ON q.i18n_key IN ('partner.sex')
  WHERE cu.suche REGEXP 'pw' AND cu.suche NOT REGEXP 'pm|pp' AND u.id IN (SELECT user_id FROM intersex_user);

# partner mwi
INSERT INTO sex_search (user_id, suche, i18n_key, choice_id, question_id, recommendation_category)
  SELECT
    u.id          AS user_id,
    cu.suche,
    'search_sex_mwi',
    (SELECT id FROM choice WHERE i18n_key = 'search_sex_mwi') AS choice_id,
    q.id AS question_id,
    'PARNTERSHIP'
      AS recommendation_category
  FROM compuser AS cu
    JOIN user_ u
      ON u.legacy_id = cu.no
    JOIN question q ON q.i18n_key IN ('partner.sex')
  WHERE cu.suche REGEXP 'pm' AND cu.suche REGEXP 'pw' AND cu.suche NOT REGEXP 'pp' AND u.id IN (SELECT user_id FROM intersex_user);


INSERT INTO answer (DTYPE, legacy_id, change_date, create_date, question_id, user_id)
  SELECT
    'ChoiceAnswer',
    'sex_search',
    NOW(),
    NOW(),
    ss.question_id,
    ss.user_id
  FROM sex_search AS ss;

INSERT INTO choice_answer (answer_id, choice_id)
  SELECT
    a.id,
    ss.choice_id
  FROM answer AS a
    JOIN sex_search AS ss
      ON a.user_id = ss.user_id AND a.question_id = ss.question_id;

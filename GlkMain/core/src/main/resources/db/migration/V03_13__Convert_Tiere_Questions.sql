# Invariants
SET @questionnaire_name = _utf8'person' COLLATE utf8_unicode_ci;
SET @question_name = _utf8'person.tiere' COLLATE utf8_unicode_ci;
SET @question_group_name = @question_name COLLATE utf8_unicode_ci;
SET @legacy_name = CONCAT(@question_name, _utf8'_new') COLLATE utf8_unicode_ci;

SELECT id
INTO @bool_group_id
FROM choice_group
WHERE name = 'Ja/Nein';

SELECT id
INTO @questionnaire_id
FROM questionnaire
WHERE i18n_key = @questionnaire_name;

SELECT id
INTO @question_id
FROM question
WHERE i18n_key = @question_name;

SELECT IF(MAX(sort_order) IS NULL, 0, MAX(sort_order) + 1)
INTO @question_group_sort
FROM question_group
WHERE questionnaire_id = @questionnaire_id;

# Idempotence
DELETE FROM i18n
WHERE legacy_id = @legacy_name;
DELETE ca FROM choice_answer ca
  JOIN answer AS a ON ca.answer_id = a.id
WHERE a.legacy_id LIKE CONCAT(@legacy_name, '%');
DELETE FROM answer
WHERE legacy_id LIKE CONCAT(@legacy_name, '%');
DELETE FROM question
WHERE legacy_id LIKE CONCAT(@legacy_name, '%');
DELETE FROM question_group
WHERE legacy_id LIKE CONCAT(@legacy_name, '%');

# create question_group
INSERT INTO question_group (legacy_id, i18n_key, questionnaire_id, deleted, sort_order)
VALUES (@legacy_name, @question_group_name, @questionnaire_id, FALSE,
        @question_group_sort);

SELECT id
INTO @question_group_id
FROM question_group
WHERE i18n_key = @question_name;

# create questions
INSERT INTO question (DTYPE, legacy_id, label, required, selection_type, i18n_key, choice_group_id, question_group_id, deleted, sort_order, representation_type, adjustable_relationship_visibility, only_admin_visible)
  SELECT
    'ChoiceQuestion',
    CONCAT(@legacy_name, id),
    CONCAT_WS('_', @question_name, choice),
    TRUE,
    'SINGLE',
    CONCAT_WS('_', @question_name, choice),
    @bool_group_id,
    @question_group_id,
    FALSE,
    sort_order,
    'DEFAULT',
    FALSE,
    FALSE
  FROM choice
  WHERE question_id = @question_id;

# migrate answers
DROP TABLE IF EXISTS tmp_tier_questions;
CREATE TEMPORARY TABLE tmp_tier_questions AS
  SELECT
    id,
    legacy_id,
    i18n_key,
    CAST(REPLACE(legacy_id, @legacy_name, '') AS UNSIGNED) AS choice_id
  FROM question AS q
  WHERE q.legacy_id LIKE CONCAT(@legacy_name, '%');

INSERT INTO answer (DTYPE, legacy_id, question_id, user_id)
  SELECT
    t1.DTYPE,
    t1.legacy_id,
    t1.question_id,
    t2.user_id
  FROM
    (SELECT
       'ChoiceAnswer' AS DTYPE,
       q.legacy_id,
       q.id           AS question_id
     FROM tmp_tier_questions AS q
    ) AS t1
    CROSS JOIN
    (SELECT a.user_id
     FROM answer AS a
     WHERE a.question_id = @question_id
    ) AS t2;

# migrate choice answers

SELECT id
INTO @choice_yes
FROM choice
WHERE i18n_key = 'yes_opt';
SELECT id
INTO @choice_no
FROM choice
WHERE i18n_key = 'no_opt';

DROP TABLE IF EXISTS tmp_tier_answer;
CREATE TEMPORARY TABLE tmp_tier_answer AS
  SELECT
    a.id            AS answer_id,
    a.user_id       AS user_id,
    c.id            AS choice_id,
    c.i18n_key      AS choice_i18n,
    q_new.i18n_key  AS question_new_i18n,
    q_new.id        AS question_new_id,
    q_new.legacy_id AS question_new_legacy
  FROM answer AS a
    JOIN choice_answer AS ca ON ca.answer_id = a.id
    JOIN choice AS c ON ca.choice_id = c.id
    JOIN tmp_tier_questions AS q_new
      ON CONCAT(@legacy_name, c.id) = q_new.legacy_id
  WHERE a.question_id = @question_id;


CALL CREATE_INDEX('tmp_tier_answer_user_idx', 'tmp_tier_answer', 'user_id');
CALL CREATE_INDEX('tmp_tier_answer_question_new_legacy_idx', 'tmp_tier_answer',
                  'question_new_legacy');

# set yes option for presented animals
INSERT INTO choice_answer
  SELECT
    a_new.id,
    @choice_yes
  FROM tmp_tier_answer AS ta
    JOIN answer AS a_new ON
                           ta.user_id = a_new.user_id AND
                           ta.question_new_id = a_new.question_id;
# set no option for not presented animal
INSERT INTO choice_answer
  SELECT
    a.id,
    @choice_no
  FROM answer AS a LEFT JOIN choice_answer AS ca ON a.id = ca.answer_id
  WHERE a.legacy_id LIKE CONCAT(@legacy_name, '%') AND ca.choice_id IS NULL;

UPDATE question
SET deleted = TRUE
WHERE i18n_key = @question_name;

INSERT INTO i18n (legacy_id, language, i18n_key, i18n_value, base_name)
VALUES
  (@legacy_name, 'DE', @question_group_name, 'Haustiere',
   'QUESTION_GROUP_NAME'),
  (@legacy_name, 'DE', 'person.tiere_hu', 'Ich besitze einen Hund',
   'QUESTION_NAME'),
  (@legacy_name, 'DE', 'person.tiere_ka', 'Ich besitze eine Katze',
   'QUESTION_NAME'),
  (@legacy_name, 'DE', 'person.tiere_na', 'Ich besitze ein Klein-/Nagetier',
   'QUESTION_NAME'),
  (@legacy_name, 'DE', 'person.tiere_pf', 'Ich besitze ein Pferd',
   'QUESTION_NAME'),
  (@legacy_name, 'DE', 'person.tiere_vo', 'Ich besitze einen Vogel',
   'QUESTION_NAME'),
  (@legacy_name, 'DE', 'person.tiere_re', 'Ich besitze ein Reptil',
   'QUESTION_NAME'),
  (@legacy_name, 'DE', 'person.tiere_fi', 'Ich besitze einen Fisch',
   'QUESTION_NAME'),
  (@legacy_name, 'DE', 'person.tiere_in', 'Ich besitze Insekten/Spinne',
   'QUESTION_NAME');
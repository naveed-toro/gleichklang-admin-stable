DELETE FROM question
WHERE
  i18n_key IN ('sex', 'partner.sex', 'freund.sex');

DELETE FROM choice_group WHERE legacy_id IN ('sex', 'Gesuchtes Geschlecht');

DELETE FROM question_group
WHERE
  legacy_id IN ('stammdaten', 'partner.stammdaten', 'freund.stammdaten');

DELETE FROM questionnaire
WHERE legacy_id IN ('stammdaten');

DELETE FROM i18n WHERE legacy_id like '%sex%' OR i18n.legacy_id  like '%stammdaten%';

SELECT MAX(sort_order)
INTO @max_sort
FROM questionnaire;

SELECT @max_sort;

# insert missing questionnaires
CALL DROP_INDEX('questionnaire_sort_order', 'questionnaire');

UPDATE questionnaire
SET sort_order = sort_order + 1;

CALL CREATE_INDEX('questionnaire_sort_order', 'questionnaire', 'sort_order');

INSERT INTO questionnaire (legacy_id, create_date, i18n_key, recommendation_category, deleted, sort_order)
VALUES ('stammdaten', NOW(), 'stammdaten', NULL, FALSE, 1);

# insert missing question groups
DROP INDEX question_group_sort_order_questionnaire ON question_group;

UPDATE question_group
SET sort_order = sort_order + 1
WHERE questionnaire_id IN (
  SELECT id
  FROM questionnaire
  WHERE legacy_id IN
        ('partner', 'freund'));

INSERT INTO question_group (legacy_id, create_date, i18n_key, questionnaire_id, deleted, sort_order)
VALUES
  ('stammdaten', NOW(), 'stammdaten', (
    SELECT id
    FROM questionnaire
    WHERE
      legacy_id = 'stammdaten'), FALSE, 1),
  ('freund.stammdaten', NOW(), 'freund.stammdaten', (
    SELECT id
    FROM questionnaire
    WHERE legacy_id = 'freund'), FALSE,
   1),
  ('partner.stammdaten', NOW(), 'partner.stammdaten', (
    SELECT id
    FROM questionnaire
    WHERE legacy_id = 'partner'),
   FALSE,
   1);

CALL CREATE_UNIQUE_INDEX('question_group_sort_order_questionnaire', 'question_group', 'sort_order
                         , questionnaire_id');

INSERT INTO choice_group (legacy_id, change_date, create_date, name, deleted)
VALUES
  ('sex', NOW(), NOW(), 'Eigenes Geschlecht', FALSE),
  ('Gesuchtes Geschlecht', NOW(), NOW(), 'Gesuchtes Geschlecht', FALSE);

# insert missing questions

INSERT INTO question (DTYPE, legacy_id, change_date, required, selection_type, max_length, number_of_lines, max_value, min_value, i18n_key, choice_group_id, question_group_id, deleted, sort_order, visible, representation_type)
VALUES
  ('ChoiceQuestion', 'sex', NOW(), TRUE, 'SINGLE', NULL, NULL, NULL, NULL, 'stamm.sex', (
    SELECT id
    FROM
      choice_group
    WHERE name = 'Eigenes Geschlecht'),
                     (
                       SELECT id
                       FROM question_group
                       WHERE
                         i18n_key = 'stammdaten'), FALSE, 1, TRUE, 'DEFAULT'),
  ('ChoiceQuestion', 'partner.sex', NOW(), TRUE, 'MULTIPLE', NULL, NULL, NULL, NULL, 'partner.sex',
                     (SELECT id FROM choice_group WHERE name = 'Gesuchtes Geschlecht'), (
     SELECT id
     FROM
       question_group
     WHERE i18n_key = 'partner.stammdaten'), FALSE, 1, TRUE, 'DEFAULT'),
  ('ChoiceQuestion', 'freund.sex', NOW(), TRUE, 'MULTIPLE', NULL, NULL, NULL, NULL, 'freund.sex',
                     (SELECT id FROM choice_group WHERE name = 'Gesuchtes Geschlecht'), (
     SELECT id
     FROM
       question_group
     WHERE i18n_key = 'freund.stammdaten'), FALSE, 1, TRUE, 'DEFAULT');

# sex 
INSERT INTO choice (legacy_id, change_date, create_date, sort_order, i18n_key, choice_group_id)
VALUES
  ('MALE', NOW(), NOW(), 1, 'sex_m', (
    SELECT id
    FROM choice_group
    WHERE name = 'Eigenes Geschlecht')),
  ('FEMALE', NOW(), NOW(), 2, 'sex_w', (
    SELECT id
    FROM choice_group
    WHERE name = 'Eigenes Geschlecht')),
  ('INTERSEXUAL', NOW(), NOW(), 3, 'sex_i', (
    SELECT id
    FROM choice_group
    WHERE name = 'Eigenes Geschlecht')),
  ('INTERSEXUAL_MALE', NOW(), NOW(), 4, 'sex_mi', (
    SELECT id
    FROM choice_group
    WHERE name =
          'Eigenes Geschlecht')),
  ('INTERSEXUAL_FEMALE', NOW(), NOW(), 5, 'sex_wi', (
    SELECT id
    FROM choice_group
    WHERE name =
          'Eigenes Geschlecht'));

# Gesuchtes Geschlecht
INSERT INTO choice (legacy_id, change_date, create_date, sort_order, i18n_key, choice_group_id)
VALUES
  ('m', NOW(), NOW(), 1, 'search_sex_m', (
    SELECT id
    FROM choice_group
    WHERE name = 'Gesuchtes Geschlecht')),
  ('w', NOW(), NOW(), 2, 'search_sex_w', (
    SELECT id
    FROM choice_group
    WHERE name = 'Gesuchtes Geschlecht')),
  ('i', NOW(), NOW(), 3, 'search_sex_i',
   (
    SELECT id
    FROM choice_group
    WHERE name = 'Gesuchtes Geschlecht')),
  ('mw', NOW(), NOW(), 4, 'search_sex_mw',
   (
    SELECT id
    FROM choice_group
    WHERE name = 'Gesuchtes Geschlecht')),

  ('mi', NOW(), NOW(), 5, 'search_sex_mi',
   (
    SELECT id
    FROM choice_group
    WHERE name = 'Gesuchtes Geschlecht')),

  ('wi', NOW(), NOW(), 6, 'search_sex_wi',
   (
    SELECT id
    FROM choice_group
    WHERE name = 'Gesuchtes Geschlecht')),

  ('mwi', NOW(), NOW(), 7, 'search_sex_mwi',
   (
    SELECT id
    FROM choice_group
    WHERE name = 'Gesuchtes Geschlecht')),

  ('p', NOW(), NOW(), 8, 'search_sex_p',
   (
    SELECT id
    FROM choice_group
    WHERE name = 'Gesuchtes Geschlecht'));

UPDATE question AS q
SET q.choice_group_id = (
  SELECT id
  FROM choice_group AS cg
  WHERE cg.name = 'Gesuchtes Geschlecht'),
  q.selection_type    = 'SINGLE'
WHERE q.i18n_key IN ('partner.sex', 'freund.sex');

INSERT INTO i18n (legacy_id, change_date, create_date, language, i18n_key, i18n_value, base_name)
VALUES
  ('sex_m', NOW(), NOW(), 'DE', 'sex_m', 'männlich', 'CHOICE_VALUE'),
  ('sex_w', NOW(), NOW(), 'DE', 'sex_w', 'weiblich', 'CHOICE_VALUE'),
  ('sex_mi', NOW(), NOW(), 'DE', 'sex_mi', 'männlich intersexuell', 'CHOICE_VALUE'),
  ('sex_wi', NOW(), NOW(), 'DE', 'sex_wi', 'weiblich intersexuell', 'CHOICE_VALUE'),
  ('sex_i', NOW(), NOW(), 'DE', 'sex_i', 'intersexuell', 'CHOICE_VALUE'),

  ('m', NOW(), NOW(), 'DE', 'search_sex_m', 'Suche nur Mann', 'CHOICE_VALUE'),
  ('w', NOW(), NOW(), 'DE', 'search_sex_w', 'Suche nur Frau', 'CHOICE_VALUE'),
  ('i', NOW(), NOW(), 'DE', 'search_sex_i', 'Suche nur intersexuell', 'CHOICE_VALUE'),

  ('mw', NOW(), NOW(), 'DE', 'search_sex_mw', 'Suche nur Mann oder Frau', 'CHOICE_VALUE'),
  ('mi', NOW(), NOW(), 'DE', 'search_sex_mi', 'Suche Mann oder intersexuell', 'CHOICE_VALUE'),
  ('wi', NOW(), NOW(), 'DE', 'search_sex_wi', 'Suche Frau oder intersexuell', 'CHOICE_VALUE'),
  ('mwi', NOW(), NOW(), 'DE', 'search_sex_mwi', 'Suche Mann oder Frau oder intersexuell', 'CHOICE_VALUE'),
  ('p', NOW(), NOW(), 'DE', 'search_sex_p', 'Suche pansexuell', 'CHOICE_VALUE'),

  ('stamm.sex', NOW(), NOW(), 'DE', 'stamm.sex', 'Geschlecht', 'QUESTION_NAME'),
  ('stammdaten', NOW(), NOW(), 'DE', 'stammdaten', 'Persönliche Daten', 'QUESTION_GROUP_NAME'),
  ('stammdaten', NOW(), NOW(), 'DE', 'stammdaten', 'Ihre allgemeine Information', 'QUESTIONNAIRE_NAME'),
  ('stammdaten', NOW(), NOW(), 'DE', 'stammdaten', 'Bitte tragen Sie Ihre allgemeine Information ein',
   'QUESTIONNAIRE_DESCRIPTION'),
  ('partner.sex', NOW(), NOW(), 'DE', 'partner.sex', 'Gesuchtes Geschlecht', 'QUESTION_NAME'),
  ('freund.sex', NOW(), NOW(), 'DE', 'freund.sex', 'Gesuchtes Geschlecht', 'QUESTION_NAME'),
  ('partner.stammdaten', NOW(), NOW(), 'DE', 'partner.stammdaten', 'Allgemeine Suchangaben', 'QUESTION_GROUP_NAME'),
  ('freund.stammdaten', NOW(), NOW(), 'DE', 'freund.stammdaten', 'Allgemeine Suchangaben', 'QUESTION_GROUP_NAME');

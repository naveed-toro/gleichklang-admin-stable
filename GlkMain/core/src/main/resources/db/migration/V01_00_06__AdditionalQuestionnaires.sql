UPDATE i18n
SET base_name = 'CHOICE_VALUE'
WHERE i18n_key LIKE 'quellen%';

SET @max_sort_order = (SELECT MAX(sort_order)
                       FROM questionnaire);

SET @next_sort_order = CASE WHEN @max_sort_order IS NOT NULL THEN @max_sort_order ELSE 0 END;
SELECT @next_sort_order;

INSERT INTO questionnaire (legacy_id, create_date, i18n_key, recommendation_category, deleted,
                           visible, sort_order)
VALUES
  (
    'partner.region_question',
    NOW(),
    'partner.region_question',
    'PARTNERSHIP',
    FALSE,
    TRUE,
    @next_sort_order := @next_sort_order + 1),
  (
    'friend.region_question',
    NOW(),
    'friend.region_question',
    'FRIENDSHIP',
    FALSE,
    TRUE,
    @next_sort_order := @next_sort_order + 1),
  ('registration', NOW(), 'registration', NULL, FALSE, FALSE, @next_sort_order := @next_sort_order + 1);


INSERT INTO question_group (legacy_id, create_date, i18n_key, questionnaire_id, deleted, sort_order)
VALUES ('registration', NOW(), 'source_group', (
  SELECT id
  FROM questionnaire
  WHERE i18n_key = 'registration'), FALSE, 1),
  ('friend.region_question', NOW(), 'friend.region_question', (
    SELECT id
    FROM questionnaire
    WHERE
      legacy_id = 'friend.region_question'), FALSE, 1),
  ('partner.region_question', NOW(), 'partner.region_question', (
    SELECT id
    FROM questionnaire
    WHERE
      legacy_id = 'partner.region_question'), FALSE, 1);

INSERT INTO choice_group (legacy_id, change_date, create_date, name, deleted)
VALUES ('registration', NOW(), NOW(), 'sources', FALSE);


INSERT INTO question (DTYPE, legacy_id, change_date, create_date, required, selection_type, i18n_key, choice_group_id, question_group_id, deleted, sort_order, visible, representation_type)
VALUES ('ChoiceQuestion', 'registration', NOW(), NOW(), FALSE, 'MULTIPLE', 'source',
                          (
                            SELECT id
                            FROM choice_group
                            WHERE name = 'sources'),
                          (
                            SELECT id
                            FROM question_group
                            WHERE i18n_key = 'source_group'), FALSE, 1, TRUE, 'DEFAULT');

INSERT INTO question (DTYPE, legacy_id, create_date, required, max_length, number_of_lines, i18n_key, question_group_id, deleted, sort_order, visible, representation_type)
VALUES ('TextQuestion', 'registration', NOW(), FALSE, 65535, 3, 'source_text',
                        (
                          SELECT id
                          FROM question_group
                          WHERE i18n_key = 'source_group'), FALSE, 2, TRUE, 'DEFAULT'),
  ('RegionQuestion', 'friend.region_question', NOW(), TRUE, NULL, NULL, 'friend.region_question', (
    SELECT id
    FROM
      question_group
    WHERE i18n_key = 'friend.region_question'), FALSE, 1,
                     TRUE, 'DEFAULT'),
  ('RegionQuestion', 'partner.region_question', NOW(), TRUE, NULL, NULL, 'partner.region_question',
                     (
                       SELECT id
                       FROM
                         question_group
                       WHERE i18n_key = 'partner.region_question'),
                     FALSE,
                     1,
                     TRUE,
   'DEFAULT');

SET @i := 0;

INSERT INTO choice (legacy_id, change_date, create_date, sort_order, i18n_key, choice_group_id)
  SELECT
    t1.legacy_id,
    t1.change_date,
    t1.create_date,
    t2.sort_order,
    t2.i18n_key,
    t1.choice_group_id
  FROM
    (
      SELECT
        'registration'            AS legacy_id,
        NOW()                     AS change_date,
        NOW()                     AS create_date,
        (
          SELECT id
          FROM choice_group
          WHERE name = 'sources') AS choice_group_id) AS t1
    RIGHT JOIN
    (
      SELECT
        i18n_key,
        @i := @i + 1 AS sort_order
      FROM i18n AS i
      WHERE i.i18n_key LIKE 'quellen%' AND i.language = 'DE') AS t2
      ON TRUE;

INSERT INTO question_group (legacy_id, change_date, create_date, i18n_key, questionnaire_id, deleted, sort_order)
VALUES ('registration', NOW(), NOW(), 'press_group', (
  SELECT id
  FROM questionnaire
  WHERE i18n_key = 'registration'), FALSE, 2);

DROP TABLE IF EXISTS source_choices;
CREATE TEMPORARY TABLE source_choices
(choice_key   VARCHAR(255));

INSERT INTO source_choices (choice_key) VALUES
  ('quellen.Allesklar_Suche'),
  ('quellen.Andere_Suchmaschine'),
  ('quellen.AOL_Suche'),
  ('quellen.Aufkleber'),
  ('quellen.Autowerbung'),
  ('quellen.BW_Inet'),
  ('quellen.Dir_Post'),
  ('quellen.FB_Hausbriefkasten'),
  ('quellen.FB_KneipeCafe'),
  ('quellen.FB_sonstwo'),
  ('quellen.FB_Uni'),
  ('quellen.fernseh'),
  ('quellen.FrBk'),
  ('quellen.Google_ADS_RO'),
  ('quellen.Google_Suche'),
  ('quellen.heilkprak'),
  ('quellen.Meinestadt'),
  ('quellen.MSB_Suche'),
  ('quellen.Netscape_Suche'),
  ('quellen.Newsletter'),
  ('quellen.Plakat'),
  ('quellen.Pressebericht'),
  ('quellen.psycho'),
  ('quellen.radio_ber'),
  ('quellen.radio_wer'),
  ('quellen.Sonstiges'),
  ('quellen.T-Online-Suche'),
  ('quellen.tn_geworb'),
  ('quellen.veranst'),
  ('quellen.VideoclipWerbefilm'),
  ('quellen.Yahoo_Suche'),
  ('quellen.Zeitschriftenwerbung');

SET @sort_order := 1;

INSERT INTO choice (legacy_id, create_date, sort_order, i18n_key, choice_group_id, question_id)
  SELECT
    sc.choice_key,
    NOW(),
    @sort_order := @sort_order + 1,
    sc.choice_key,
    (
      SELECT id
      FROM choice_group
      WHERE name = 'sources'),
    (
      SELECT id
      FROM question
      WHERE i18n_key = 'source')
  FROM source_choices AS sc;


INSERT INTO question (DTYPE, legacy_id, change_date, create_date, required, selection_type, i18n_key, choice_group_id, question_group_id, deleted, sort_order, visible, representation_type)
VALUES ('ChoiceQuestion', 'registration', NOW(), NOW(), TRUE, 'SINGLE', 'press',
                          (
                            SELECT id
                            FROM choice_group
                            WHERE name = 'Ja/Nein'),
                          (
                            SELECT id
                            FROM question_group
                            WHERE i18n_key = 'press_group'), FALSE, 1, TRUE, 'DEFAULT');

UPDATE choice
SET i18n_key = LOWER(REPLACE(i18n_key, '.', '_'))
WHERE i18n_key LIKE 'quellen%';

# Affinity choice_group
INSERT INTO choice_group (legacy_id, change_date, create_date, name, deleted)
VALUES ('affinity', NOW(), NOW(), 'affinity_choices', FALSE);

INSERT INTO choice (legacy_id, change_date, create_date, sort_order, i18n_key, choice_group_id)
VALUES
  ('1', NOW(), NOW(), 1, 'affinity_1', (
    SELECT id
    FROM choice_group
    WHERE name = 'affinity_choices')),
  ('2', NOW(), NOW(), 2, 'affinity_2', (
    SELECT id
    FROM choice_group
    WHERE name = 'affinity_choices')),
  ('3', NOW(), NOW(), 3, 'affinity_3', (
    SELECT id
    FROM choice_group
    WHERE name = 'affinity_choices')),
  ('4', NOW(), NOW(), 4, 'affinity_4', (
    SELECT id
    FROM choice_group
    WHERE name =
          'affinity_choices')),
  ('5', NOW(), NOW(), 5, 'affinity_5', (
    SELECT id
    FROM choice_group
    WHERE name =
          'affinity_choices'));

INSERT INTO i18n (legacy_id, change_date, create_date, language, i18n_key, i18n_value, base_name)
VALUES
  ('affinity_1', NOW(), NOW(), 'DE', 'affinity_1', '1', 'CHOICE_VALUE'),
  ('affinity_2', NOW(), NOW(), 'DE', 'affinity_2', '2', 'CHOICE_VALUE'),
  ('affinity_3', NOW(), NOW(), 'DE', 'affinity_3', '3', 'CHOICE_VALUE'),
  ('affinity_4', NOW(), NOW(), 'DE', 'affinity_4', '4', 'CHOICE_VALUE'),
  ('affinity_5', NOW(), NOW(), 'DE', 'affinity_5', '5', 'CHOICE_VALUE');

UPDATE question
SET choice_group_id = (
  SELECT id
  FROM choice_group
  WHERE name = 'affinity_choices'),
  legacy_id         = 'affinity',
  selection_type = 'SINGLE'
WHERE DTYPE = 'NumberQuestion' AND i18n_key NOT IN (
  'partner.p_minalter', 'partner.p_prefalter', 'partner.p_maxalter', 'partner.p_mingroesse', 'partner.p_prefgroesse',
                        'partner.p_maxgroesse', 'freund.f_minalter', 'freund.f_prefalter', 'freund.f_maxalter',
                        'aussehen.groesse', 'aussehen.gewicht', 'person.kinder');
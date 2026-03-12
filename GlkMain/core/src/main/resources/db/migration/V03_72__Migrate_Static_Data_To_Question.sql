CALL ADD_COLUMN('user_settings', 'talk_to_press', 'bit');
CALL ADD_COLUMN('user_settings', 'matchmaking_success', 'bit');
CALL ADD_COLUMN('user_settings', 'satisfaction_other', 'VARCHAR(255)');
CALL ADD_COLUMN('user_settings', 'satisfaction_gleichklang', 'VARCHAR(255)');

INSERT IGNORE INTO choice_group (create_date, i18n_key) VALUES
  (NOW(), 'satisfaction');

SET @satisfaction_id=(SELECT id FROM choice_group WHERE i18n_key = 'satisfaction');
SET @yes_no_id=(SELECT id FROM choice_group WHERE i18n_key = 'Ja/Nein');

INSERT IGNORE INTO choice (create_date, choice_group_id, i18n_key, sort_order) VALUES
  (NOW(), @satisfaction_id, 'SATISFIED', 0),
  (NOW(), @satisfaction_id, 'RATHER_SATISFIED', 1),
  (NOW(), @satisfaction_id, 'RATHER_UNSATISFIED', 2),
  (NOW(), @satisfaction_id, 'UNSATISFIED', 3),
  (NOW(), @satisfaction_id, 'NO_JUDGE', 4);

SET @questionnaire_id=(SELECT id FROM questionnaire WHERE i18n_key = 'registration');
SET @max_sort_order=(SELECT MAX(sort_order) FROM question_group WHERE questionnaire_id = @questionnaire_id);

INSERT IGNORE INTO question_group (create_date, questionnaire_id, i18n_key, sort_order) VALUES
  (NOW(), @questionnaire_id, 'satisfaction', @max_sort_order + 1);

SET @question_group_id=(SELECT id FROM question_group WHERE i18n_key = 'satisfaction');

INSERT IGNORE INTO question (DTYPE, create_date, question_group_id, choice_group_id, i18n_key, requirement, selection_type, sort_order) VALUES
  ('ChoiceQuestion', NOW(), @question_group_id, @satisfaction_id, 'satisfaction', 'OPTIONAL', 'SINGLE', 1),
  ('ChoiceQuestion', NOW(), @question_group_id, @yes_no_id, 'match_success', 'OPTIONAL', 'SINGLE', 2),
  ('ChoiceQuestion', NOW(), @question_group_id, @satisfaction_id, 'satisfaction_other', 'OPTIONAL', 'SINGLE', 3);

INSERT IGNORE INTO i18n (create_date, language, i18n_key, i18n_value, base_name) VALUES
  (NOW(), 'DE', 'satisfaction', 'satisfaction', 'CHOICE_GROUP'),

  (NOW(), 'DE', 'SATISFIED', 'zufrieden', 'CHOICE_VALUE'),
  (NOW(), 'DE', 'RATHER_SATISFIED', 'eher zufrieden', 'CHOICE_VALUE'),
  (NOW(), 'DE', 'RATHER_UNSATISFIED', 'eher unzufrieden', 'CHOICE_VALUE'),
  (NOW(), 'DE', 'UNSATISFIED', 'unzufrieden', 'CHOICE_VALUE'),
  (NOW(), 'DE', 'NO_JUDGE', 'kann ich noch nicht beurteilen', 'CHOICE_VALUE'),

  (NOW(), 'DE', 'satisfaction', 'Abfrage der Zufriedenheit', 'QUESTION_GROUP_NAME'),
  (NOW(), 'DE', 'satisfaction', 'Bitte geben Sie hier an, wie zufrieden Sie mit der Gleichklang Plattform sind und ob Sie bei der Vermittlung bereits Erfolg hatten:', 'QUESTION_GROUP_DESCRIPTION'),

  (NOW(), 'DE', 'satisfaction', 'Zufriedenheit mit Gleichklang', 'QUESTION_NAME'),
  (NOW(), 'DE', 'satisfaction', 'Ich bin mit Gleichklang insgesamt:', 'QUESTION_DESCRIPTION'),
  (NOW(), 'DE', 'match_success', 'Vermittlungserfolg', 'QUESTION_NAME'),
  (NOW(), 'DE', 'match_success', 'Ich habe mit Gleichklang bereits Erfolg gehabt:', 'QUESTION_DESCRIPTION'),
  (NOW(), 'DE', 'satisfaction_other', 'Zufriedenheit mit anderen Plattformen', 'QUESTION_NAME'),
  (NOW(), 'DE', 'satisfaction_other', 'Bitte geben Sie hier an, wie zufrieden Sie in der Vergangenheit mit ANDEREN Partnerbörsen/Kennenlern-Plattformen waren:', 'QUESTION_DESCRIPTION');

INSERT IGNORE INTO answer (DTYPE, create_date, question_id, user_id)
  SELECT
    'ChoiceAnswer',
    NOW(),
    q.id,
    us.user_id
  FROM
    user_settings us LEFT JOIN question q ON
    q.i18n_key = 'satisfaction' OR
    q.i18n_key = 'match_success' OR
    q.i18n_key = 'satisfaction_other' OR
    q.i18n_key = 'press';

SET @yes=(SELECT id FROM choice WHERE i18n_key = 'yes_opt');
SET @no=(SELECT id FROM choice WHERE i18n_key = 'yes_opt');

SET @sa1=(SELECT id FROM choice WHERE i18n_key = 'SATISFIED');
SET @sa2=(SELECT id FROM choice WHERE i18n_key = 'RATHER_SATISFIED');
SET @sa3=(SELECT id FROM choice WHERE i18n_key = 'RATHER_UNSATISFIED');
SET @sa4=(SELECT id FROM choice WHERE i18n_key = 'UNSATISFIED');
SET @sa5=(SELECT id FROM choice WHERE i18n_key = 'NO_JUDGE');

INSERT IGNORE INTO choice_answer (answer_id, choice_id)
  SELECT
    a.id,
    CASE us.satisfaction_gleichklang
    WHEN 'SATISFIED'
      THEN @sa1
    WHEN 'RATHER_SATISFIED'
      THEN @sa2
    WHEN 'RATHER_UNSATISFIED'
      THEN @sa3
    WHEN 'UNSATISFIED'
      THEN @sa4
    ELSE
      @sa5
    END
  FROM answer a
    LEFT JOIN question q ON a.question_id = q.id
    LEFT JOIN user_settings us ON us.user_id = a.user_id
  WHERE
    q.i18n_key = 'satisfaction';

INSERT IGNORE INTO choice_answer (answer_id, choice_id)
  SELECT
    a.id,
    CASE us.satisfaction_other
    WHEN 'SATISFIED'
      THEN @sa1
    WHEN 'RATHER_SATISFIED'
      THEN @sa2
    WHEN 'RATHER_UNSATISFIED'
      THEN @sa3
    WHEN 'UNSATISFIED'
      THEN @sa4
    ELSE
      @sa5
    END
  FROM answer a
    LEFT JOIN question q ON a.question_id = q.id
    LEFT JOIN user_settings us ON us.user_id = a.user_id
  WHERE
    q.i18n_key = 'satisfaction_other';

INSERT IGNORE INTO choice_answer (answer_id, choice_id)
  SELECT
    a.id,
    CASE us.matchmaking_success
    WHEN true
      THEN @yes
    WHEN false
      THEN @no
    END
  FROM answer a
    LEFT JOIN question q ON a.question_id = q.id
    LEFT JOIN user_settings us ON us.user_id = a.user_id
  WHERE
    q.i18n_key = 'match_success';

INSERT IGNORE INTO choice_answer (answer_id, choice_id)
  SELECT
    a.id,
    CASE us.talk_to_press
    WHEN true
      THEN @yes
    WHEN false
      THEN @no
    END
  FROM answer a
    LEFT JOIN question q ON a.question_id = q.id
    LEFT JOIN user_settings us ON us.user_id = a.user_id
  WHERE
    q.i18n_key = 'press';

CALL DROP_COLUMN('user_settings', 'talk_to_press');
CALL DROP_COLUMN('user_settings', 'matchmaking_success');
CALL DROP_COLUMN('user_settings', 'satisfaction_other');
CALL DROP_COLUMN('user_settings', 'satisfaction_gleichklang');
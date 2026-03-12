CALL DROP_COLUMN('choice_group', 'legacy_name');
CALL DROP_COLUMN('choice', 'choice');

INSERT IGNORE INTO choice_group (create_date, i18n_key, deleted) VALUES (NOW(), 'standard_typus_anzeige', 0);

SET @standard_id=(SELECT id FROM choice_group WHERE i18n_key = 'standard_typus_anzeige');

INSERT IGNORE INTO choice (create_date, choice_group_id, i18n_key, sort_order) VALUES
    (NOW(), @standard_id, 'standard_typus_anzeige_nein', 0),
    (NOW(), @standard_id, 'standard_typus_anzeige_eher_nein', 1),
    (NOW(), @standard_id, 'standard_typus_anzeige_unentschieden', 2),
    (NOW(), @standard_id, 'standard_typus_anzeige_eher_ja', 3),
    (NOW(), @standard_id, 'standard_typus_anzeige_ja', 4);

SET @nein=(SELECT id FROM choice WHERE i18n_key = 'standard_typus_anzeige_nein');
SET @eher_nein=(SELECT id FROM choice WHERE i18n_key = 'standard_typus_anzeige_eher_nein');
SET @unentschieden=(SELECT id FROM choice WHERE i18n_key = 'standard_typus_anzeige_unentschieden');
SET @eher_ja=(SELECT id FROM choice WHERE i18n_key = 'standard_typus_anzeige_eher_ja');
SET @ja=(SELECT id FROM choice WHERE i18n_key = 'standard_typus_anzeige_ja');

UPDATE question SET choice_group_id = @standard_id WHERE i18n_key IN
(
    'person.relprev_ch',
    'person.relprev_is',
    'person.relprev_jud',
    'person.relprev_bud',
    'person.relprev_hin',
    'person.relprev_schin',
    'person.relprev_bahai',
    'person.relprev_eso',
    'person.relprev_agn',
    'person.ordnungsliebe'
);

INSERT IGNORE INTO i18n (create_date, language, i18n_key, i18n_value, base_name) VALUES
  (NOW(), 'DE', 'standard_typus_anzeige', 'Standard-Typus-Anzeige', 'CHOICE_GROUP'),

  (NOW(), 'DE', 'standard_typus_anzeige_nein', 'nein', 'CHOICE_VALUE'),
  (NOW(), 'DE', 'standard_typus_anzeige_eher_nein', 'eher nein', 'CHOICE_VALUE'),
  (NOW(), 'DE', 'standard_typus_anzeige_unentschieden', 'unentschieden', 'CHOICE_VALUE'),
  (NOW(), 'DE', 'standard_typus_anzeige_eher_ja', 'eher ja', 'CHOICE_VALUE'),
  (NOW(), 'DE', 'standard_typus_anzeige_ja', 'ja', 'CHOICE_VALUE');

UPDATE answer a LEFT JOIN question q ON a.question_id = q.id JOIN choice_answer ca ON ca.answer_id = a.id LEFT JOIN choice c ON ca.choice_id = c.id
SET ca.choice_id = CASE c.i18n_key
     WHEN 'eher_nein'
       THEN @eher_nein
     WHEN 'eher_ja'
        THEN @eher_ja
     WHEN 'auf_keinen_fall'
        THEN @nein
     WHEN 'auf_jeden_fall'
        THEN @ja
     WHEN 'unentschieden'
        THEN @unentschieden
     ELSE ca.choice_id END
WHERE q.i18n_key IN
(
    'person.relprev_ch',
    'person.relprev_is',
    'person.relprev_jud',
    'person.relprev_bud',
    'person.relprev_hin',
    'person.relprev_schin',
    'person.relprev_bahai',
    'person.relprev_eso',
    'person.relprev_agn',
    'person.ordnungsliebe'
);

DELETE qm FROM questions_mapping qm JOIN question q ON qm.source_question_id = q.id OR qm.target_question_id = q.id WHERE q.i18n_key IN
(
    'person.relprev_ch',
    'person.relprev_is',
    'person.relprev_jud',
    'person.relprev_bud',
    'person.relprev_hin',
    'person.relprev_schin',
    'person.relprev_bahai',
    'person.relprev_eso',
    'person.relprev_agn',
    'person.ordnungsliebe'
);

DELETE ac FROM activator_choice ac JOIN activator a ON a.id = ac.activator_id JOIN question q ON a.activating_question_id = q.id WHERE q.i18n_key IN
(
    'person.relprev_ch',
    'person.relprev_is',
    'person.relprev_jud',
    'person.relprev_bud',
    'person.relprev_hin',
    'person.relprev_schin',
    'person.relprev_bahai',
    'person.relprev_eso',
    'person.relprev_agn',
    'person.ordnungsliebe'
);

DELETE a FROM activator a JOIN question q ON a.activating_question_id = q.id WHERE q.i18n_key IN
(
    'person.relprev_ch',
    'person.relprev_is',
    'person.relprev_jud',
    'person.relprev_bud',
    'person.relprev_hin',
    'person.relprev_schin',
    'person.relprev_bahai',
    'person.relprev_eso',
    'person.relprev_agn',
    'person.ordnungsliebe'
);
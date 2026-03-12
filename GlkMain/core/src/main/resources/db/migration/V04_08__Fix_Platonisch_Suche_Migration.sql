SET @choice_group_id=(SELECT id FROM choice_group WHERE i18n_key = 'Platonisch');

UPDATE choice SET sort_order = 4 WHERE i18n_key = 'nur_vorschläge,...atonisch_suchen';

UPDATE choice SET i18n_key = 'ja_nur', sort_order = 3 WHERE i18n_key = 'unentschieden$6';
UPDATE i18n SET i18n_key = 'ja_nur' WHERE i18n_key = 'unentschieden$6';
UPDATE i18n SET i18n_value = 'ja nur' WHERE i18n_key = 'ja_nur' AND language = 'DE';
UPDATE i18n SET i18n_value = 'yes only' WHERE i18n_key = 'ja_nur' AND language = 'EN';

UPDATE choice SET i18n_key = 'unentschieden_platonisch', sort_order = 2 WHERE i18n_key = 'ja nur$6';
UPDATE i18n SET i18n_key = 'unentschieden_platonisch' WHERE i18n_key = 'ja nur$6';
UPDATE i18n SET i18n_value = 'Unentschieden' WHERE i18n_key = 'unentschieden_platonisch' AND language = 'DE';
UPDATE i18n SET i18n_value = 'Undecided' WHERE i18n_key = 'unentschieden_platonisch' AND language = 'EN';

INSERT IGNORE INTO choice (create_date, change_date, sort_order, i18n_key, choice_group_id)
    VALUES (NOW(), NOW(), 2, 'unentschieden_platonisch', @choice_group_id);

INSERT IGNORE INTO i18n (create_date, language, i18n_key, i18n_value, base_name) VALUES
  (NOW(), 'DE', 'unentschieden_platonisch', 'Unentschieden', 'CHOICE_VALUE'),
  (NOW(), 'EN', 'unentschieden_platonisch', 'Undecided', 'CHOICE_VALUE');

-- migrate missing undecided

SET @choice_id=(SELECT id FROM choice WHERE i18n_key = 'unentschieden_platonisch' AND choice_group_id = @choice_group_id);
SET @question_id=(SELECT id FROM question WHERE i18n_key = 'partner.p_platonisch_suche');

INSERT IGNORE INTO answer (DTYPE, legacy_id, create_date, change_date, question_id, user_id)
    SELECT
        'ChoiceAnswer',
        cp.no,
        NOW(),
        NOW(),
        @question_id,
        u.id
    FROM comppartner cp JOIN user_ u on u.legacy_id = cp.owner WHERE cp.p_platonisch_suche IS NULL;

INSERT IGNORE INTO choice_answer (answer_id, choice_id)
    SELECT
        a.id,
        @choice_id
    FROM answer a LEFT JOIN choice_answer ca ON ca.answer_id = a.id WHERE a.question_id = @question_id AND ca.choice_id IS NULL;
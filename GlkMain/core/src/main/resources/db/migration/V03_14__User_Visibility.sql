UPDATE question
SET
  adjustable_relationship_visibility = TRUE
WHERE i18n_key IN
      (
        'person.hochsensibel_info',
        'partner.p_erotik',
        'freund.f_erotik',
        'partner.p_platonisch_suche',
        'freund.f_gay_select',
        'partner.p_bdsm_suche',
        'freund.f_bdsm_suche',
        'person.relprev_ch',
        'person.relprev_is',
        'person.relprev_jud',
        'person.relprev_bud',
        'person.relprev_hin',
        'person.relprev_schin',
        'person.relprev_bahai',
        'person.relprev_eso',
        'person.relprev_agn'
      );
CALL ADD_COLUMN('answer', 'relationship_visible', 'bit(1) DEFAULT TRUE');
SELECT id
INTO @yes_opt
FROM choice
WHERE i18n_key = 'yes_opt';
SELECT id
INTO @no_opt
FROM choice
WHERE i18n_key = 'no_opt';

DELIMITER //
DROP PROCEDURE IF EXISTS `INSERT_ANSWER_VISIBLE` //

-- Inserts relationship visibility to answers table
--
-- question     : question for which visibility should be inserted
-- question_visible     : question defining the visibility
CREATE PROCEDURE `INSERT_ANSWER_VISIBLE`
  (
    _question         VARCHAR(255),
    _question_visible VARCHAR(255)
  )
  BEGIN
 -- saves ids of yes/no choices to variables
    SELECT id
    INTO @yes_opt
    FROM choice
    WHERE i18n_key = 'yes_opt';
    SELECT id
    INTO @no_opt
    FROM choice
    WHERE i18n_key = 'no_opt';

    UPDATE answer AS a
      JOIN question AS q
        ON q.i18n_key = _question AND a.question_id = q.id
      JOIN answer AS a2 ON a.user_id = a2.user_id
      JOIN question AS q2
        ON q2.i18n_key = _question_visible AND a2.question_id = q2.id
      JOIN choice_answer AS ca2
        ON a2.id = ca2.answer_id
    SET a.relationship_visible = ca2.choice_id = @yes_opt;
  END //

DELIMITER;

CALL INSERT_ANSWER_VISIBLE('person.hochsensibel_info', 'person.hochsensibel_mitteil');
CALL INSERT_ANSWER_VISIBLE('partner.p_erotik', 'partner.p_erotik_mitteil');
CALL INSERT_ANSWER_VISIBLE('freund.f_erotik', 'freund.f_erotik_mitteil');
CALL INSERT_ANSWER_VISIBLE('partner.p_platonisch_suche', 'partner.p_platonisch_mitteil');
CALL INSERT_ANSWER_VISIBLE('freund.f_gay_select', 'freund.f_gay_mitteil');
CALL INSERT_ANSWER_VISIBLE('partner.p_bdsm_suche', 'partner.p_bdsm_mitteil');
CALL INSERT_ANSWER_VISIBLE('freund.f_bdsm_suche', 'freund.f_bdsm_mitteil');
CALL INSERT_ANSWER_VISIBLE('person.relprev_ch', 'person.rel_mitteil');
CALL INSERT_ANSWER_VISIBLE('person.relprev_is', 'person.rel_mitteil');
CALL INSERT_ANSWER_VISIBLE('person.relprev_jud', 'person.rel_mitteil');
CALL INSERT_ANSWER_VISIBLE('person.relprev_bud', 'person.rel_mitteil');
CALL INSERT_ANSWER_VISIBLE('person.relprev_hin', 'person.rel_mitteil');
CALL INSERT_ANSWER_VISIBLE('person.relprev_schin', 'person.rel_mitteil');
CALL INSERT_ANSWER_VISIBLE('person.relprev_bahai', 'person.rel_mitteil');
CALL INSERT_ANSWER_VISIBLE('person.relprev_eso', 'person.rel_mitteil');
CALL INSERT_ANSWER_VISIBLE('person.relprev_agn', 'person.rel_mitteil');

UPDATE question SET deleted = TRUE WHERE i18n_key LIKE '%_mitteil';
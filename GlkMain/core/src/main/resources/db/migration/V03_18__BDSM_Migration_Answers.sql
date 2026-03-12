# Invariants
SET @partner_allg_name_old = _utf8'partner.p_bdsm_suche_allg' COLLATE utf8_unicode_ci;
SET @freund_allg_name_old = _utf8'freund.f_bdsm_suche_allg' COLLATE utf8_unicode_ci;
SET @partner_spez_name_old = _utf8'partner.p_bdsm_suche_spezial' COLLATE utf8_unicode_ci;
SET @freund_spez_name_old = _utf8'freund.f_bdsm_suche_spezial' COLLATE utf8_unicode_ci;

SET @partner_allg_name = _utf8'partner.p_bdsm_suche_allg_new_' COLLATE utf8_unicode_ci;
SET @freund_allg_name = _utf8'freund.f_bdsm_suche_allg_new_' COLLATE utf8_unicode_ci;
SET @partner_spez_name = _utf8'partner.p_bdsm_suche_spezial_new_' COLLATE utf8_unicode_ci;
SET @freund_spez_name = _utf8'freund.f_bdsm_suche_spezial_new_' COLLATE utf8_unicode_ci;

SET @partner_bdsm_suche_old = _utf8'partner.p_bdsm_suche' COLLATE utf8_unicode_ci;
SET @partner_bdsm_mitteil_old = _utf8'partner.p_bdsm_mitteil' COLLATE utf8_unicode_ci;

SET @freund_bdsm_suche_old = _utf8'freund.f_bdsm_suche' COLLATE utf8_unicode_ci;
SET @freund_bdsm_mitteil_old = _utf8'freund.f_bdsm_mitteil' COLLATE utf8_unicode_ci;

DELETE ca FROM choice_answer AS ca
  JOIN answer AS a ON ca.answer_id = a.id
  JOIN question AS q ON a.question_id = q.id
WHERE q.i18n_key IN
      (@partner_allg_name, @partner_spez_name, @freund_allg_name, @freund_spez_name);

DELETE a FROM answer AS a
  JOIN question AS q ON a.question_id = q.id
WHERE q.i18n_key IN
      (@partner_allg_name, @partner_spez_name, @freund_allg_name, @freund_spez_name);


# Migrate answers

DELIMITER //
DROP PROCEDURE IF EXISTS REPLACE_CHOICE_ANSWER //

-- Replace values in choice_answer
-- _old_question_key  question to change
-- _old_choice_key    choice to change
-- _new_question_key  replacement question
-- _new_choice_key    replacement choice

CREATE PROCEDURE REPLACE_CHOICE_ANSWER
  (
    `_old_question_key` VARCHAR(255),
    `_old_choice_key`   VARCHAR(255),
    `_new_question_key` VARCHAR(255),
    `_new_choice_key`   VARCHAR(255)
  )
  BEGIN
    SELECT id
    INTO @old_question_id
    FROM question
    WHERE i18n_key = `_old_question_key`;
    SELECT id
    INTO @new_question_id
    FROM question
    WHERE i18n_key = `_new_question_key`;
    SELECT id
    INTO @old_choice_id
    FROM choice
    WHERE i18n_key = `_old_choice_key`;
    SELECT id
    INTO @new_choice_id
    FROM choice
    WHERE i18n_key = `_new_choice_key`;

    INSERT INTO choice_answer (answer_id, choice_id)
      SELECT
        a_new.id,
        @new_choice_id
      FROM answer AS a_old
        JOIN choice_answer AS ca_old
          ON
            a_old.id = ca_old.answer_id AND a_old.question_id = @old_question_id
            AND
            ca_old.choice_id = @old_choice_id
        CROSS JOIN answer AS a_new ON a_new.question_id = @new_question_id AND
                                      a_old.user_id = a_new.user_id;
  END //

DELIMITER ;


# Partner allg answers

SELECT id INTO @partner_old_question_id FROM question WHERE i18n_key = @partner_bdsm_suche_old;
SELECT id INTO @partner_old__question_visible_id FROM question WHERE i18n_key = @partner_bdsm_mitteil_old;

SELECT id INTO @freund_old_question_id FROM question WHERE i18n_key = @freund_bdsm_suche_old;
SELECT id INTO @freund_old_question_visible_id FROM question WHERE i18n_key = @freund_bdsm_mitteil_old;

SELECT id INTO @partner_allg_question_id FROM question WHERE i18n_key = @partner_allg_name;
SELECT id INTO @freund_allg_question_id FROM question WHERE i18n_key = @freund_allg_name;
SELECT id INTO @partner_spez_question_id FROM question WHERE i18n_key = @partner_spez_name;
SELECT id INTO @freund_spez_question_id FROM question WHERE i18n_key = @freund_spez_name;

SELECT id INTO @yes_opt FROM choice WHERE i18n_key = 'yes_opt';

INSERT INTO answer (DTYPE, question_id, user_id, relationship_visible)
  SELECT
    'ChoiceAnswer',
    @partner_allg_question_id,
    a.user_id,
    IFNULL(visibility_ca.choice_id = @yes_opt, TRUE)
  FROM answer a
    LEFT JOIN answer AS visibility_a ON a.user_id = visibility_a.user_id
    LEFT JOIN choice_answer AS visibility_ca
      ON visibility_a.id = visibility_ca.answer_id
  WHERE a.question_id = @partner_old_question_id AND
        visibility_a.question_id = @partner_old__question_visible_id;


CALL REPLACE_CHOICE_ANSWER(@partner_bdsm_suche_old,
                           'nein,_auf_keine...auf_keinen_fall',
                           @partner_allg_name,
                           CONCAT(@partner_allg_name, 1));

CALL REPLACE_CHOICE_ANSWER(@partner_bdsm_suche_old,
                           'nein,_aber_ich_...verzichten_kann',
                           @partner_allg_name,
                           CONCAT(@partner_allg_name, 2));

CALL REPLACE_CHOICE_ANSWER(@partner_bdsm_suche_old,
                           'ja,_auch__partn...rolle_einnehmen',
                           @partner_allg_name,
                           CONCAT(@partner_allg_name, 3));

CALL REPLACE_CHOICE_ANSWER(@partner_bdsm_suche_old,
                           'ja,_auch__partn...rolle_einnehmen$1',
                           @partner_allg_name,
                           CONCAT(@partner_allg_name, 3));

CALL REPLACE_CHOICE_ANSWER(@partner_bdsm_suche_old,
                           'ja,_auch__egal_..._rolle_einnimmt',
                           @partner_allg_name,
                           CONCAT(@partner_allg_name, 3));

CALL REPLACE_CHOICE_ANSWER(@partner_bdsm_suche_old,
                           'ja,_auch__partn...chsel_einnehmen',
                           @partner_allg_name,
                           CONCAT(@partner_allg_name, 3));

CALL REPLACE_CHOICE_ANSWER(@partner_bdsm_suche_old,
                           'ja,_nur__partne...rolle_einnehmen',
                           @partner_allg_name,
                           CONCAT(@partner_allg_name, 4));

CALL REPLACE_CHOICE_ANSWER(@partner_bdsm_suche_old,
                           'ja,_nur__partne...rolle_einnehmen$1',
                           @partner_allg_name,
                           CONCAT(@partner_allg_name, 4));

CALL REPLACE_CHOICE_ANSWER(@partner_bdsm_suche_old,
                           'ja,_nur__egal_o..._rolle_einnimmt',
                           @partner_allg_name,
                           CONCAT(@partner_allg_name, 4));

CALL REPLACE_CHOICE_ANSWER(@partner_bdsm_suche_old,
                           'ja,_nur__partne...chsel_einnehmen',
                           @partner_allg_name,
                           CONCAT(@partner_allg_name, 4));

# Partner speziel answers
INSERT INTO answer (DTYPE, question_id, user_id, relationship_visible)
  SELECT
    'ChoiceAnswer',
    @partner_spez_question_id,
    a.user_id,
    IFNULL(visibility_ca.choice_id = @yes_opt, TRUE)
  FROM answer a
    LEFT JOIN answer AS visibility_a ON a.user_id = visibility_a.user_id
    LEFT JOIN choice_answer AS visibility_ca ON visibility_a.id = visibility_ca.answer_id
  WHERE a.question_id = @partner_old_question_id AND
        visibility_a.question_id = @partner_old__question_visible_id;

CALL REPLACE_CHOICE_ANSWER(@partner_bdsm_suche_old,
                           'ja,_auch__partn...rolle_einnehmen',
                           @partner_spez_name,
                           CONCAT(@partner_spez_name, 1));

CALL REPLACE_CHOICE_ANSWER(@partner_bdsm_suche_old,
                           'ja,_auch__partn...rolle_einnehmen$1',
                           @partner_spez_name,
                           CONCAT(@partner_spez_name, 2));

CALL REPLACE_CHOICE_ANSWER(@partner_bdsm_suche_old,
                           'ja,_auch__egal_..._rolle_einnimmt',
                           @partner_spez_name,
                           CONCAT(@partner_spez_name, 3));

CALL REPLACE_CHOICE_ANSWER(@partner_bdsm_suche_old,
                           'ja,_auch__partn...chsel_einnehmen',
                           @partner_spez_name,
                           CONCAT(@partner_spez_name, 4));


CALL REPLACE_CHOICE_ANSWER(@partner_bdsm_suche_old,
                           'ja,_nur__partne...rolle_einnehmen',
                           @partner_spez_name,
                           CONCAT(@partner_spez_name, 1));

CALL REPLACE_CHOICE_ANSWER(@partner_bdsm_suche_old,
                           'ja,_nur__partne...rolle_einnehmen$1',
                           @partner_spez_name,
                           CONCAT(@partner_spez_name, 2));

CALL REPLACE_CHOICE_ANSWER(@partner_bdsm_suche_old,
                           'ja,_nur__egal_o..._rolle_einnimmt',
                           @partner_spez_name,
                           CONCAT(@partner_spez_name, 3));

CALL REPLACE_CHOICE_ANSWER(@partner_bdsm_suche_old,
                           'ja,_nur__partne...chsel_einnehmen',
                           @partner_spez_name,
                           CONCAT(@partner_spez_name, 4));

# Freund allg answers
INSERT INTO answer (DTYPE, question_id, user_id, relationship_visible)
  SELECT
    'ChoiceAnswer',
    @freund_allg_question_id,
    a.user_id,
    IFNULL(visibility_ca.choice_id = @yes_opt, TRUE)
  FROM answer a
    LEFT JOIN answer AS visibility_a ON a.user_id = visibility_a.user_id
    LEFT JOIN choice_answer AS visibility_ca ON visibility_a.id = visibility_ca.answer_id
  WHERE a.question_id = @freund_old_question_id AND
        visibility_a.question_id = @freund_old_question_visible_id;


CALL REPLACE_CHOICE_ANSWER(@freund_bdsm_suche_old,
                           'nein,_auf_keine...auf_keinen_fall$2',
                           @freund_allg_name,
                           CONCAT(@freund_allg_name, 1));

CALL REPLACE_CHOICE_ANSWER(@freund_bdsm_suche_old,
                           'nein,_aber_ich_...verzichten_kann$1',
                           @freund_allg_name,
                           CONCAT(@freund_allg_name, 2));

CALL REPLACE_CHOICE_ANSWER(@freund_bdsm_suche_old,
                           'ja,_auch__freun...rolle_einnehmen',
                           @freund_allg_name,
                           CONCAT(@freund_allg_name, 3));

CALL REPLACE_CHOICE_ANSWER(@freund_bdsm_suche_old,
                           'ja,_auch__freun...rolle_einnehmen$1',
                           @freund_allg_name,
                           CONCAT(@freund_allg_name, 3));

CALL REPLACE_CHOICE_ANSWER(@freund_bdsm_suche_old,
                           'ja,_auch__egal_..._rolle_einnimmt$1',
                           @freund_allg_name,
                           CONCAT(@freund_allg_name, 3));

CALL REPLACE_CHOICE_ANSWER(@freund_bdsm_suche_old,
                           'ja,_auch__freun...chsel_einnehmen',
                           @freund_allg_name,
                           CONCAT(@freund_allg_name, 3));

CALL REPLACE_CHOICE_ANSWER(@freund_bdsm_suche_old,
                           'ja,_nur__freund...rolle_einnehmen',
                           @freund_allg_name,
                           CONCAT(@freund_allg_name, 4));

CALL REPLACE_CHOICE_ANSWER(@freund_bdsm_suche_old,
                           'ja,_nur__freund...rolle_einnehmen$1',
                           @freund_allg_name,
                           CONCAT(@freund_allg_name, 4));

CALL REPLACE_CHOICE_ANSWER(@freund_bdsm_suche_old,
                           'ja,_nur__egal_o..._rolle_einnimmt$1',
                           @freund_allg_name,
                           CONCAT(@freund_allg_name, 4));

CALL REPLACE_CHOICE_ANSWER(@freund_bdsm_suche_old,
                           'ja,_nur__freund...chsel_einnehmen',
                           @freund_allg_name,
                           CONCAT(@freund_allg_name, 4));

# Freund speziel answers
INSERT INTO answer (DTYPE, question_id, user_id, relationship_visible)
  SELECT
    'ChoiceAnswer',
    @freund_spez_question_id,
    a.user_id,
    IFNULL(visibility_ca.choice_id = @yes_opt, TRUE)
  FROM answer a
    LEFT JOIN answer AS visibility_a ON a.user_id = visibility_a.user_id
    LEFT JOIN choice_answer AS visibility_ca ON visibility_a.id = visibility_ca.answer_id
  WHERE a.question_id = @freund_old_question_id AND
        visibility_a.question_id = @freund_old_question_visible_id;


CALL REPLACE_CHOICE_ANSWER(@freund_bdsm_suche_old,
                           'ja,_auch__freun...rolle_einnehmen',
                           @freund_spez_name,
                           CONCAT(@freund_spez_name, 1));

CALL REPLACE_CHOICE_ANSWER(@freund_bdsm_suche_old,
                           'ja,_auch__freun...rolle_einnehmen$1',
                           @freund_spez_name,
                           CONCAT(@freund_spez_name, 2));

CALL REPLACE_CHOICE_ANSWER(@freund_bdsm_suche_old,
                           'ja,_auch__egal_..._rolle_einnimmt$1',
                           @freund_spez_name,
                           CONCAT(@freund_spez_name, 3));

CALL REPLACE_CHOICE_ANSWER(@freund_bdsm_suche_old,
                           'ja,_auch__freun...chsel_einnehmen',
                           @freund_spez_name,
                           CONCAT(@freund_spez_name, 4));


CALL REPLACE_CHOICE_ANSWER(@freund_bdsm_suche_old,
                           'ja,_nur__freund...rolle_einnehmen',
                           @freund_spez_name,
                           CONCAT(@freund_spez_name, 1));

CALL REPLACE_CHOICE_ANSWER(@freund_bdsm_suche_old,
                           'ja,_nur__freund...rolle_einnehmen$1',
                           @freund_spez_name,
                           CONCAT(@freund_spez_name, 2));

CALL REPLACE_CHOICE_ANSWER(@freund_bdsm_suche_old,
                           'ja,_nur__egal_o..._rolle_einnimmt$1',
                           @freund_spez_name,
                           CONCAT(@freund_spez_name, 3));

CALL REPLACE_CHOICE_ANSWER(@freund_bdsm_suche_old,
                           'ja,_nur__freund...chsel_einnehmen',
                           @freund_spez_name,
                           CONCAT(@freund_spez_name, 4));

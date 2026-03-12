# Partly duplicates fixed version of V03_18__BDSM_Migration_Answers.sql

SET @partner_allg_name = _utf8'partner.p_bdsm_suche_allg_new_' COLLATE utf8_unicode_ci;
SET @partner_spez_name = _utf8'partner.p_bdsm_suche_spezial_new_' COLLATE utf8_unicode_ci;
SET @partner_bdsm_mitteil_old = _utf8'partner.p_bdsm_mitteil' COLLATE utf8_unicode_ci;

SET @freund_allg_name = _utf8'freund.f_bdsm_suche_allg_new_' COLLATE utf8_unicode_ci;
SET @freund_spez_name = _utf8'freund.f_bdsm_suche_spezial_new_' COLLATE utf8_unicode_ci;
SET @freund_bdsm_mitteil_old = _utf8'freund.f_bdsm_mitteil' COLLATE utf8_unicode_ci;

SELECT id INTO @partner_allg_question_id FROM question WHERE i18n_key = @partner_allg_name;
SELECT id INTO @partner_spez_question_id FROM question WHERE i18n_key = @partner_spez_name;
SELECT id INTO @partner_old__question_visible_id FROM question WHERE i18n_key = @partner_bdsm_mitteil_old;

SELECT id INTO @freund_allg_question_id FROM question WHERE i18n_key = @freund_allg_name;
SELECT id INTO @freund_spez_question_id FROM question WHERE i18n_key = @freund_spez_name;
SELECT id INTO @freund_old_question_visible_id FROM question WHERE i18n_key = @freund_bdsm_mitteil_old;

SELECT id INTO @yes_opt FROM choice WHERE i18n_key = 'yes_opt';

UPDATE answer a
  LEFT JOIN answer AS visibility_a ON a.user_id = visibility_a.user_id
  LEFT JOIN choice_answer AS visibility_ca ON visibility_a.id = visibility_ca.answer_id
SET a.relationship_visible = IFNULL(visibility_ca.choice_id = @yes_opt, TRUE)
WHERE a.question_id = @partner_allg_question_id AND
      visibility_a.question_id = @partner_old__question_visible_id;

UPDATE answer a
  LEFT JOIN answer AS visibility_a ON a.user_id = visibility_a.user_id
  LEFT JOIN choice_answer AS visibility_ca ON visibility_a.id = visibility_ca.answer_id
SET a.relationship_visible = IFNULL(visibility_ca.choice_id = @yes_opt, TRUE)
WHERE a.question_id = @partner_spez_question_id AND
      visibility_a.question_id = @partner_old__question_visible_id;

UPDATE answer a
  LEFT JOIN answer AS visibility_a ON a.user_id = visibility_a.user_id
  LEFT JOIN choice_answer AS visibility_ca ON visibility_a.id = visibility_ca.answer_id
SET a.relationship_visible = IFNULL(visibility_ca.choice_id = @yes_opt, TRUE)
WHERE a.question_id = @freund_allg_question_id AND
      visibility_a.question_id = @freund_old_question_visible_id;

UPDATE answer a
  LEFT JOIN answer AS visibility_a ON a.user_id = visibility_a.user_id
  LEFT JOIN choice_answer AS visibility_ca ON visibility_a.id = visibility_ca.answer_id
SET a.relationship_visible = IFNULL(visibility_ca.choice_id = @yes_opt, TRUE)
WHERE a.question_id = @freund_spez_question_id AND
      visibility_a.question_id = @freund_old_question_visible_id;

SET @bdsm_nur_question_id=(SELECT id FROM question WHERE i18n_key = 'partner.p_bdsm_nur');
SET @bdsm_question_id=(SELECT id FROM question WHERE i18n_key = 'partner.p_bdsm_suche_allg_new_');
SET @bdsm_choice_group_id=(SELECT id FROM choice_group WHERE i18n_key = 'BDSM-Initial (Partner)');
SET @yes_choice_id = (SELECT id FROM choice WHERE choice_group_id = @bdsm_choice_group_id AND i18n_key = 'partner.p_bdsm_suche_allg_new_4');

DELETE choice_answer FROM choice_answer JOIN (SELECT * FROM (
    SELECT bdsm_nur_a.id FROM user_ u
        JOIN answer bdsm_nur_a ON u.id = bdsm_nur_a.user_id
        JOIN answer bdsm_a ON u.id = bdsm_a.user_id
        LEFT JOIN choice_answer ca ON ca.answer_id = bdsm_a.id
    WHERE
        bdsm_nur_a.question_id = @bdsm_nur_question_id AND
        bdsm_a.question_id = @bdsm_question_id AND
        ca.choice_id <> @yes_choice_id
) AS temp) t ON t.id = answer_id;

SET @bdsm_nur_question_id=(SELECT id FROM question WHERE i18n_key = 'freund.f_bdsm_nur');
SET @bdsm_question_id=(SELECT id FROM question WHERE i18n_key = 'freund.f_bdsm_suche_allg_new_');
SET @bdsm_choice_group_id=(SELECT id FROM choice_group WHERE i18n_key = 'BDSM-Initial (Freund)');
SET @yes_choice_id = (SELECT id FROM choice WHERE choice_group_id = @bdsm_choice_group_id AND i18n_key = 'freund.f_bdsm_suche_allg_new_4');

DELETE choice_answer FROM choice_answer JOIN (SELECT * FROM (
    SELECT bdsm_nur_a.id FROM user_ u
        JOIN answer bdsm_nur_a ON u.id = bdsm_nur_a.user_id
        JOIN answer bdsm_a ON u.id = bdsm_a.user_id
        LEFT JOIN choice_answer ca ON ca.answer_id = bdsm_a.id
    WHERE
        bdsm_nur_a.question_id = @bdsm_nur_question_id AND
        bdsm_a.question_id = @bdsm_question_id AND
        ca.choice_id <> @yes_choice_id
) AS temp) t ON t.id = answer_id;
DROP TABLE IF EXISTS legacy_locatables;
CREATE TEMPORARY TABLE legacy_locatables
  SELECT q.i18n_key
  FROM question AS q
  WHERE
    q.i18n_key IN ('partner.umzug', 'partner.p_region', 'partner.p_plz_1', 'partner.p_land_1', 'partner.p_radius_1', 'partner.p_plz_2', 'partner.p_land_2', 'partner.p_radius_2', 'partner.p_plz_3', 'partner.p_land_3', 'partner.p_radius_3', 'freund.f_region', 'freund.f_plz_1', 'freund.f_land_1', 'freund.f_radius_1', 'freund.f_plz_2', 'freund.f_land_2', 'freund.f_radius_2', 'freund.f_plz_3', 'freund.f_land_3', 'freund.f_radius_3');

UPDATE questionnaire AS q1 JOIN question_group AS qg
    ON q1.id = qg.questionnaire_id
  JOIN question AS q
    ON qg.id = q.question_group_id
  JOIN legacy_locatables AS ll
    ON q.i18n_key = ll.i18n_key
SET q.deleted = TRUE, qg.deleted = TRUE;


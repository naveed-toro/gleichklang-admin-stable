# delete unused choice groups
DELETE ca FROM choice_answer ca
  JOIN choice c ON ca.choice_id = c.id
  JOIN choice_group cg ON c.choice_group_id = cg.id
WHERE cg.legacy_id IN
      ('partner.p_bdsm_suche', 'partner.p_region', 'partner.p_land_1', 'partner.p_radius_1');
DELETE c FROM choice c
  JOIN choice_group cg ON c.choice_group_id = cg.id
WHERE cg.legacy_id IN
      ('partner.p_bdsm_suche', 'partner.p_region', 'partner.p_land_1', 'partner.p_radius_1');
DELETE a FROM answer a
  JOIN question AS q ON a.question_id = q.id
  JOIN choice_group AS cg ON q.choice_group_id = cg.id
WHERE cg.legacy_id IN
      ('partner.p_bdsm_suche', 'partner.p_region', 'partner.p_land_1', 'partner.p_radius_1');
DELETE q FROM question q
  JOIN choice_group cg ON q.choice_group_id = cg.id
WHERE cg.legacy_id IN
      ('partner.p_bdsm_suche', 'partner.p_region', 'partner.p_land_1', 'partner.p_radius_1');
DELETE cg FROM choice_group cg
WHERE legacy_id IN
      ('partner.p_bdsm_suche', 'partner.p_region', 'partner.p_land_1', 'partner.p_radius_1');

DELETE ac FROM activator_choice AS ac
  JOIN activator AS a ON ac.activator_id = a.id
JOIN question AS q ON a.activating_question_id = q.id OR a.enables_question_id = q.id JOIN question_group AS qg
WHERE qg.legacy_id = 'bdsm';

DELETE a FROM activator AS a
  JOIN question AS q ON a.activating_question_id = q.id OR a.enables_question_id = q.id JOIN question_group AS qg
WHERE qg.legacy_id = 'bdsm';

DELETE ca FROM choice_answer ca JOIN answer as a ON ca.answer_id = a.id
JOIN question as q ON a.question_id = q.id JOIN question_group as qg ON q.question_group_id = qg.id
WHERE qg.legacy_id = 'bdsm';

DELETE a FROM answer as a
  JOIN question as q ON a.question_id = q.id JOIN question_group as qg ON q.question_group_id = qg.id
WHERE qg.legacy_id = 'bdsm';


DELETE q FROM question_group qg
  JOIN question AS q ON qg.id = q.question_group_id
WHERE qg.legacy_id = 'bdsm';

DELETE qg FROM question_group qg
WHERE qg.legacy_id = 'bdsm';

# set unique index on name

CALL CREATE_UNIQUE_INDEX('choice_group_name_idx', 'choice_group', 'name');
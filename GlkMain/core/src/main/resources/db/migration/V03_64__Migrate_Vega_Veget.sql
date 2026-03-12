UPDATE choice_answer SET choice_id = (SELECT id FROM choice WHERE i18n_key = 'ja$2') WHERE answer_id IN
(
SELECT a_vege_id FROM
(
SELECT
a_vege.id AS a_vege_id
FROM
question AS q_vegan
JOIN choice_group cg_vegan ON q_vegan.choice_group_id = cg_vegan.id
JOIN choice c_vegan ON cg_vegan.id = c_vegan.choice_group_id
JOIN answer a_vegan ON a_vegan.question_id = q_vegan.id
JOIN choice_answer ca_vegan ON ca_vegan.answer_id = a_vegan.id AND ca_vegan.choice_id = c_vegan.id,
question AS q_vege
JOIN choice_group cg_vege ON q_vege.choice_group_id = cg_vege.id
JOIN choice c_vege ON cg_vege.id = c_vege.choice_group_id
JOIN answer a_vege ON a_vege.question_id = q_vege.id
JOIN choice_answer ca_vege ON ca_vege.answer_id = a_vege.id AND ca_vege.choice_id = c_vege.id
WHERE
q_vegan.i18n_key = 'person.vegan_info' AND
q_vege.i18n_key = 'person.vege_info' AND
c_vegan.i18n_key = 'ja$3' AND
c_vege.i18n_key = 'auf_keinen_fall$10' AND
a_vegan.user_id = a_vege.user_id
)
AS false_answers
);

UPDATE choice_answer SET choice_id = (SELECT id FROM choice WHERE i18n_key = 'ich_stehe_dem_v...arier_zu_werden') WHERE answer_id IN
(
SELECT a_vege_id FROM
(
SELECT
a_vege.id AS a_vege_id
FROM
question AS q_vegan
JOIN choice_group cg_vegan ON q_vegan.choice_group_id = cg_vegan.id
JOIN choice c_vegan ON cg_vegan.id = c_vegan.choice_group_id
JOIN answer a_vegan ON a_vegan.question_id = q_vegan.id
JOIN choice_answer ca_vegan ON ca_vegan.answer_id = a_vegan.id AND ca_vegan.choice_id = c_vegan.id,
question AS q_vege
JOIN choice_group cg_vege ON q_vege.choice_group_id = cg_vege.id
JOIN choice c_vege ON cg_vege.id = c_vege.choice_group_id
JOIN answer a_vege ON a_vege.question_id = q_vege.id
JOIN choice_answer ca_vege ON ca_vege.answer_id = a_vege.id AND ca_vege.choice_id = c_vege.id
WHERE
q_vegan.i18n_key = 'person.vegan_info' AND
q_vege.i18n_key = 'person.vege_info' AND
c_vegan.i18n_key = 'ich_stehe_dem_v...ganer_zu_werden' AND
c_vege.i18n_key = 'auf_keinen_fall$10' AND
a_vegan.user_id = a_vege.user_id
)
AS false_answers
);
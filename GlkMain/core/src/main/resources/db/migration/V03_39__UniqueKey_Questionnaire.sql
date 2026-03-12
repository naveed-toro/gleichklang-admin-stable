# drop normal index
CALL DROP_INDEX('questionnaire_sort_order', 'questionnaire');

# repair index
UPDATE questionnaire SET sort_order = (SELECT * FROM (SELECT sort_order + 1 FROM questionnaire ORDER BY sort_order DESC limit 1) AS max_sort) WHERE id IN (SELECT * FROM (SELECT q1.id FROM questionnaire q1 JOIN questionnaire q2 ON q1.sort_order = q2.sort_order AND q1.id < q2.id) AS target_id);

# create unique index
CALL CREATE_UNIQUE_INDEX('questionnaire_sort_order', 'questionnaire', 'sort_order');

DROP TABLE IF EXISTS tmp_broken_answers;
CREATE TEMPORARY TABLE tmp_broken_answers
AS
  SELECT
    a.id AS answer_id,
    a.question_id as question_id,
    count(a.id)
  FROM answer AS a
    JOIN question AS q ON a.question_id = q.id
    LEFT JOIN choice_answer AS ca ON a.id = ca.answer_id
    JOIN choice AS c ON ca.choice_id = c.id
    JOIN user_ AS u ON a.user_id = u.id
  WHERE q.selection_type = 'SINGLE' AND u.email NOT LIKE 'valid%@example.com'
  GROUP BY a.id
  HAVING count(a.id) > 1;

CALL CREATE_INDEX('tba_answer_idx', 'tmp_broken_answers', 'answer_id');
CALL CREATE_INDEX('tba_question_idx', 'tmp_broken_answers', 'question_id');

DELETE ca FROM choice_answer AS ca
  JOIN tmp_broken_answers AS tba ON ca.answer_id = tba.answer_id;

DROP TABLE IF EXISTS tmp_question_choice;
CREATE TEMPORARY TABLE tmp_question_choice
AS
  SELECT
    q.id as question_id,
    c.id as choice_id
  FROM question AS q
    JOIN choice_group AS cg ON q.choice_group_id = cg.id
    JOIN choice AS c ON cg.id = c.choice_group_id WHERE q.selection_type = 'SINGLE' GROUP BY q.id;

CALL CREATE_INDEX('tqc_question_idx', 'tmp_question_choice', 'question_id');

INSERT INTO choice_answer
  SELECT tba.answer_id, tqc.choice_id FROM tmp_broken_answers as tba JOIN tmp_question_choice as tqc ON tba.question_id = tqc.question_id;


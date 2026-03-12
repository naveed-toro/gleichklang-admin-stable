# INSERT INTO answer( DTYPE, create_date, question_id, user_id)
#     SELECT REPLACE(t1.DTYPE, 'Question', 'Answer') AS DTYPE, NOW(), t1.question_id, t1.user_id
#     FROM
#         (select u.id as user_id, q.id as question_id, q.DTYPE from user_ u, question q)
#             as t1
#         left join
#         answer as a ON a.question_id = t1.question_id and a.user_id = t1.user_id
#     WHERE
#         a.id IS NULL
#     ORDER BY t1.user_id, t1.question_id;
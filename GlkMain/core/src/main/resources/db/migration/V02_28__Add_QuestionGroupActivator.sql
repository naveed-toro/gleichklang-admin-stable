CALL ADD_COLUMN('activator', 'enables_question_group_id', 'BIGINT');

CALL ADD_FOREIGN_KEY('activator', 'enables_question_group_id', 'question_group', 'id');
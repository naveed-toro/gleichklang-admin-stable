CALL ADD_COLUMN('filter', 'text_value', 'VARCHAR(255)');
CALL ADD_COLUMN('filter', 'text_question_id', 'BIGINT');

CALL ADD_FOREIGN_KEY('filter', 'text_question_id', 'question', 'id');
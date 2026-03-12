CALL ADD_COLUMN('filter', 'number_question_id', 'BIGINT');
CALL ADD_COLUMN('filter', 'min_value', 'INT');
CALL ADD_COLUMN('filter', 'max_value', 'INT');
CALL ADD_COLUMN('filter', 'min_age_value', 'INT');
CALL ADD_COLUMN('filter', 'max_age_value', 'INT');

CALL ADD_FOREIGN_KEY('filter', 'number_question_id', 'question', 'id');
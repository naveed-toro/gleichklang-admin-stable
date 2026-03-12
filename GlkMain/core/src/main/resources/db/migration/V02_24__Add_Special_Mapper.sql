CALL ADD_COLUMN('questions_mapping', 'max_age_question_id', 'BIGINT');
CALL ADD_COLUMN('questions_mapping', 'min_age_question_id', 'BIGINT');
CALL ADD_COLUMN('questions_mapping', 'avatar_question_id', 'BIGINT');
CALL ADD_COLUMN('questions_mapping', 'true_choice_id', 'BIGINT');

CALL ADD_FOREIGN_KEY('questions_mapping', 'max_age_question_id', 'question', 'id');
CALL ADD_FOREIGN_KEY('questions_mapping', 'min_age_question_id', 'question', 'id');
CALL ADD_FOREIGN_KEY('questions_mapping', 'avatar_question_id', 'question', 'id');
CALL ADD_FOREIGN_KEY('questions_mapping', 'true_choice_id', 'choice', 'id');
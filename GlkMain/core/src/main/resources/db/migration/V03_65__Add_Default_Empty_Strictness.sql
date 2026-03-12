CALL ADD_COLUMN('questions_mapping', 'default_empty_strictness', 'INT(11)');

UPDATE questions_mapping SET default_empty_strictness = 0 WHERE default_empty_strictness IS NULL;

ALTER TABLE questions_mapping MODIFY default_empty_strictness INT(11) NOT NULL;
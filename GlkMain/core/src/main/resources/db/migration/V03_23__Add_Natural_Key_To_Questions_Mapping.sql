CALL ADD_COLUMN('questions_mapping', 'natural_key', 'VARCHAR(255)');

UPDATE questions_mapping SET natural_key = CONCAT(dtype, id);

ALTER TABLE questions_mapping MODIFY natural_key VARCHAR(255) NOT NULL;

CALL CREATE_UNIQUE_INDEX('questions_mapping_natural_key', 'questions_mapping', 'natural_key');
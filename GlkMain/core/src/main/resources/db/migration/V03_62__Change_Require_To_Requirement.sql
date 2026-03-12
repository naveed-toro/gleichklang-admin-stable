CALL ADD_COLUMN('question', 'requirement', 'VARCHAR(255)');
CALL ADD_COLUMN('question', 'required', 'BIT(1)');

UPDATE question SET requirement = 'REQUIRED' WHERE required = true;
UPDATE question SET requirement = 'OPTIONAL' WHERE required = false;

CALL DROP_COLUMN('question', 'required');
ALTER TABLE question MODIFY requirement VARCHAR(255) NOT NULL;
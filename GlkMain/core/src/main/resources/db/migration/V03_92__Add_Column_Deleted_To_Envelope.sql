CALL CHANGE_COLUMN('envelope', 'deleted', 'hidden', 'BIT NOT NULL');
CALL ADD_COLUMN('envelope', 'deleted', 'BIT(1) NOT NULL DEFAULT FALSE');
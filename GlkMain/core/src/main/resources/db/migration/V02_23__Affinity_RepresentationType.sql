CALL ADD_COLUMN('question', 'representation_type', 'varchar(25) NOT NULL DEFAULT \'DEFAULT\'');

UPDATE question AS q
SET q.representation_type = 'AFFINITY'
WHERE q.DTYPE = 'ChoiceQuestion' AND q.legacy_id = 'affinity';

UPDATE question AS q
SET q.representation_type = 'RICH_TEXT'
WHERE q.legacy_id LIKE '%text%' AND q.DTYPE = 'TextQuestion';
CALL ADD_COLUMN('activator', 'natural_key', 'VARCHAR(255)');

UPDATE activator SET natural_key = CONCAT(dtype, id);

ALTER TABLE activator MODIFY natural_key VARCHAR(255) NOT NULL;

CALL CREATE_UNIQUE_INDEX('activator_natural_key', 'activator', 'natural_key');
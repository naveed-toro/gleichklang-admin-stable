CALL ADD_COLUMN('message', 'message_type', 'VARCHAR(255)');

UPDATE message SET message_type = 'CANCEL_MESSAGE' WHERE subject = 'Kontakt entfernt';
UPDATE message SET message_type = 'CANCEL_MESSAGE' WHERE subject = 'Contact deleted';
UPDATE message SET message_type = 'DEFAULT' WHERE message_type IS NULL;

ALTER TABLE message MODIFY message_type VARCHAR(255) NOT NULL;
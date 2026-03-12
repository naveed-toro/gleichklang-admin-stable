CALL ADD_COLUMN('mail_queue_entry', 'DTYPE', "VARCHAR(31) NOT NULL DEFAULT 'UnregisteredUserMailQueueEntry'");
CALL CHANGE_COLUMN('mail_queue_entry', 'recipient_email', 'recipient_email', 'varchar(255) COLLATE utf8_unicode_ci');
CALL ADD_COLUMN('mail_queue_entry', 'recipient_id', 'BIGINT');
CALL ADD_COLUMN('relationship', 'notified', 'BIT');
CALL CREATE_INDEX('relationship_notified', 'relationship', 'notified');
UPDATE relationship SET notified = false;
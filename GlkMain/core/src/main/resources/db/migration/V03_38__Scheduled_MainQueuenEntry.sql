CALL CHANGE_COLUMN('mail_queue_entry', 'send_count', 'attempts', 'INT(11) NOT NULL');
CALL ADD_COLUMN('mail_queue_entry', 'next_reminder_date', 'DATETIME');

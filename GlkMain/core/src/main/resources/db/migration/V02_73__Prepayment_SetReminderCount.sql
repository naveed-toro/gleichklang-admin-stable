CALL DROP_COLUMN('payment', 'next_reminder_date');
CALL ADD_COLUMN('payment', 'next_reminder_date', 'DATETIME');
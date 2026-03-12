CALL ADD_COLUMN('undeliverable_mail', 'incidents', 'INT NOT NULL');

CALL CREATE_UNIQUE_INDEX('undeliverable_mail_recipient_email_undeliverable_mail_reason', 'undeliverable_mail', 'recipient_email, undeliverable_mail_reason');
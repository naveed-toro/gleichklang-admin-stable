DELETE FROM mail_queue_entry WHERE delivery_status = 'SCHEDULED' AND mail_template = 'RENEWAL_CHOSEN_REMINDER';
DELETE e1 FROM mail_queue_entry e1, mail_queue_entry e2 WHERE
  e1.id < e2.id AND
  e1.delivery_status = 'SCHEDULED' AND
  e1.mail_template = 'RENEWAL_DISABLED_REMINDER' AND
  e1.mail_template = e2.mail_template AND
  e1.delivery_status = e2.delivery_status AND
  e1.recipient_id = e2.recipient_id AND
  e1.recipient_id IS NOT NULL AND
  e1.recipient_email IS NULL;
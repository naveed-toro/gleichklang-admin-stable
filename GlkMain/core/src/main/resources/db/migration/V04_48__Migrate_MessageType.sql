UPDATE message m JOIN envelope e ON e.message_id = m.id AND e.DTYPE = 'ReceiverEnvelope' SET m.message_type = 'PENDING_PAYMENT' WHERE m.subject = 'Offene Rechnung verhindert die Verlängerung' AND e.user_id IS NULL;
UPDATE message m JOIN envelope e ON e.message_id = m.id AND e.DTYPE = 'ReceiverEnvelope' SET m.message_type = 'PENDING_PAYMENT' WHERE m.subject = 'Subscription cannot be renewed due to unpaid invoice' AND e.user_id IS NULL;

UPDATE message m JOIN envelope e ON e.message_id = m.id AND e.DTYPE = 'ReceiverEnvelope' SET m.message_type = 'ABUSE' WHERE m.subject like 'Missbrauch: %' AND e.user_id IS NULL;
UPDATE message m JOIN envelope e ON e.message_id = m.id AND e.DTYPE = 'ReceiverEnvelope' SET m.message_type = 'ABUSE' WHERE m.subject like 'Abuse: %' AND e.user_id IS NULL;
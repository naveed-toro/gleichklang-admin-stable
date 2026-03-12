DELETE FROM envelope WHERE message_id IN (SELECT m_id FROM (SELECT m.id AS m_id FROM message m LEFT JOIN envelope sender ON sender.message_id = m.id AND sender.DTYPE = 'SenderEnvelope' LEFT JOIN envelope receiver ON receiver.message_id = m.id AND receiver.DTYPE = 'ReceiverEnvelope' WHERE receiver.id IS NULL OR sender.id IS NULL) AS without_envelope);

SET FOREIGN_KEY_CHECKS = 0;
DELETE FROM message WHERE id IN (SELECT m_id FROM (SELECT m.id AS m_id FROM message m LEFT JOIN envelope sender ON sender.message_id = m.id AND sender.DTYPE = 'SenderEnvelope' LEFT JOIN envelope receiver ON receiver.message_id = m.id AND receiver.DTYPE = 'ReceiverEnvelope' WHERE receiver.id IS NULL OR sender.id IS NULL) AS without_envelope);
SET FOREIGN_KEY_CHECKS = 1;
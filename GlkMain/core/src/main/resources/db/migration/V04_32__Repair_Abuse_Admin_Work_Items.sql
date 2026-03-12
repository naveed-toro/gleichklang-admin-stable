INSERT INTO admin_work_item
(create_date, message_id, work_item_status)
  SELECT
    NOW(),
    m.id,
    'NEW'
  FROM message m
    JOIN envelope e ON e.message_id = m.id
  WHERE
    e.user_id IS NULL AND
    e.dtype = 'ReceiverEnvelope' AND
    NOT EXISTS (SELECT 1 FROM admin_work_item ai where ai.message_id = m.id);
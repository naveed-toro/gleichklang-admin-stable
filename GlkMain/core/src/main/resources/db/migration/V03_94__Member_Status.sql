-- Set member status REGISTRATION for users who didn't have any subscriptions ever
UPDATE user_ u
  LEFT JOIN subscription s ON s.user_id = u.id
SET u.member_status = 'REGISTRATION'
WHERE u.DTYPE = 'User' AND u.email IS NOT NULL AND s.id IS NULL;

-- Set member status REGISTERED for users with active subscription
UPDATE user_ u
  LEFT JOIN subscription s ON s.user_id = u.id
SET u.member_status = 'REGISTERED'
WHERE u.DTYPE = 'User' AND u.member_status = 'CANCELED' AND s.current = TRUE;
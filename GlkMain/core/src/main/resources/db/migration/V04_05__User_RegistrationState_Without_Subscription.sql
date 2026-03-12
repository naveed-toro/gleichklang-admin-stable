INSERT INTO user_registration_state (change_date, create_date, user_id, registration_state)
  SELECT
    NOW()     AS change_date,
    NOW()     AS create_date,
    u.id      AS user_id,
    'PAYMENT' AS registration_state
  FROM user_ u
  WHERE u.member_status = 'REGISTERED' AND
        NOT exists(SELECT 1 FROM subscription WHERE current = TRUE AND user_id = u.id) AND
        NOT exists(SELECT 1 FROM user_registration_state WHERE user_id = u.id);

UPDATE user_registration_state state
SET state.registration_state = 'PAYMENT'
WHERE state.registration_state <> 'PAYMENT' AND
      state.user_id IN (SELECT id FROM user_ u WHERE u.member_status = 'REGISTERED' AND
      NOT exists(SELECT 1 FROM subscription WHERE current = TRUE AND user_id = u.id));

UPDATE user_ u
SET u.member_status = 'REGISTRATION'
WHERE u.member_status = 'REGISTERED' AND
      NOT exists(SELECT 1 FROM subscription WHERE current = TRUE AND user_id = u.id);

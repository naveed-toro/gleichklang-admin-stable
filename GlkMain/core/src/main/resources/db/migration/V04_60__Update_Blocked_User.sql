
ALTER TABLE user_
  ADD column  isBlocked bit(1) DEFAULT b'0';

  commit;

UPDATE user_
SET isBlocked = 1
WHERE id IN (
SELECT user_id
FROM scamming
);

commit;

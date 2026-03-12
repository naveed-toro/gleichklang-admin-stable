
ALTER TABLE user_
  ADD column  blockedDate DATETIME DEFAULT NULL;
  commit;
ALTER TABLE user_
  ADD column  adminBlockedDate DATETIME  DEFAULT NULL;
  commit;
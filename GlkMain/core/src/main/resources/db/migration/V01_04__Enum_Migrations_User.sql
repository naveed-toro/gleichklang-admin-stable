UPDATE user_
SET
  memberStatus = CASE memberStatus
                 WHEN 'teilnehmer'
                   THEN 'ACTIVE'
                 WHEN 'exteilnehmer'
                   THEN 'EXMEMBER'
                 ELSE memberStatus
                 END,
  sex          = CASE sex
                 WHEN 'm'
                   THEN 'MALE'
                 WHEN 'w'
                   THEN 'FEMALE'
                 ELSE sex
                 END;



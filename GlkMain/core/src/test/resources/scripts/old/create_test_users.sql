SET @i = 0;

ALTER TABLE compuser CONVERT TO CHARACTER SET utf8
  COLLATE utf8_general_ci;
ALTER TABLE compmessage CONVERT TO CHARACTER SET utf8
  COLLATE utf8_general_ci;

UPDATE compuser
SET
  login      = CONCAT('valid', CONCAT(@i := @i + 1, '@example.com')),
  password   = 'secret',
  mail       = CONCAT('valid', CONCAT(@i, '@example.com')),
  type       = 'teilnehmer',
  changedate = NOW(),
  createdate = NOW(),
  pseudonym  = CONCAT('marvin', @i),
  vorname    = CONCAT('Bob', @i),
  nachname   = 'Baumeister';

SELECT no FROM compuser WHERE mail = 'valid1@example.com' INTO @user_legacy;

SELECT teilnehmer FROM compmessage
  WHERE teilnehmer IS NOT NULL
  GROUP BY teilnehmer
  HAVING COUNT(teilnehmer) > 10
  LIMIT 1 INTO @teilnehmer;

SELECT recipient FROM compmessage
  WHERE recipient IS NOT NULL AND recipient != @teilnehmer
  GROUP BY recipient
  HAVING COUNT(recipient) > 10
  LIMIT 1 INTO @recipient;

SELECT @user_legacy, @teilnehmer, @recipient;

SELECT type, COUNT(*) FROM compuser GROUP BY type;

SELECT teilnahmeart, COUNT(*) FROM compuser GROUP BY teilnahmeart;

SELECT participant_state, COUNT(*) FROM compuser GROUP BY participant_state;

SELECT COUNT(*) AS messages_for_user
FROM compmessage old_msg
  JOIN compuser u
    ON old_msg.teilnehmer = u.no OR old_msg.recipient = u.no COLLATE utf8_general_ci;

SELECT
  cu.login,
  count(cu.login),
  ' recipients '
FROM compmessage AS cm
  JOIN compuser AS cu ON cm.recipient = cu.no
GROUP BY cu.login;

SELECT
  cu.login,
  count(cu.login),
  ' senders '
FROM compmessage AS cm
  JOIN compuser AS cu ON cm.sender = cu.no
GROUP BY cu.login;

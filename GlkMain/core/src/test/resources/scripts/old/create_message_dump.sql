DELETE FROM gk_small.compmessage;

INSERT INTO gk_small.compmessage
  SELECT *
  FROM gk_live.compmessage
  WHERE teilnehmer = '67080c6ba8' OR recipient = '67080c6ba8'
                                     AND no NOT IN (SELECT no
                                                    FROM gk_small.compmessage)
  GROUP BY no;

INSERT INTO gk_small.compmessage
  SELECT m2.*
  FROM gk_small.compmessage AS m1 JOIN gk_live.compmessage AS m2
      ON m1.reply_message = m2.no
  WHERE m1.no NOT IN (SELECT no
                      FROM gk_small.compmessage)
  GROUP BY m2.no;

INSERT INTO gk_small.compmessage
  SELECT m2.*
  FROM gk_small.compmessage AS m1 JOIN gk_live.compmessage AS m2
      ON m1.replied_message = m2.no
  WHERE m1.no NOT IN (SELECT no
                      FROM gk_small.compmessage)
  GROUP BY m2.no;

INSERT INTO gk_small.compuser
  SELECT u1.*
  FROM gk_small.compmessage AS m1 JOIN gk_live.compuser AS u1
      ON m1.teilnehmer = u1.no OR m1.recipient = u1.no
  WHERE m1.no NOT IN (SELECT no
                      FROM gk_small.compmessage)
  GROUP BY u1.no;

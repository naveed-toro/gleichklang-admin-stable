SET FOREIGN_KEY_CHECKS  = 0;
DELETE FROM envelope;
DELETE FROM message;
SET FOREIGN_KEY_CHECKS  = 1;
/* migrate messages */
CALL ADD_COLUMN('message', 'deleted', 'BIT NOT NULL DEFAULT 0');

CALL CREATE_UNIQUE_INDEX('message_legacy_id', 'message', 'legacy_id');

INSERT INTO message (legacy_id, change_date, create_date, send_date, body, subject, sent, deleted)
  SELECT
    no,
    changedate,
    createdate,
    sentdate,
    nachrichtentext,
    '',
    kontakt_visiblestatesender != 'draft',
    0
  FROM compmessage
  WHERE type = 'kontakt' AND nachrichtentext IS NOT NULL;

/* set replied_message_id */
UPDATE message dest INNER JOIN compmessage src
    ON src.no = dest.legacy_id
  INNER JOIN message new_id
    ON src.replied_message = new_id.legacy_id
SET dest.reply_to_message_id = new_id.id;

/* migrate sender */
INSERT INTO envelope (DTYPE, legacy_id, change_date, create_date, user_id, deleted, read_, message_id)
  SELECT
    'SenderEnvelope',
    new_msg.legacy_id,
    new_msg.change_date,
    new_msg.create_date,
    usr.id,
    old_msg.kontakt_visiblestatesender = 'deleted',
    NULL,
    new_msg.id
  FROM message new_msg INNER JOIN compmessage old_msg
      ON new_msg.legacy_id = old_msg.no
    INNER JOIN user_ usr
      ON usr.legacy_id = old_msg.teilnehmer;

INSERT INTO envelope (DTYPE, legacy_id, change_date, create_date, user_id, deleted, read_, message_id)
  SELECT
    'ReceiverEnvelope',
    new_msg.legacy_id,
    new_msg.change_date,
    new_msg.create_date,
    usr.id,
    old_msg.kontakt_visiblestaterecipient = 'deleted',
    old_msg.kontakt_activitystate = 'open',
    new_msg.id
  FROM message new_msg INNER JOIN compmessage old_msg
      ON new_msg.legacy_id = old_msg.no
    INNER JOIN user_ usr
      ON usr.legacy_id = old_msg.recipient;
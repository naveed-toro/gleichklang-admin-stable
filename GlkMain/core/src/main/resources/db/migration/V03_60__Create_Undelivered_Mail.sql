CREATE TABLE IF NOT EXISTS undeliverable_mail
(
 id                        BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 legacy_id                 VARCHAR(255),
 change_date               DATETIME,
 create_date               DATETIME,

 undeliverable_mail_reason VARCHAR(255) NOT NULL,
 recipient_email           VARCHAR(255) NOT NULL
);

DELETE FROM undeliverable_mail;

INSERT INTO undeliverable_mail(legacy_id, change_date, create_date, undeliverable_mail_reason, recipient_email)
  SELECT
    m.no         AS legacy_id,
    m.changedate AS change_date,
    m.createdate AS create_date,

    CASE m.undeliveryreason
    WHEN 'MailboxFull'
      THEN 'MAILBOX_FULL'
    WHEN 'SpamFilter'
      THEN 'SPAM_FILTER'
    WHEN 'RelayAccessDenied'
      THEN 'RELAY_ACCESS_DENIED'
    WHEN 'UnrouteableAddress'
      THEN 'UNROUTABLE_ADDRESS'
    WHEN 'InvalidAddress'
      THEN 'INVALID_ADDRESS'
    WHEN 'DomainNotFound'
      THEN 'DOMAIN_NOT_FOUND'
    WHEN 'unknown'
      THEN 'UNKNOWN'
    ELSE 'NONE'
    END
                 AS undeliverable_mail_reason,
    m.mail       AS recipient_email
  FROM compundeliveredmail m
  WHERE
    m.mail IS NOT NULL;

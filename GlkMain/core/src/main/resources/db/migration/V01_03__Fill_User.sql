SET FOREIGN_KEY_CHECKS = 0;

DELETE FROM address;
DELETE FROM user_settings;
DELETE FROM user_;

CALL DROP_INDEX('user_email', 'user_');

INSERT INTO user_ (legacy_id, create_date, alias, birthDate, confirmationCode, email, firstName, lastName, memberStatus, newMail, password, sex, statusMessage)
  SELECT
    no               AS legacy_id,
    createdate       AS create_date,
    pseudonym        AS alias,
    geburtstag       AS birthDate,
    confirmationcode AS confirmationCode,
    login            AS email,
    vorname          AS firstName,
    nachname         AS lastName,
    type             AS memberStatus,
    new_mail         AS newMail,
    password         AS password,
    geschlecht       AS sex,
    NULL             AS statusMessage
  FROM compuser AS c
  WHERE (c.type = 'teilnehmer' AND c.login IS NOT NULL AND c.pseudonym IS NOT NULL) OR c.type = 'exteilnehmer';

DELETE FROM user_ WHERE id IN (SELECT u_id FROM (SELECT u1.id AS u_id FROM user_ u1 WHERE memberStatus = 'exteilnehmer' AND EXISTS(SELECT 1 FROM user_ u2 where u2.email = u1.email AND u2.memberStatus = 'teilnehmer')) AS double_entries);

CALL CREATE_UNIQUE_INDEX('user_email', 'user_', 'email');

INSERT INTO address (legacy_id, create_date, city, country, streetWithNumber, zip, user_id)
  SELECT
    no                              AS legacy_id,
    createdate                      AS create_date,
    ort                             AS city,
    land                            AS country,
    CONCAT_WS(" ", strasse, hausnr) AS streetWithNumber,
    plz                             AS zip,
    u.id                            AS user_id
  FROM compuser AS cu JOIN user_ AS u
      ON cu.no = u.legacy_id;

INSERT INTO user_settings (legacy_id, create_date, cancellationPolicyAccepted, communityRulesAccepted, disableAds, disableFurtherInfo, generalTermsAccepted, mailBlocked, matchmakingSuccess, privacyPolicyAccepted, satisfactionGleichklang, satisfactionOther, sourcePage, sourceText, talkToPress, user_id)
  SELECT
    no                            AS legacy_id,
    createdate                    AS create_date,
    widerrufsbelehrung_akzeptiert AS cancellationPolicyAccepted,
    comunityregeln_akzeptiert     AS communityRulesAccepted,
    keine_werbung                 AS disableAds,
    supress_infomails             AS disableFurtherInfo,
    agb_akzeptiert                AS generalTermsAccepted,
    mailblocked                   AS mailBlocked,
    matching_success              AS matchmakingSuccess,
    datenschutz_akzeptiert        AS privacyPolicyAccepted,
    user_satisfaction             AS satisfactionGleichklang,
    user_satisfaction_other       AS satisfactionOther,
    quellen                       AS sourcePage,
    quellefrei                    AS sourceText,
    pressinfo                     AS talkToPress,
    u.id                          AS user_id

  FROM compuser AS cu JOIN user_ AS u
      ON cu.no = u.legacy_id;

CALL ADD_FOREIGN_KEY('user_settings', 'id', 'user_', 'id');

SET FOREIGN_KEY_CHECKS = 1;

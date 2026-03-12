/**
compuser
 */

UPDATE compuser
SET mail = LOWER(CONCAT(RIGHT(password(mail), 10), '@example.com'))
WHERE mail IS NOT NULL AND mail != '';

UPDATE compuser
SET password = LOWER(RIGHT(password(password), 10))
WHERE password IS NOT NULL AND password != '';

UPDATE compuser
SET login = LOWER(CONCAT(RIGHT(password(login), 10), '@example.com'))
WHERE login IS NOT NULL AND login != '';

UPDATE compuser
SET vorname = LOWER(RIGHT(password(vorname), 4))
WHERE vorname IS NOT NULL AND vorname != '';

UPDATE compuser
SET nachname = LOWER(RIGHT(password(nachname), 4))
WHERE nachname IS NOT NULL AND nachname != '';

UPDATE compuser
SET pseudonym = RIGHT(PASSWORD(pseudonym), 10)
WHERE pseudonym IS NOT NULL AND pseudonym != '';

UPDATE compuser
SET telefon = RIGHT(PASSWORD(telefon), 8)
WHERE telefon IS NOT NULL AND telefon != '';

UPDATE compuser
SET strasse = RIGHT(PASSWORD(strasse), 8)
WHERE strasse IS NOT NULL AND strasse != '';

/**
compadportalrecipient
 */

UPDATE compadportalrecipient
SET
  portal_mail = LOWER(CONCAT(RIGHT(password(portal_mail), 10), '@example.com'))
WHERE portal_mail IS NOT NULL AND portal_mail != '';

UPDATE compadportalrecipient
SET portal_username = LOWER(RIGHT(password(portal_username), 10))
WHERE portal_username IS NOT NULL AND portal_username != '';
/**
compmessage
 */
UPDATE compmessage
SET
  recipientmail=LOWER(CONCAT(RIGHT(password(recipientmail), 10), '@example.com')),
  sendermail = LOWER(CONCAT(RIGHT(password(sendermail), 10), '@example.com')),
  recipientname=LOWER(RIGHT(password(recipientname),10)),
  sendername = LOWER(RIGHT(password(sendername), 10))
WHERE sendermail IS NOT NULL AND sendermail != ''
      AND sendermail NOT LIKE '%@example.com';

/**
compundeliveredmail
 */
UPDATE compundeliveredmail
SET mail = LOWER(CONCAT(RIGHT(password(mail), 10), '@example.com'))
WHERE mail IS NOT NULL AND mail != ''
      AND mail NOT LIKE '%@example.com';

/**
compunsolicitedmail
 */
UPDATE compunsolicitedmail
SET mail = LOWER(CONCAT(RIGHT(password(mail), 10), '@example.com'))
WHERE mail IS NOT NULL AND mail != ''
      AND mail NOT LIKE '%@example.com';

/**
compnewsletterrecipient
 */
UPDATE compnewsletterrecipient
SET mail = LOWER(CONCAT(RIGHT(password(mail), 10), '@example.com'))
WHERE mail IS NOT NULL AND mail != ''
      AND mail NOT LIKE '%@example.com';


/**
compext_gesellschaft
 */
UPDATE compext_gesellschaft
SET mail    = LOWER(CONCAT(RIGHT(password(mail), 10), '@example.com')),
  pseudonym = RIGHT(PASSWORD(pseudonym), 8)
WHERE mail IS NOT NULL AND mail != ''
      AND mail NOT LIKE '%@example.com';

/**
compext_adjektive
 */
UPDATE compext_adjektive
SET mail    = LOWER(CONCAT(RIGHT(password(mail), 10), '@example.com')),
  pseudonym = RIGHT(PASSWORD(pseudonym), 8)
WHERE mail IS NOT NULL AND mail != ''
      AND mail NOT LIKE '%@example.com';

/**
compext_persoenlichkeit
 */
UPDATE compext_persoenlichkeit
SET mail    = LOWER(CONCAT(RIGHT(password(mail), 10), '@example.com')),
  pseudonym = RIGHT(PASSWORD(pseudonym), 8)
WHERE mail IS NOT NULL AND mail != ''
      AND mail NOT LIKE '%@example.com';
/**
compext_stoerbarkeit
 */
UPDATE compext_stoerbarkeit
SET mail    = LOWER(CONCAT(RIGHT(password(mail), 10), '@example.com')),
  pseudonym = RIGHT(PASSWORD(pseudonym), 8)
WHERE mail IS NOT NULL AND mail != ''
      AND mail NOT LIKE '%@example.com';

/**
compext_partnerschaft
*/
UPDATE compext_partnerschaft
SET mail    = LOWER(CONCAT(RIGHT(password(mail), 10), '@example.com')),
  pseudonym = RIGHT(PASSWORD(pseudonym), 8)
WHERE mail IS NOT NULL AND mail != ''
      AND mail NOT LIKE '%@example.com';

/**
compproband_socio_economics
*/
UPDATE compproband_socio_economics
SET mail    = LOWER(CONCAT(RIGHT(password(mail), 10), '@example.com')),
  pseudonym = RIGHT(PASSWORD(pseudonym), 10)
WHERE mail IS NOT NULL AND mail != ''
      AND mail NOT LIKE '%@example.com';

/**
compext_kritische_lebensereignisse
*/
UPDATE compext_kritische_lebensereignisse
SET mail    = LOWER(CONCAT(RIGHT(password(mail), 10), '@example.com')),
  pseudonym = RIGHT(PASSWORD(pseudonym), 10)
WHERE mail IS NOT NULL AND mail != ''
      AND mail NOT LIKE '%@example.com';

/**
compext_hobby_e
 */
UPDATE compext_hobby_e
SET mail    = LOWER(CONCAT(RIGHT(password(mail), 10), '@example.com')),
  pseudonym = RIGHT(PASSWORD(pseudonym), 10)
WHERE mail IS NOT NULL AND mail != ''
      AND mail NOT LIKE '%@example.com';


/**
compext_freundschaft
 */
UPDATE compext_freundschaft
SET mail    = LOWER(CONCAT(RIGHT(password(mail), 10), '@example.com')),
  pseudonym = RIGHT(PASSWORD(pseudonym), 10)
WHERE mail IS NOT NULL AND mail != ''
      AND mail NOT LIKE '%@example.com';


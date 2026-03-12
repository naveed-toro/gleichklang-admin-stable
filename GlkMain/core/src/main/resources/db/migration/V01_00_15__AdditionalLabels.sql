DELETE FROM i18n WHERE legacy_id IN ('registration', 'friend.region_question', 'partner.region_question');

INSERT INTO i18n (legacy_id, create_date, language, i18n_key, i18n_value, base_name)
VALUES
  ('registration', NOW(), 'DE', 'registration', 'Mitgliederumfrage', 'QUESTIONNAIRE_NAME'),
  ('registration', NOW(), 'DE', 'registration', 'Bitte beantworten Sie noch diese Fragen', 'QUESTIONNAIRE_DESCRIPTION'),
  ('registration', NOW(), 'DE', 'source_group', 'Wie sind Sie auf uns aufmerksam geworden?', 'QUESTION_GROUP_NAME'),
  ('registration', NOW(), 'DE', 'source_group', 'Es ist für das Wachstum unserer Community und damit aller
  Mitglieder wichtig, dass wir Werbung effektiv und wirksam platzieren. Bitte seien Sie daher so freundlich und geben an, wie Sie auf uns gekommen sind. Kreuzen Sie dazu die passenden Begriffe an und bitte schreiben Sie auch in den freien Text die genauen Quellen (z.B. Titel einer Zeitung, Adresse einer Internetseite etc.) Auch im freien Text sind Mehrfachangaben möglich.', 'QUESTION_GROUP_DESCRIPTION'),
  ('registration', NOW(), 'DE', 'source', 'Aufmerksamkeit', 'QUESTION_NAME'),
  ('registration', NOW(), 'DE', 'source_text', 'Freitextangabe der Quelle', 'QUESTION_NAME'),
  ('registration', NOW(), 'DE', 'press_group', 'Interesse an Pressekontakten?', 'QUESTION_GROUP_NAME'),
  ('registration', NOW(), 'DE', 'press_group', 'Immer wieder erhalten wir Anfragen von Journalisten, ob wir Ihnen Mitglieder nennen können, die interessiert und bereit wären, gegenüber der Presse von ihren Erfahrungen mit der Partnersuche oder der Freundschaftssuche zu berichten, gegebenenfalls auch gänzlich anonym. Falls Sie hieran Interesse haben, würden wir Sie jeweils bei einer Anfrage anschreiben und fragen, ob wir Ihre Email an den Journalisten oder die Journalistin weiterleiten sollen. Bitte geben Sie an, wenn bei Ihnen ein solches Interesse besteht:', 'QUESTION_GROUP_DESCRIPTION'),
  ('registration', NOW(), 'DE', 'press', 'Ich bin interessiert, gegebenenfalls mit den Medien zu sprechen',
   'QUESTION_NAME'),

  ('friend.region_question', NOW(), 'DE', 'friend.region_question', 'Regionale Suche (Freundschaft)', 'QUESTIONNAIRE_NAME'),
  ('friend.region_question', NOW(), 'DE', 'friend.region_question', 'Bitte wählen Sie Kriterien für die regionale Suche', 'QUESTIONNAIRE_DESCRIPTION'),

  ('friend.region_question', NOW(), 'DE', 'friend.region_question', 'Regionale Suche (Freundschaft)', 'QUESTION_GROUP_NAME'),
  ('friend.region_question', NOW(), 'DE', 'friend.region_question', 'Bitte wählen Sie Kriterien für die regionale Suche', 'QUESTION_GROUP_DESCRIPTION'),

  ('friend.region_question', NOW(), 'DE', 'friend.region_question', 'Regionale Suche (Freundschaft)', 'QUESTION_NAME'),
  ('friend.region_question', NOW(), 'DE', 'friend.region_question', 'Bitte wählen Sie Kriterien für die regionale Suche', 'QUESTION_DESCRIPTION'),


  ('partner.region_question', NOW(), 'DE', 'partner.region_question', 'Regionale Suche (Partnerschaft)', 'QUESTIONNAIRE_NAME'),
  ('partner.region_question', NOW(), 'DE', 'partner.region_question', 'Bitte wählen Sie Kriterien für die regionale Suche', 'QUESTIONNAIRE_DESCRIPTION'),

  ('partner.region_question', NOW(), 'DE', 'partner.region_question', 'Regionale Suche (Partnerschaft)', 'QUESTION_GROUP_NAME'),
  ('partner.region_question', NOW(), 'DE', 'partner.region_question', 'Bitte wählen Sie Kriterien für die regionale Suche', 'QUESTION_GROUP_DESCRIPTION'),

  ('partner.region_question', NOW(), 'DE', 'partner.region_question', 'Regionale Suche (Partnerschaft)', 'QUESTION_NAME'),
  ('partner.region_question', NOW(), 'DE', 'partner.region_question', 'Bitte wählen Sie Kriterien für die regionale Suche', 'QUESTION_DESCRIPTION');

UPDATE i18n
SET i18n_key = LOWER(REPLACE(i18n_key, '.', '_'))
WHERE i18n_key LIKE 'quellen%';

UPDATE i18n
SET i18n_value = REPLACE(i18n_value, '$', '@');
UPDATE i18n
SET
  i18n_value = REPLACE(i18n_value, '@{instruktion_tastatur_skala_eingabe}', '');

DELETE FROM i18n WHERE i18n_key  = 'ALL' AND base_name IN ('CONTINENT', 'COUNTRY', 'REGION');

INSERT INTO i18n(legacy_id, change_date, create_date, language, i18n_key, i18n_value, base_name)
VALUES
  ('continents.ALL', NOW(), NOW(), 'DE', 'ALL', 'Alle Kontinente', 'CONTINENT'),
  ('countries.ALL', NOW(), NOW(), 'DE', 'ALL', 'Alle Länder', 'COUNTRY'),
  ('regions.ALL', NOW(), NOW(), 'DE', 'ALL', 'Alle Regionen', 'REGION');


DELETE l.* FROM locatable l LEFT JOIN i18n i
    ON l.i18n_key = i.i18n_key AND i.language = 'DE'
WHERE DTYPE IN ('Continent', 'Country', 'Region') AND l.i18n_key IS NULL;
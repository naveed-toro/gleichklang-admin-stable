SET @default_legacy = _utf8'V03_16__BDSM_Migration' COLLATE utf8_unicode_ci;

# Idempotence
DELETE ac FROM activator_choice AS ac
  JOIN activator AS a ON ac.activator_id = a.id
WHERE a.legacy_id = @default_legacy;
DELETE a FROM activator AS a
WHERE a.legacy_id = @default_legacy;
DELETE mmv FROM matching_matrix_value AS mmv
  JOIN matching_matrix AS mm ON mmv.matrix_id = mm.id
  JOIN choice_group AS cg
    ON mm.source_choice_group_id = cg.id OR mm.target_choice_group_id = cg.id
WHERE cg.legacy_id = @default_legacy;

DELETE mm FROM matching_matrix mm
  JOIN choice_group AS cg
    ON mm.source_choice_group_id = cg.id OR mm.target_choice_group_id = cg.id
WHERE cg.legacy_id = @default_legacy;

DELETE ca FROM choice_answer ca
  JOIN choice AS c ON ca.choice_id = c.id
WHERE c.legacy_id = @default_legacy;
DELETE ca FROM choice_answer AS ca
  JOIN answer AS a ON ca.answer_id = a.id
  JOIN question AS q ON a.question_id = q.id
WHERE q.legacy_id = @default_legacy OR
      q.i18n_key IN
      ('partner.p_bdsm_suche_allg', 'freund.f_bdsm_suche_allg', 'partner.p_bdsm_suche_spezial', 'freund.f_bdsm_suche_spezial');
DELETE a FROM answer a
  JOIN question AS q ON a.question_id = q.id
WHERE q.legacy_id = @default_legacy OR
      q.i18n_key IN
      ('partner.p_bdsm_suche_allg', 'freund.f_bdsm_suche_allg', 'partner.p_bdsm_suche_spezial', 'freund.f_bdsm_suche_spezial');

DELETE FROM choice
WHERE legacy_id = @default_legacy;
UPDATE question AS q
  JOIN choice_group AS cg ON q.choice_group_id = cg.id
SET q.choice_group_id = 1
WHERE cg.legacy_id = @default_legacy;
DELETE FROM choice_group
WHERE legacy_id = @default_legacy;
DELETE FROM i18n
WHERE legacy_id = @default_legacy;
DELETE FROM question
WHERE legacy_id = @default_legacy;

# update choice group names
UPDATE choice_group
SET name = 'Spezial-Ausschließlich (bisexuell)'
WHERE name = 'Spezial-Ausschließlich' AND legacy_name LIKE '%bisexuell;';

UPDATE choice_group
SET name = 'Spezial-Ausschließlich (transsexuell)'
WHERE name = 'Spezial-Ausschließlich' AND legacy_name LIKE '%transsexuell;';

UPDATE choice_group
SET name = 'Spezial-Ausschließlich (intersexuell)'
WHERE name = 'Spezial-Ausschließlich' AND legacy_name LIKE '%intersexuell;';

UPDATE choice_group
SET name = 'Spezial-Ausschließlich (crossdresser)'
WHERE name = 'Spezial-Ausschließlich' AND legacy_name LIKE '%crossdresser;';

UPDATE choice_group
SET name = 'Spezial-Ausschließlich (mehrfachbeziehung)'
WHERE name = 'Spezial-Ausschließlich' AND legacy_name LIKE '%fachbeziehung;';

# new BDSM migration
UPDATE question
SET deleted = TRUE
WHERE i18n_key LIKE '%bdsm_suche' OR i18n_key LIKE '%bdsm_mitteil';
UPDATE choice_group
SET deleted = TRUE
WHERE name LIKE '%bdsm%';

# invariants
SET @partner_allg_name_old = _utf8'partner.p_bdsm_suche_allg' COLLATE
                             utf8_unicode_ci;

SET @freund_allg_name_old = _utf8'freund.f_bdsm_suche_allg' COLLATE
                            utf8_unicode_ci;

SET @partner_spez_name_old = _utf8'partner.p_bdsm_suche_spezial' COLLATE
                             utf8_unicode_ci;

SET @freund_spez_name_old = _utf8'freund.f_bdsm_suche_spezial' COLLATE
                            utf8_unicode_ci;


SET @partner_allg_name = _utf8'partner.p_bdsm_suche_allg_new_' COLLATE
                         utf8_unicode_ci;

SET @freund_allg_name = _utf8'freund.f_bdsm_suche_allg_new_' COLLATE
                        utf8_unicode_ci;

SET @partner_spez_name = _utf8'partner.p_bdsm_suche_spezial_new_' COLLATE
                         utf8_unicode_ci;

SET @freund_spez_name = _utf8'freund.f_bdsm_suche_spezial_new_' COLLATE
                        utf8_unicode_ci;

SELECT id
INTO @question_group_partner_id
FROM question_group
WHERE i18n_key = 'p_erotikundsex';

SELECT id
INTO @question_group_freund_id
FROM question_group
WHERE i18n_key = 'f_erotikundsex';

# partner allg

INSERT INTO choice_group (legacy_id, legacy_name, name, deleted)
VALUES (@default_legacy, @partner_allg_name, 'BDSM-Initial (Partner)', FALSE);

SELECT id
INTO @partner_allg_choice_group_id
FROM choice_group
WHERE legacy_name = @partner_allg_name;

SET @idx := 0;

INSERT INTO choice (legacy_id, sort_order, i18n_key, choice, choice_group_id)
VALUES
  (@default_legacy, @idx := @idx + 1,
   CONCAT(@partner_allg_name, @idx),
   CONCAT(@partner_allg_name, @idx), @partner_allg_choice_group_id),
  (@default_legacy, @idx := @idx + 1,
   CONCAT(@partner_allg_name, @idx),
   CONCAT(@partner_allg_name, @idx), @partner_allg_choice_group_id),
  (@default_legacy, @idx := @idx + 1,
   CONCAT(@partner_allg_name, @idx),
   CONCAT(@partner_allg_name, @idx), @partner_allg_choice_group_id),
  (@default_legacy, @idx := @idx + 1,
   CONCAT(@partner_allg_name, @idx),
   CONCAT(@partner_allg_name, @idx), @partner_allg_choice_group_id);

SET @idx := 0;
INSERT INTO i18n (legacy_id, language, i18n_key, i18n_value, base_name) VALUES
  (@default_legacy, 'DE',
   CONCAT(@partner_allg_name, @idx := @idx + 1),
   'Nein, auf keinen Fall', 'CHOICE_VALUE'),
  (@default_legacy, 'DE',
   CONCAT(@partner_allg_name, @idx := @idx + 1),
   'Nein, aber ich akzeptiere es, wenn mein(e) Partner(in) BDSM-Präferenzen hat, sofern er/sie auch darauf verzichten kann',
   'CHOICE_VALUE'),
  (@default_legacy, 'DE',
   CONCAT(@partner_allg_name, @idx := @idx + 1),
   'Ja, auch', 'CHOICE_VALUE'),
  (@default_legacy, 'DE',
   CONCAT(@partner_allg_name, @idx := @idx + 1),
   'Ja, nur', 'CHOICE_VALUE');


SELECT IF(MAX(q2.sort_order) IS NULL, 0, MAX(q2.sort_order) + 1)
INTO @partner_sort
FROM question AS q2
WHERE q2.question_group_id = @question_group_partner_id;

INSERT INTO question (DTYPE, legacy_id, required, selection_type, i18n_key, choice_group_id, question_group_id, deleted, sort_order, representation_type, adjustable_relationship_visibility, only_admin_visible)
VALUES
  ('ChoiceQuestion', @default_legacy, TRUE, 'SINGLE', @partner_allg_name,
                     @partner_allg_choice_group_id, @question_group_partner_id,
                     FALSE,
                     @partner_sort, 'DEFAULT', TRUE, FALSE);

UPDATE i18n AS i
SET
  i18n_value = 'Ich bin an einem Partner/ einer Partnerin interessiert, der/die BDSM-Interessen hat (Spielerische Formen von Dominanz und Unterwerfung)',
  i18n_key   = @partner_allg_name
WHERE i18n_key = 'partner.p_bdsm_suche_allg' AND base_name = 'QUESTION_NAME';

# freund allg
INSERT INTO choice_group (legacy_id, legacy_name, name, deleted)
VALUES (@default_legacy, @freund_allg_name, 'BDSM-Initial (Freund)', FALSE);

SELECT id
INTO @freund_allg_choice_group_id
FROM choice_group
WHERE legacy_name = @freund_allg_name;

SET @idx := 0;

INSERT INTO choice (legacy_id, sort_order, i18n_key, choice, choice_group_id)
VALUES
  (@default_legacy, @idx := @idx + 1,
   CONCAT(@freund_allg_name, @idx),
   CONCAT(@freund_allg_name, @idx), @freund_allg_choice_group_id),
  (@default_legacy, @idx := @idx + 1,
   CONCAT(@freund_allg_name, @idx),
   CONCAT(@freund_allg_name, @idx), @freund_allg_choice_group_id),
  (@default_legacy, @idx := @idx + 1,
   CONCAT(@freund_allg_name, @idx),
   CONCAT(@freund_allg_name, @idx), @freund_allg_choice_group_id),
  (@default_legacy, @idx := @idx + 1,
   CONCAT(@freund_allg_name, @idx),
   CONCAT(@freund_allg_name, @idx), @freund_allg_choice_group_id);

SET @idx := 0;
INSERT INTO i18n (legacy_id, language, i18n_key, i18n_value, base_name) VALUES
  (@default_legacy, 'DE',
   CONCAT(@freund_allg_name, @idx := @idx + 1),
   'Nein, auf keinen Fall', 'CHOICE_VALUE'),
  (@default_legacy, 'DE',
   CONCAT(@freund_allg_name, @idx := @idx + 1),
   'Nein, aber ich akzeptiere es, wenn mein(e) Freund(in) BDSM-Präferenzen hat, sofern er/sie auch darauf verzichten kann',
   'CHOICE_VALUE'),
  (@default_legacy, 'DE',
   CONCAT(@freund_allg_name, @idx := @idx + 1),
   'Ja, auch', 'CHOICE_VALUE'),
  (@default_legacy, 'DE',
   CONCAT(@freund_allg_name, @idx := @idx + 1),
   'Ja, nur', 'CHOICE_VALUE');


SELECT IF(MAX(q2.sort_order) IS NULL, 0, MAX(q2.sort_order) + 1)
INTO @freund_sort
FROM question AS q2
WHERE q2.question_group_id = @question_group_freund_id;

INSERT INTO question (DTYPE, legacy_id, required, selection_type, i18n_key, choice_group_id, question_group_id, deleted, sort_order, representation_type, adjustable_relationship_visibility, only_admin_visible)
VALUES
  ('ChoiceQuestion', @default_legacy, TRUE, 'SINGLE', @freund_allg_name,
                     @freund_allg_choice_group_id, @question_group_freund_id,
                     FALSE,
                     @freund_sort, 'DEFAULT', TRUE, FALSE);

UPDATE i18n AS i
SET
  i18n_value = 'Ich bin an einem Freund/ einer Freundin interessiert, der/die BDSM-Interessen hat (Spielerische Formen von Dominanz und Unterwerfung)',
  i18n_key   = @freund_allg_name
WHERE i18n_key = 'freund.f_bdsm_suche_allg' AND base_name = 'QUESTION_NAME';

# partner spezial

INSERT INTO choice_group (legacy_id, legacy_name, name, deleted)
VALUES (@default_legacy, @partner_spez_name, 'BDSM-Spezial (Partner)', FALSE);

SELECT id
INTO @partner_spez_choice_group_id
FROM choice_group
WHERE legacy_name = @partner_spez_name;

SET @idx := 0;

INSERT INTO choice (legacy_id, sort_order, i18n_key, choice, choice_group_id)
VALUES
  (@default_legacy, @idx := @idx + 1,
   CONCAT(@partner_spez_name, @idx),
   CONCAT(@partner_spez_name, @idx), @partner_spez_choice_group_id),
  (@default_legacy, @idx := @idx + 1,
   CONCAT(@partner_spez_name, @idx),
   CONCAT(@partner_spez_name, @idx), @partner_spez_choice_group_id),
  (@default_legacy, @idx := @idx + 1,
   CONCAT(@partner_spez_name, @idx),
   CONCAT(@partner_spez_name, @idx), @partner_spez_choice_group_id),
  (@default_legacy, @idx := @idx + 1,
   CONCAT(@partner_spez_name, @idx),
   CONCAT(@partner_spez_name, @idx), @partner_spez_choice_group_id);

SET @idx := 0;
INSERT INTO i18n (legacy_id, language, i18n_key, i18n_value, base_name) VALUES
  (@default_legacy, 'DE',
   CONCAT(@partner_spez_name, @idx := @idx + 1),
   'Partner/in sollte devote Rolle einnehmen', 'CHOICE_VALUE'),
  (@default_legacy, 'DE',
   CONCAT(@partner_spez_name, @idx := @idx + 1),
   'Partner/in sollte dominante Rolle einnehmen', 'CHOICE_VALUE'),
  (@default_legacy, 'DE',
   CONCAT(@partner_spez_name, @idx := @idx + 1),
   'Partner/in sollte devote oder dominante Rolle einnehmen', 'CHOICE_VALUE'),
  (@default_legacy, 'DE',
   CONCAT(@partner_spez_name, @idx := @idx + 1),
   'Partner/in sollte devote oder dominante Rolle im Wechsel einnehmen',
   'CHOICE_VALUE');


SELECT IF(MAX(q2.sort_order) IS NULL, 0, MAX(q2.sort_order) + 1)
INTO @partner_sort
FROM question AS q2
WHERE q2.question_group_id = @question_group_partner_id;

INSERT INTO question (DTYPE, legacy_id, required, selection_type, i18n_key, choice_group_id, question_group_id, deleted, sort_order, representation_type, adjustable_relationship_visibility, only_admin_visible)
VALUES
  ('ChoiceQuestion', @default_legacy, FALSE, 'SINGLE', @partner_spez_name,
                     @partner_spez_choice_group_id, @question_group_partner_id,
                     FALSE,
                     @partner_sort, 'DEFAULT', TRUE, FALSE);

UPDATE i18n AS i
SET
  i18n_value = 'Welche speziellen Vorlieben in Bezug auf BDSM sind gewünscht?',
  i18n_key   = @partner_spez_name
WHERE i18n_key = 'partner.p_bdsm_suche_spezial' AND base_name = 'QUESTION_NAME';

# freund spezial

INSERT INTO choice_group (legacy_id, legacy_name, name, deleted)
VALUES (@default_legacy, @freund_spez_name, 'BDSM-Spezial (Freund)', FALSE);

SELECT id
INTO @freund_spez_choice_group_id
FROM choice_group
WHERE legacy_name = @freund_spez_name;

SET @idx := 0;

INSERT INTO choice (legacy_id, sort_order, i18n_key, choice, choice_group_id)
VALUES
  (@default_legacy, @idx := @idx + 1,
   CONCAT(@freund_spez_name, @idx),
   CONCAT(@freund_spez_name, @idx), @freund_spez_choice_group_id),
  (@default_legacy, @idx := @idx + 1,
   CONCAT(@freund_spez_name, @idx),
   CONCAT(@freund_spez_name, @idx), @freund_spez_choice_group_id),
  (@default_legacy, @idx := @idx + 1,
   CONCAT(@freund_spez_name, @idx),
   CONCAT(@freund_spez_name, @idx), @freund_spez_choice_group_id),
  (@default_legacy, @idx := @idx + 1,
   CONCAT(@freund_spez_name, @idx),
   CONCAT(@freund_spez_name, @idx), @freund_spez_choice_group_id);

SET @idx := 0;
INSERT INTO i18n (legacy_id, language, i18n_key, i18n_value, base_name) VALUES
  (@default_legacy, 'DE',
   CONCAT(@freund_spez_name, @idx := @idx + 1),
   'Freund/in sollte devote Rolle einnehmen', 'CHOICE_VALUE'),
  (@default_legacy, 'DE',
   CONCAT(@freund_spez_name, @idx := @idx + 1),
   'Freund/in sollte dominante Rolle einnehmen', 'CHOICE_VALUE'),
  (@default_legacy, 'DE',
   CONCAT(@freund_spez_name, @idx := @idx + 1),
   'Freund/in sollte devote oder dominante Rolle einnehmen', 'CHOICE_VALUE'),
  (@default_legacy, 'DE',
   CONCAT(@freund_spez_name, @idx := @idx + 1),
   'Freund/in sollte devote oder dominante Rolle im Wechsel einnehmen',
   'CHOICE_VALUE');

SELECT IF(MAX(q2.sort_order) IS NULL, 0, MAX(q2.sort_order) + 1)
INTO @freund_sort
FROM question AS q2
WHERE q2.question_group_id = @question_group_partner_id;

INSERT INTO question (DTYPE, legacy_id, required, selection_type, i18n_key, choice_group_id, question_group_id, deleted, sort_order, representation_type, adjustable_relationship_visibility, only_admin_visible)
VALUES
  ('ChoiceQuestion', @default_legacy, FALSE, 'SINGLE', @freund_spez_name,
                     @freund_spez_choice_group_id, @question_group_freund_id,
                     FALSE,
                     @freund_sort, 'DEFAULT', TRUE, FALSE);

UPDATE i18n AS i
SET
  i18n_value = 'Welche speziellen Vorlieben in Bezug auf BDSM sind gewünscht?',
  i18n_key   = @freund_spez_name
WHERE i18n_key = 'freund.f_bdsm_suche_spezial' AND base_name = 'QUESTION_NAME';


SELECT id
INTO @partner_allg_question_id
FROM question
WHERE i18n_key = @partner_allg_name;
SELECT id
INTO @freund_allg_question_id
FROM question
WHERE i18n_key = @freund_allg_name;
SELECT id
INTO @partner_spez_question_id
FROM question
WHERE i18n_key = @partner_spez_name;
SELECT id
INTO @freund_spez_question_id
FROM question
WHERE i18n_key = @freund_spez_name;

# activator
INSERT INTO activator (DTYPE, legacy_id, activating_question_id, enables_question_id)
VALUES
  ('QuestionActivator', @default_legacy, @partner_allg_question_id,
   @partner_spez_question_id),
  ('QuestionActivator', @default_legacy, @freund_allg_question_id,
   @freund_spez_question_id);

SELECT id
INTO @partner_activator_id
FROM activator
WHERE activating_question_id = @partner_allg_question_id AND
      enables_question_id = @partner_spez_question_id;
SELECT id
INTO @freund_activator_id
FROM activator
WHERE activating_question_id = @freund_allg_question_id AND
      enables_question_id = @freund_spez_question_id;
INSERT INTO activator_choice (activator_id, choice_id)
  SELECT
    @partner_activator_id,
    c.id
  FROM choice AS c
  WHERE c.choice_group_id = @partner_allg_choice_group_id AND c.sort_order > 2;
INSERT INTO activator_choice (activator_id, choice_id)
  SELECT
    @freund_activator_id,
    c.id
  FROM choice AS c
  WHERE c.choice_group_id = @freund_allg_choice_group_id AND c.sort_order > 2;

SELECT id
INTO @partner_old_question_id
FROM question
WHERE i18n_key = @partner_bdsm_suche_old;
SELECT id
INTO @freund_old_question_id
FROM question
WHERE i18n_key = @freund_bdsm_suche_old;



SELECT id
INTO @partner_question_nur_id
FROM question
WHERE i18n_key = 'partner.p_bdsm_nur';
SELECT id
INTO @freund_question_nur_id
FROM question
WHERE i18n_key = 'freund.f_bdsm_nur';


SELECT IF(MAX(q.sort_order) IS NULL, 0, MAX(q.sort_order) + 1)
INTO @max_partner_question_group
FROM question AS q
WHERE q.question_group_id = @question_group_partner_id;
SELECT IF(MAX(q.sort_order) IS NULL, 0, MAX(q.sort_order) + 1)
INTO @max_freund_question_group
FROM question AS q
WHERE q.question_group_id = @question_group_freund_id;
UPDATE question q
SET sort_order = @max_partner_question_group
WHERE q.id = @partner_question_nur_id;
UPDATE question q
SET sort_order = @max_freund_question_group
WHERE q.id = @freund_question_nur_id;

INSERT INTO activator (DTYPE, legacy_id, activating_question_id, enables_question_id)
VALUES
  ('QuestionActivator', @default_legacy, @partner_allg_question_id,
   @partner_question_nur_id),
  ('QuestionActivator', @default_legacy, @freund_allg_question_id,
   @freund_question_nur_id);

SELECT id
INTO @partner_activator_id
FROM activator
WHERE activating_question_id = @partner_allg_question_id AND
      enables_question_id = @partner_question_nur_id;
SELECT id
INTO @freund_activator_id
FROM activator
WHERE activating_question_id = @freund_allg_question_id AND
      enables_question_id = @freund_question_nur_id;
INSERT INTO activator_choice (activator_id, choice_id)
  SELECT
    @partner_activator_id,
    c.id
  FROM choice AS c
  WHERE c.choice_group_id = @partner_allg_choice_group_id AND c.sort_order > 2;
INSERT INTO activator_choice (activator_id, choice_id)
  SELECT
    @freund_activator_id,
    c.id
  FROM choice AS c
  WHERE c.choice_group_id = @freund_allg_choice_group_id AND c.sort_order > 2;



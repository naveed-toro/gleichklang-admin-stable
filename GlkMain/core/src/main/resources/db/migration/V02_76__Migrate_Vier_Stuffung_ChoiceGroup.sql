DROP TABLE IF EXISTS tmp_choice_migration;
CREATE TEMPORARY TABLE tmp_choice_migration AS
  SELECT
    c1.choice AS old_choice,
    c1.id     AS old_choice_id,
    c2.choice AS new_choice,
    c2.id     AS new_choice_id
  FROM choice c1
    JOIN choice_group cg1
      ON c1.choice_group_id = cg1.id AND cg1.name = 'Vier-Stufung (ja/nein)'
    JOIN choice c2
    JOIN choice_group cg2
      ON c2.choice_group_id = cg2.id AND cg2.name = 'Standard-Typus'
         AND c2.choice = CASE c1.choice
                         WHEN '001'
                           THEN '001'
                         WHEN '002'
                           THEN '002'
                         WHEN '003'
                           THEN '004'
                         WHEN '004'
                           THEN
                             '005' END;

UPDATE choice_answer AS ca
  JOIN tmp_choice_migration AS tcm
    ON ca.choice_id = tcm.old_choice_id
SET ca.choice_id = tcm.new_choice_id;

UPDATE question AS q
  JOIN choice_group AS cg
    ON q.choice_group_id = cg.id AND cg.name = 'Vier-Stufung (ja/nein)'
SET q.choice_group_id = (
  SELECT id
  FROM choice_group
  WHERE name = 'Standard-Typus');

DELETE c FROM choice AS c
  JOIN choice_group AS cg
    ON c.choice_group_id = cg.id
WHERE cg.name = 'Vier-Stufung (ja/nein)';
DELETE FROM choice_group
WHERE name = 'Vier-Stufung (ja/nein)';

DELETE qm FROM questions_mapping AS qm
  JOIN matching_matrix AS mm
    ON qm.matrix_id = mm.id
  JOIN choice_group AS cg
    ON mm.source_choice_group_id = cg.id OR mm.target_choice_group_id = cg.id
WHERE cg.name = 'Vier-Stufung (ja/nein)';

DELETE mm FROM matching_matrix AS mm
  JOIN choice_group AS cg
    ON mm.source_choice_group_id = cg.id OR mm.target_choice_group_id = cg.id
WHERE cg.name = 'Vier-Stufung (ja/nein)';


UPDATE choice_group AS cg
  JOIN choice AS c
    ON cg.id = c.choice_group_id
  JOIN i18n AS i
    ON i.i18n_key = c.i18n_key AND i.language = 'DE'
       AND cg.name = 'Standard-Typus'
SET i.i18n_value = CASE i.i18n_key
                   WHEN 'auf_keinen_fall'
                     THEN 'Nein'
                   WHEN 'auf_jeden_fall'
                     THEN 'Ja'
                   ELSE i
                   .i18n_value END;
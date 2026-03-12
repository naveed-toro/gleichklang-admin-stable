UPDATE answer AS a
  SET a.DTYPE = 'PartnerRegionAnswer' WHERE question_id = (SELECT id FROM question
WHERE i18n_key = 'partner.region_question');

DROP TABLE IF EXISTS partner_region;
CREATE TEMPORARY TABLE partner_region
SELECT *
FROM
  (
    SELECT
      id as answer_id,
      DTYPE as DTYPE,
      question_id,
      user_id AS user_id
    FROM answer
    WHERE question_id = (SELECT id FROM question WHERE i18n_key = 'partner.region_question')) AS ra
  JOIN
  (
    SELECT
      user_id AS user_id2,
      relocatable,
      search_relocatable
    FROM answer
    WHERE DTYPE = 'PartnerRegionAnswer') AS pra
    ON ra.user_id = pra.user_id2;

UPDATE partner_region as pr JOIN answer AS a ON pr.answer_id = a.id
SET a.relocatable = pr.relocatable,
a.search_relocatable = pr.search_relocatable,
a.DTYPE = 'PartnerRegionAnswer';

UPDATE answer AS a JOIN user_ AS u ON a.user_id = u.id JOIN comppartner cu
    ON u.legacy_id = cu.owner
SET a.relocatable = CASE WHEN cu.umzug IN ('004', '005') THEN TRUE ELSE FALSE END
WHERE a.DTYPE = 'PartnerRegionAnswer';




DROP TABLE IF EXISTS answer_region_temp, sex_search, question_choice_group_name, migration_fixed_data, migration_locatable, migration_user_with_regions;


CREATE TABLE IF NOT EXISTS address_lookup (
city         VARCHAR(255),
country_code VARCHAR(2)
);
CALL ADD_COLUMN('address', 'unchecked', 'BIT(1) DEFAULT NULL');

DELETE a FROM address AS a where unchecked = true;

INSERT INTO address(legacy_id, create_date, city, streetWithNumber, user_id, continent_id, country_id, tmp_zip, payment, unchecked)
    SELECT
      no                              AS legacy_id,
      createdate                      AS create_date,
      ort                             AS city,
      CONCAT_WS(' ', strasse, hausnr) AS streetWithNumber,
      u.id                            AS user_id,
      l.parent_id                     AS continent_id,
      l.id                            AS country_id,
      plz                             AS tmp_zip,
      TRUE AS payment,
      TRUE                            AS unchecked
    FROM compuser AS cu
      JOIN user_ AS u
        ON cu.no = u.legacy_id
      JOIN address_lookup AS al ON al.city = cu.ort
      JOIN locatable AS l ON al.country_code = l.i18n_key AND l.DTYPE = 'Country'
    WHERE land NOT IN ('DE', 'AT', 'CH');

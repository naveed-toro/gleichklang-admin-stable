SET FOREIGN_KEY_CHECKS = 0;
DELETE FROM locatable;
SET FOREIGN_KEY_CHECKS = 1;
# Insert continents
INSERT INTO locatable (DTYPE, create_date, legacy_id, i18n_key, parent_id)
  SELECT
    'Continent' AS DTYPE,
    NOW()       AS create_date,
    code        AS legacy_id,
    code        AS i18n_key,
    NULL        AS parent_id
  FROM continents AS cont;

# Insert countries

INSERT INTO locatable (DTYPE, legacy_id, i18n_key, create_date, parent_id)
  SELECT
    'Country' AS DTYPE,
    `ISO-2`   AS legacy_id,
    `ISO-2`   AS i18n_key,
    NOW()     AS create_date,
    loc.id    AS parent_id
  FROM countries AS coun
    LEFT JOIN locatable AS loc
      ON loc.i18n_key = coun.Continent;

# Insert Regions
INSERT INTO locatable (DTYPE, legacy_id, i18n_key, create_date, parent_id, region_name)
  SELECT
    'Region' AS DTYPE,
    iso      AS legacy_id,
    iso      AS i18n_key,
    NOW()    AS create_date,
    loc.id   AS parent_id,
    reg.name AS region_name
  FROM regions AS reg
    LEFT JOIN locatable AS loc
      ON loc.i18n_key = reg.a3166_1_iso;

UPDATE locatable SET sort_order = 0 WHERE i18n_key = 'DE';
UPDATE locatable SET sort_order = 1 WHERE i18n_key = 'AT';
UPDATE locatable SET sort_order = 2 WHERE i18n_key = 'CH';

DROP TABLE IF EXISTS countries, continents, regions;

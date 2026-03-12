DROP TABLE IF EXISTS continent, country, zip;

CREATE TABLE country AS
  SELECT
    l.id,
    l.i18n_key,
    i.i18n_value,
    l.parent_id AS continent_id
  FROM locatable AS l JOIN i18n AS i
      ON l.i18n_key = i.i18n_key
  WHERE l.DTYPE = 'Country' AND
        i.base_name = 'COUNTRY' AND
        i.language = 'DE';

CREATE TABLE zip AS
  SELECT
    l.id,
    l.zip,
    l.region_id,
    l2.parent_id as country_id
  FROM locatable AS l JOIN locatable as l2 ON l.parent_id = l2.id
  WHERE l.DTYPE = 'Zip' AND l2.DTYPE = 'Region';

CALL ADD_COLUMN('address', 'continent_id', 'BIGINT DEFAULT NULL');
CALL ADD_COLUMN('address', 'country_id', 'BIGINT DEFAULT NULL');
CALL ADD_COLUMN('address', 'zip_id', 'BIGINT DEFAULT NULL');
CALL ADD_COLUMN('address', 'region_id', 'BIGINT DEFAULT NULL');

CALL CREATE_INDEX('address_country', 'address', 'country');
CALL CREATE_INDEX('address_zip', 'address', 'zip');

CALL CREATE_INDEX('country_continent', 'country', 'continent_id');
CALL CREATE_INDEX('zip_region', 'zip', 'region_id');

UPDATE address AS a JOIN country AS c
    ON a.country = c.i18n_key
SET a.country_id = c.id, a.continent_id = c.continent_id;

UPDATE address AS a JOIN zip AS z
    ON a.zip = z.zip and a.country_id = z.country_id
SET a.zip_id = z.id, a.region_id = z.region_id;

CALL ADD_FOREIGN_KEY('address', 'continent_id', 'locatable', 'id');
CALL ADD_FOREIGN_KEY('address', 'country_id', 'locatable', 'id');
CALL ADD_FOREIGN_KEY('address', 'zip_id', 'locatable', 'id');
CALL ADD_FOREIGN_KEY('address', 'region_id', 'locatable', 'id');

DROP TABLE IF EXISTS continent, country, zip;
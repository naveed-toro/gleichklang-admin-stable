CALL CREATE_INDEX('locatable_region_name', 'locatable', 'region_name');

UPDATE locatable AS z
  JOIN locatable AS r
    ON z.region_name = r.i18n_key AND z.region_name IS NOT NULL AND
       z.DTYPE = 'Zip' AND r.DTYPE = 'Region'
SET z.region_id = r.id
WHERE z.region_id IS NULL;

SELECT id
INTO @ch_ti
FROM locatable
WHERE i18n_key = 'CH-TI' AND DTYPE = 'Region';

UPDATE locatable AS z
SET z.region_id = @ch_ti,
  z.region_name = 'CH-TI'
WHERE z.zip IN ('6713', '6714') AND z.region_name LIKE 'CH%';

/* there is a german exclave which has a german and a swiss postal code*/
SELECT id
INTO @ch_id
FROM locatable WHERE i18n_key = 'CH' AND DTYPE = 'Country';

SELECT id
INTO @ch_zip
FROM locatable WHERE zip = '8238' AND DTYPE = 'Zip' AND parent_id = @ch_id;

SELECT id
INTO @de_id
FROM locatable WHERE i18n_key = 'DE' AND DTYPE = 'Country';

SELECT id
INTO @de_zip
FROM locatable WHERE zip = '78266' AND DTYPE = 'Zip' AND parent_id = @de_id;

SELECT id
INTO @de_region
FROM locatable WHERE i18n_key = 'DE-BW';

UPDATE address AS a
SET
  country_id = @de_id,
  zip_id = @de_zip,
  region_id = @de_region
WHERE country_id = @ch_id AND zip_id = @ch_zip;

DELETE FROM locatable
WHERE DTYPE = 'Zip' AND region_id IS NULL;

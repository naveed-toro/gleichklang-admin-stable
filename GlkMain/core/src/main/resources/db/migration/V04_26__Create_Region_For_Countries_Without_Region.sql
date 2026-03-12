DELETE FROM locatable WHERE i18n_key = 'GN-KD; 2' AND DTYPE = 'Region';
DELETE FROM locatable WHERE i18n_key = 'IE-C; 2' AND DTYPE = 'Region';

INSERT INTO locatable (DTYPE, change_date, create_date, i18n_key, parent_id, region_name)
    SELECT
        'Region',
        NOW(),
        NOW(),
        CONCAT(c.i18n_key, '-', c.i18n_key),
        c.id,
        i.i18n_value
    FROM
        locatable c LEFT JOIN i18n i ON i.i18n_key = c.i18n_key AND i.language = 'DE' AND i.base_name = 'COUNTRY'
    WHERE
        c.DTYPE = 'Country' AND NOT EXISTS (SELECT 1 FROM locatable r WHERE r.parent_id = c.id);

INSERT INTO i18n (change_date, create_date, language, i18n_key, i18n_value, base_name)
    SELECT
        NOW(),
        NOW(),
        c_i.language,
        r.i18n_key,
        c_i.i18n_value,
        'REGION'
    FROM
        locatable r JOIN locatable c ON r.parent_id = c.id AND c.DTYPE = 'Country' JOIN i18n c_i ON c_i.i18n_key = c.i18n_key AND c_i.base_name = 'COUNTRY'
    WHERE
        r.DTYPE = 'Region' AND NOT EXISTS (SELECT 1 FROM i18n r_i WHERE r_i.i18n_key = r.i18n_key AND r_i.base_name = 'REGION' AND r_i.language = c_i.language);
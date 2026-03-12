CALL ADD_COLUMN('user_settings', 'language', 'VARCHAR(255) NOT NULL DEFAULT "DE"');
--DELETE FROM i18n WHERE language = 'KM';
DELETE FROM i18n WHERE language <> 'DE' AND language <> 'EN';
UPDATE user_settings SET language = 'DE' WHERE language <> 'DE' AND language <> 'EN';
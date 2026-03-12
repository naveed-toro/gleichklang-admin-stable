CALL CHANGE_COLUMN('choice_group', 'name', 'i18n_key', 'VARCHAR(255) NOT NULL');

INSERT IGNORE INTO i18n (create_date, language, i18n_key, i18n_value, base_name)
SELECT create_date, 'DE', i18n_key, i18n_key, 'CHOICE_GROUP' FROM choice_group;
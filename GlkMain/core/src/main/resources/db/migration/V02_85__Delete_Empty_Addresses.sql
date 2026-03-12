DELETE FROM address WHERE country_id IS NULL;
CALL DROP_COLUMN('address', 'country');
CALL DROP_COLUMN('address', 'zip');
CALL CHANGE_COLUMN('address', 'country_id', 'country_id', 'bigint(20) NOT NULL');
CALL CHANGE_COLUMN('address', 'continent_id', 'continent_id', 'bigint(20) NOT NULL');
CALL CHANGE_COLUMN('address', 'user_id', 'user_id', 'bigint(20) NOT NULL');
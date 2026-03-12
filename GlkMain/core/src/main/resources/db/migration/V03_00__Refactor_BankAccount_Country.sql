CALL ADD_COLUMN('bank_account', 'country_id', 'BIGINT');

UPDATE bank_account a
  JOIN locatable l ON a.country = l.i18n_key AND l.DTYPE = 'Country'

SET
  a.country_id = l.id;

CALL CHANGE_COLUMN('bank_account', 'country_id', 'country_id', 'BIGINT NOT NULL');
CALL ADD_FOREIGN_KEY('bank_account', 'country_id', 'locatable', 'id');
CALL DROP_COLUMN('bank_account', 'country');
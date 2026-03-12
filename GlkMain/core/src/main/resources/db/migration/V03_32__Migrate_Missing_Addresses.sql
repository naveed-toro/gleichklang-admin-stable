CALL ADD_COLUMN('address', 'checked', 'BIT(1) DEFAULT NULL');

UPDATE address AS a
SET a.checked = TRUE
WHERE unchecked IS NOT TRUE;

UPDATE address AS a
SET a.checked = FALSE
WHERE unchecked IS TRUE;

CALL DROP_COLUMN('address', 'unchecked');
SET @tr_id = (SELECT id FROM locatable WHERE i18n_key = "TR" AND DTYPE = "Country" LIMIT 1);
SET @cy_id = (SELECT id FROM locatable WHERE i18n_key = "CY" AND DTYPE = "Country" LIMIT 1);

SET @eu_id = (SELECT id FROM locatable WHERE i18n_key = "EU" AND DTYPE = "Continent" LIMIT 1);

UPDATE locatable SET parent_id = @eu_id WHERE id IN (@tr_id, @cy_id);
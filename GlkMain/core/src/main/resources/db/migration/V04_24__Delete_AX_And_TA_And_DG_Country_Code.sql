-- AX -> FI (Aland -> Finnland)
-- TA -> SH (Tristan da Cunha -> Saint Helena)
-- remove DG (Diego Garcia)

SET @ax_id = (SELECT id FROM locatable WHERE i18n_key = "AX" AND DTYPE = "Country" LIMIT 1);
SET @fi_id = (SELECT id FROM locatable WHERE i18n_key = "FI" AND DTYPE = "Country" LIMIT 1);
SET @ta_id = (SELECT id FROM locatable WHERE i18n_key = "TA" AND DTYPE = "Country" LIMIT 1);
SET @sh_id = (SELECT id FROM locatable WHERE i18n_key = "SH" AND DTYPE = "Country" LIMIT 1);
SET @dg_id = (SELECT id FROM locatable WHERE i18n_key = "DG" AND DTYPE = "Country" LIMIT 1);

-- update existing things
UPDATE address SET address.country_id = @fi_id WHERE address.country_id = @ax_id;
UPDATE address SET address.country_id = @sh_id WHERE address.country_id = @ta_id;

UPDATE region_search_request set region_search_request.country_id = @fi_id WHERE region_search_request.country_id = @ax_id;
UPDATE region_search_request set region_search_request.country_id = @sh_id WHERE region_search_request.country_id = @ta_id;

UPDATE region_search_request_restriction SET region_search_request_restriction.locatable_id = @fi_id WHERE region_search_request_restriction.locatable_id = @ax_id;
UPDATE region_search_request_restriction SET region_search_request_restriction.locatable_id = @sh_id WHERE region_search_request_restriction.locatable_id = @ta_id;

UPDATE filter SET filter.locatable_id = @fi_id WHERE filter.locatable_id = @ax_id;
UPDATE filter SET filter.locatable_id = @sh_id WHERE filter.locatable_id = @ta_id;

UPDATE bank_account SET bank_account.country_id = @fi_id WHERE bank_account.country_id = @ax_id;
UPDATE bank_account SET bank_account.country_id = @sh_id WHERE bank_account.country_id = @ta_id;

-- delete
DELETE FROM locatable WHERE i18n_key = "AX" AND DTYPE = "Country";
DELETE FROM locatable WHERE i18n_key = "TA" AND DTYPE = "Country";
DELETE FROM locatable WHERE i18n_key = "DG" AND DTYPE = "Country";

DELETE FROM i18n WHERE i18n_key = "AX" AND base_name = 'COUNTRY';

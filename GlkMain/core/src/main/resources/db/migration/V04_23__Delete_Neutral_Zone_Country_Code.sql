-- Replace all occurrences of NT (Neutral Zone) to IQ (Iraq)

SET @iraq_country_id = (SELECT id FROM locatable WHERE i18n_key = "IQ" AND DTYPE = "Country" LIMIT 1);
SET @neutral_zone_country_id = (SELECT id FROM locatable WHERE i18n_key = "NT" AND DTYPE = "Country" LIMIT 1);

-- Change all entities with NT (Neutral Zone) to IQ (Iraq)
UPDATE address SET address.country_id = @iraq_country_id WHERE address.country_id = @neutral_zone_country_id;
UPDATE region_search_request set region_search_request.country_id = @iraq_country_id WHERE region_search_request.country_id = @neutral_zone_country_id;
UPDATE region_search_request_restriction SET region_search_request_restriction.locatable_id = @iraq_country_id WHERE region_search_request_restriction.locatable_id = @neutral_zone_country_id;
UPDATE filter SET filter.locatable_id = @iraq_country_id WHERE filter.locatable_id = @neutral_zone_country_id;
UPDATE bank_account SET bank_account.country_id = @iraq_country_id WHERE bank_account.country_id = @neutral_zone_country_id;

-- Delete IC entry (Canary Islands)
DELETE FROM locatable WHERE i18n_key = "NT" AND DTYPE = "Country";

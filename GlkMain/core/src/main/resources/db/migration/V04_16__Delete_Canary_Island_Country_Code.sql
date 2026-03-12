-- Replace all occurrences of IC (Canary Islands) to ES (Spain)
-- Delete locatable entity IC

SET @spain_country_id = (SELECT id FROM locatable WHERE i18n_key = "ES" AND DTYPE = "Country" LIMIT 1);
SET @canary_island_country_id = (SELECT id FROM locatable WHERE i18n_key = "IC" AND DTYPE = "Country" LIMIT 1);

-- Change all entities with IC (Canary Islands) to ES (Spain)
UPDATE address SET address.country_id = @spain_country_id WHERE address.country_id = @canary_island_country_id;
UPDATE region_search_request set region_search_request.country_id = @spain_country_id WHERE region_search_request.country_id = @canary_island_country_id;
UPDATE region_search_request_restriction SET region_search_request_restriction.locatable_id = @spain_country_id WHERE region_search_request_restriction.locatable_id = @canary_island_country_id;
UPDATE filter SET filter.locatable_id = @spain_country_id WHERE filter.locatable_id = @canary_island_country_id;
UPDATE bank_account SET bank_account.country_id = @spain_country_id WHERE bank_account.country_id = @canary_island_country_id;

-- Delete IC entry (Canary Islands)
DELETE FROM locatable WHERE i18n_key = "IC" AND DTYPE = "Country";

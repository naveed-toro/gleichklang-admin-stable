package de.binaerebauten.gleichklang.core.migration;

import de.binaerebauten.gleichklang.core.migration.model.MigrationLocatableEntity;
import de.binaerebauten.gleichklang.core.migration.querybuilder.LocatableSQLQueryBuilder;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.flywaydb.core.api.migration.spring.SpringJdbcMigration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by michael on 22/07/15.
 */
public class V01_00_14__Insert_Zips implements SpringJdbcMigration
{
	private static final char DELIMITER = '\t';

	private static final Logger LOG = LoggerFactory.getLogger(V01_00_14__Insert_Zips.class);
	private static final int BATCH_SIZE = 1500;
	private final Map<String, Long> countries = new HashMap<>();
	private final Map<String, Long> regions = new HashMap<>();

	@Override
	public void migrate(JdbcTemplate jdbcTemplate) throws Exception
	{
		Integer numberOfUsers = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM compuser", Integer.class);

		if(numberOfUsers == 0){
			return;
		}

		prepare(jdbcTemplate);
		insertZips(jdbcTemplate);
	}


	private void prepare(JdbcTemplate jdbcTemplate)
	{
		jdbcTemplate.execute("DELETE FROM locatable WHERE DTYPE = 'Zip'");
		initCountryMap(jdbcTemplate);
		initRegionsMap(jdbcTemplate);
	}

	private void insertZips(JdbcTemplate jdbcTemplate) throws IOException
	{
		LocatableSQLQueryBuilder locatableSQLQueryBuilder = new LocatableSQLQueryBuilder();

		try (InputStream resource = V01_00_14__Insert_Zips.class.getResourceAsStream("/db/migration/data/zip_table.csv"))
		{

			try (CSVParser parser = CSVFormat.DEFAULT
					.withDelimiter(DELIMITER)
					.withHeader()
					.parse(new InputStreamReader(resource, StandardCharsets.UTF_8)))
			{
				Iterator<CSVRecord> recordsIter = parser.iterator();

				int zipSortOrder = 1;
				MigrationLocatableEntity zipEntity = new MigrationLocatableEntity(
						MigrationLocatableEntity.LOCATABLE_TYPE.Zip, zipSortOrder++);
				while (recordsIter.hasNext())
				{
					final CSVRecord record = recordsIter.next();

					zipEntity.setZip(record.get("postal_code"));
					zipEntity.setLatitude(Double.valueOf(record.get("latitude")));
					zipEntity.setLongitude(Double.valueOf(record.get("longitude")));
					zipEntity.setParentId(getCountryId(record));
					zipEntity.setRegionName(getRegionName(record));
					zipEntity.setRegionId(getRegionId(record));

					locatableSQLQueryBuilder.addValue(zipEntity);

					if(zipSortOrder > 1 && zipSortOrder % BATCH_SIZE == 0){
						locatableSQLQueryBuilder = insertZips(jdbcTemplate, locatableSQLQueryBuilder, zipEntity);
					}

					zipEntity = new MigrationLocatableEntity(
							MigrationLocatableEntity.LOCATABLE_TYPE.Zip, zipSortOrder++);
				}

				insertZips(jdbcTemplate, locatableSQLQueryBuilder, zipEntity);

			}
		}
	}

	private LocatableSQLQueryBuilder insertZips(JdbcTemplate jdbcTemplate,
			LocatableSQLQueryBuilder locatableSQLQueryBuilder, MigrationLocatableEntity zipEntity)
	{
		int inserted = jdbcTemplate.update(locatableSQLQueryBuilder.getSQLQuery());
		LOG.info("Inserted {} zips. Last inserted zip: {} {}", inserted, zipEntity.getRegionName(),
				zipEntity.getZip());
		locatableSQLQueryBuilder = new LocatableSQLQueryBuilder();
		return locatableSQLQueryBuilder;
	}

	private Long getRegionId(CSVRecord record)
	{
		String regionName = getRegionName(record);
		return regions.get(regionName);
	}

	private String getRegionName(CSVRecord record)
	{
		String countryCode = record.get("country_code");
		String placeCode = record.get("place_code");
		if (placeCode != null && !placeCode.contains(countryCode))
		{
			return String.format("%s-%s", countryCode, placeCode);
		}
		return placeCode;
	}

	private Long getCountryId(CSVRecord record)
	{
		return countries.get(record.get("country_code"));
	}

	private void initCountryMap(JdbcTemplate jdbcTemplate)
	{
		jdbcTemplate.queryForList("SELECT DISTINCT legacy_id AS country_code, id FROM locatable WHERE DTYPE = 'Country' ")
				.forEach(stringObjectMap -> countries.put((String) stringObjectMap.get("country_code"), (Long) stringObjectMap.get("id")));
	}

	private void initRegionsMap(JdbcTemplate jdbcTemplate)
	{
		jdbcTemplate.queryForList("SELECT DISTINCT legacy_id AS region_code, id FROM locatable WHERE DTYPE = 'Region'")
				.forEach(stringObjectMap -> regions.put((String) stringObjectMap.get("region_code"), (Long) stringObjectMap.get("id")));
	}

}

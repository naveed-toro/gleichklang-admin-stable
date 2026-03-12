package de.binaerebauten.gleichklang.core.migration;

import org.flywaydb.core.api.migration.spring.SpringJdbcMigration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import java.util.ArrayList;
import java.util.List;

public class V04_52__Delete_After_Cancel_Filter implements SpringJdbcMigration
{
	@Override
	public void migrate(JdbcTemplate jdbcTemplate)
	{
		final ArrayList<Long> filterIds = new ArrayList<>();
		
		final SqlRowSet rowSet = jdbcTemplate.queryForRowSet("SELECT id FROM filter WHERE DTYPE = 'AfterCancelFilter';");
		while (rowSet.next())
		{
			filterIds.add(rowSet.getLong("id"));
		}
		
		List<Long> additionalIds = new ArrayList<>(filterIds);
		
		while(!additionalIds.isEmpty())
		{
			additionalIds = getReferenceIds(jdbcTemplate, additionalIds);
			filterIds.addAll(additionalIds);
		}
		
		deleteFilters(jdbcTemplate, filterIds);
	}
	
	private List<Long> getReferenceIds(JdbcTemplate jdbcTemplate, List<Long> ids)
	{
		final ArrayList<Long> additionalIds = new ArrayList<>();
		if (ids.isEmpty()) return additionalIds;
		
		final NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
		final SqlParameterSource sqlParameterSource = new MapSqlParameterSource("ids", ids);
		final SqlRowSet rowSet = namedParameterJdbcTemplate.queryForRowSet("SELECT id FROM filter WHERE left_filter_id IN (:ids) OR right_filter_id IN (:ids) OR single_filter_id IN (:ids) OR template_filter_id IN (:ids)", sqlParameterSource);
		while (rowSet.next())
		{
			additionalIds.add(rowSet.getLong("id"));
		}
		
		return additionalIds;
	}
	
	private void deleteFilters(JdbcTemplate jdbcTemplate, List<Long> ids)
	{
		if(ids.isEmpty()) return;
		
		jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0;");
		ids.forEach(id -> jdbcTemplate.execute("DELETE FROM filter WHERE id = " + id));
		jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1;");
	}
}

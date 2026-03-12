package de.binaerebauten.gleichklang.core.migration.queryinserter;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by michael on 17/06/15.
 */
public abstract class BaseInserter
{
	private static final Logger LOG = LoggerFactory.getLogger(BaseInserter.class);
	protected JdbcTemplate jdbcTemplate;

	public BaseInserter(JdbcTemplate jdbcTemplate)
	{
		this.jdbcTemplate = jdbcTemplate;
	}

	protected int executeMultiple(List<String> scripts, String label)
	{
		if (scripts.isEmpty())
		{
			LOG.warn("Scripts for batch inserts are empty!");
			return 0;
		}

		String[] scriptsArray = scripts.toArray(new String[scripts.size()]);
		long start = System.currentTimeMillis();

		int[] results = jdbcTemplate.batchUpdate(scriptsArray);
		long fin = System.currentTimeMillis();

		LOG.debug("{} values for {} were inserted. It took {} ms", results.length, label, fin - start);

		commit();
		return results.length;
	}

	protected int executeMultiple(List<String> scripts)
	{

		return executeMultiple(scripts, "unknown");
	}

	protected int executeOne(String script)
	{
		if (Strings.isNullOrEmpty(script))
		{
			LOG.warn("Script is empty!");
			return 0;
		}
		int result = jdbcTemplate.update(script);
		commit();
		return result;
	}

	private void commit()
	{
		try
		{
			jdbcTemplate.getDataSource().getConnection().commit();
		}
		catch (SQLException e)
		{
			LOG.error("Commit failed", e);
		}
	}
}

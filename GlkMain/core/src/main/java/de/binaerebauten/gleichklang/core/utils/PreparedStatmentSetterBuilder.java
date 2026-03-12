package de.binaerebauten.gleichklang.core.utils;

import org.springframework.jdbc.core.PreparedStatementSetter;

/**
 * Mixin interface to create {@link PreparedStatementSetter} objects.
 *
 * Makes it easier to use the {@link org.springframework.jdbc.core.JdbcTemplate#update(String, PreparedStatementSetter)}
 * method.
 */
public interface PreparedStatmentSetterBuilder
{
	/**
	 * Returns a {@link PreparedStatementSetter} that sets the given parameters
	 * on the passed {@link java.sql.PreparedStatement}.
	 *
	 * @param params the parameters to set
	 *
	 * @return a prepared statement setter that sets the parameters of the passed prepared statement
	 */
	default PreparedStatementSetter setParams(Object... params)
	{
		return ps -> {
			for (int i = 0; i < params.length; i++)
			{
				ps.setObject(i + 1, params[i]);
			}
		};
	}
}

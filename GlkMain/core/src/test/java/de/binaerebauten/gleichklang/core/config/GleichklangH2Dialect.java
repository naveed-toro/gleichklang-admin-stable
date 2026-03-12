package de.binaerebauten.gleichklang.core.config;

import org.hibernate.dialect.H2Dialect;
import org.hibernate.dialect.function.SQLFunctionTemplate;
import org.hibernate.type.StandardBasicTypes;

import static de.binaerebauten.gleichklang.core.config.GleichklangMySQLDialect.REGEXP_FUNCTION;
import static de.binaerebauten.gleichklang.core.config.GleichklangMySQLDialect.UNIX_TIMESTAMP_FUNCTION;

/**
 * Created by michael on 20/05/15.
 */
public class GleichklangH2Dialect extends H2Dialect
{
	public GleichklangH2Dialect()
	{
		super();
		registerColumnType(java.sql.Types.VARCHAR, 65535, "clob");
		registerFunction(REGEXP_FUNCTION, new SQLFunctionTemplate(StandardBasicTypes.INTEGER, "?1 REGEXP ?2"));
		registerFunction(UNIX_TIMESTAMP_FUNCTION, new SQLFunctionTemplate(StandardBasicTypes.INTEGER, "SECOND(?1)"));
	}
}

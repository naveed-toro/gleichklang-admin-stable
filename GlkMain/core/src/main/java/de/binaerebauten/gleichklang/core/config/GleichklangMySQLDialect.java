package de.binaerebauten.gleichklang.core.config;

import org.hibernate.dialect.MySQL57InnoDBDialect;
import org.hibernate.dialect.function.SQLFunctionTemplate;
import org.hibernate.type.StandardBasicTypes;

public class GleichklangMySQLDialect extends MySQL57InnoDBDialect
{
	public static final String REGEXP_FUNCTION = "REGEXP";
	public static final String UNIX_TIMESTAMP_FUNCTION = "UNIX_TIMESTAMP";
	
	public GleichklangMySQLDialect()
	{
		super();
		registerColumnType(java.sql.Types.VARCHAR, 65535, "text");
		registerFunction(REGEXP_FUNCTION, new SQLFunctionTemplate(StandardBasicTypes.INTEGER, "?1 REGEXP ?2"));
	}
}

package de.binaerebauten.gleichklang.core.migration;

import com.google.common.collect.Lists;
import de.binaerebauten.gleichklang.core.migration.model.UserPasswordInfo;
import de.binaerebauten.gleichklang.core.migration.querybuilder.UserPasswordSQLQueryBuilder;
import org.flywaydb.core.api.migration.spring.SpringJdbcMigration;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

public class V02_44__Update_User_Passwords implements SpringJdbcMigration
{
	private static final int CHUNK_SIZE = 500;
	
	@Override
	public void migrate(JdbcTemplate jdbcTemplate) throws Exception
	{
		jdbcTemplate.execute("CALL ADD_COLUMN('user_', 'password_encr', 'VARCHAR(255) DEFAULT NULL')");
		
		final UserPasswordSQLQueryBuilder userPasswordSQLQueryBuilder = new UserPasswordSQLQueryBuilder();
		final List<UserPasswordInfo> userPasswordInfos = jdbcTemplate.query("SELECT id, password FROM user_", new BeanPropertyRowMapper<>(UserPasswordInfo.class));
		
		for (List<UserPasswordInfo> partition : Lists.partition(userPasswordInfos, CHUNK_SIZE))
		{
			userPasswordSQLQueryBuilder.clear();
			partition.forEach(userPasswordSQLQueryBuilder::addUserPasswordInfo);
			
			jdbcTemplate.execute(userPasswordSQLQueryBuilder.getQuery());
		}
		
		jdbcTemplate.execute("ALTER TABLE user_ CHANGE password password_old VARCHAR(35) DEFAULT NULL");
		jdbcTemplate.execute("ALTER TABLE user_ CHANGE password_encr password VARCHAR(255) NOT NULL");
		jdbcTemplate.execute("CALL DROP_COLUMN('user_', 'password_encr')");
	}
}

package de.binaerebauten.gleichklang.core.migration.querybuilder;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import de.binaerebauten.gleichklang.core.migration.model.UserPasswordInfo;
import java.util.StringJoiner;

public class UserPasswordSQLQueryBuilder
{
	protected static final int STRENGTH = 5;
	private StringJoiner stringJoiner = new StringJoiner(", ");

	public void addUserPasswordInfo(UserPasswordInfo userPasswordInfo)
	{
		PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(STRENGTH);
		final String encodedPassword = passwordEncoder.encode(userPasswordInfo.getPassword());

		String query = String.format("(%s, '', NOW(), '', '', '', '%s')",
				userPasswordInfo.getId(), encodedPassword);
		stringJoiner.add(query);
	}

	public String getQuery()
	{
		return String.format(
				"INSERT INTO user_ (id, alias, birth_date, email, member_status, password, password_encr) "
						+ "VALUES %s ON DUPLICATE KEY UPDATE password_encr=VALUES(password_encr)",
				stringJoiner.toString());
	}

	public boolean isEmpty()
	{
		return stringJoiner.length() == 0;
	}

	public void clear()
	{
		stringJoiner = new StringJoiner(", ");
	}

}

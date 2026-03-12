package de.binaerebauten.gleichklang.heidelpaymigration;

import de.binaerebauten.gleichklang.heidelpaymigration.service.HeidelpayRegistrationMigration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class Starter implements CommandLineRunner
{
	@Autowired
	private HeidelpayRegistrationMigration heidelpayRegistrationMigration;

	@Override
	public void run(String... strings) throws Exception
	{
		heidelpayRegistrationMigration.migrate();
	}

}

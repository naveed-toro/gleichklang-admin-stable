package de.binaerebauten.gleichklang.heidelpaymigration;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.velocity.VelocityAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;

import java.io.IOException;

@SpringBootApplication(exclude = { SecurityAutoConfiguration.class, VelocityAutoConfiguration.class })
public class TestMigrationApplication
{
	public static void main(String... args) throws IOException
	{
		new SpringApplicationBuilder(TestMigrationApplication.class).web(false).run(args);
	}

}

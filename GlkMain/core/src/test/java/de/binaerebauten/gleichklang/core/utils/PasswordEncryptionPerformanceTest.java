package de.binaerebauten.gleichklang.core.utils;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class PasswordEncryptionPerformanceTest
{
	private static final Logger LOG = LoggerFactory.getLogger(PasswordEncryptionPerformanceTest.class);

	@Test
	@Ignore
	public void testEncryptionPerformance() throws Exception
	{
		final int times = 100;
		String password = "password";

		for(int strength = 5; strength <= 10; strength++){
			PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(strength);

			long start = System.currentTimeMillis();
			for (int i = 0; i < times; i++)
			{
				passwordEncoder.encode(password);
			}

			LOG.info("Encoding of {} passwords for strength {} took {} ms", times, strength,
					String.valueOf(System.currentTimeMillis() - start));
		}

	}
}

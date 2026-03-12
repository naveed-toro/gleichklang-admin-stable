package de.binaerebauten.gleichklang.core.repository.heidelpay;

import de.binaerebauten.gleichklang.core.model.heidelpay.Registration;
import de.binaerebauten.gleichklang.core.repository.AbstractRepositoryTest;
import de.binaerebauten.gleichklang.core.utils.DefaultEntityFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Collections;

public class RegistrationRepositoryTest extends AbstractRepositoryTest<Registration>
{
	@Autowired
	private RegistrationRepository registrationRepository;
	
	@Autowired
	private DefaultEntityFactory defaultEntityFactory;
	
	@Override
	protected Collection<Registration> getPersistedEntities()
	{
		final Registration registration = new Registration();
		registration.setActive(true);
		registration.setEmail("test");
		registration.setFirstName("test");
		registration.setLastName("test");
		registration.setTransactionId("test");
		registration.setUniqueId("test");
		
		return Collections.singleton(registrationRepository.save(registration));
	}
	
	@Override
	protected JpaRepository<Registration, Long> getRepository()
	{
		return registrationRepository;
	}
}

package de.binaerebauten.gleichklang.core.repository;

import de.binaerebauten.gleichklang.core.model.payment.ExternalPaymentRegistration;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.utils.DefaultEntityFactory;
import de.binaerebauten.gleichklang.core.utils.PaymentEntityFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Unit test for {@link ExternalPaymentRegistrationRepository}.
 */
public class ExternalPaymentRegistrationRepositoryTest
		extends AbstractRepositoryTest<ExternalPaymentRegistration>
{
	private static final String REGISTRATION_ID = "registration-id";
	private static final String EXTERNAL_REFERENCE_ID = "EXTERNAL_REFERENCE_ID";

	@Autowired
	private ExternalPaymentRegistrationRepository externalPaymentRegistrationRepository;

	@Autowired
	private DefaultEntityFactory defaultEntityFactory;

	@Autowired
	private PaymentEntityFactory paymentEntityFactory;

	private ExternalPaymentRegistration externalPaymentRegistration;

	private User userWithRegistration;

	private User userWithoutRegistration;

	@Test
	public void testFindByUser()
	{
		assertThat(externalPaymentRegistrationRepository.findByUser(userWithoutRegistration),
				nullValue());
		assertThat(externalPaymentRegistrationRepository.findByUser(userWithRegistration),
				is(externalPaymentRegistration));
	}

	@Test
	public void testFindByUserAndRegistrationId()
	{
		assertThat(externalPaymentRegistrationRepository.findByUserAndRegistrationId(userWithoutRegistration, REGISTRATION_ID),
				nullValue());
		assertThat(externalPaymentRegistrationRepository.findByUserAndRegistrationId(userWithRegistration, REGISTRATION_ID),
				is(externalPaymentRegistration));
	}

	@Test
	public void testFindByExternalReferenceId()
	{
		externalPaymentRegistration.setExternalReferenceId(EXTERNAL_REFERENCE_ID);
		externalPaymentRegistrationRepository.save(externalPaymentRegistration);

		ExternalPaymentRegistration foundExternalPaymentRegistration =
				externalPaymentRegistrationRepository.findByExternalReferenceId(EXTERNAL_REFERENCE_ID);

		assertThat(foundExternalPaymentRegistration, is(externalPaymentRegistration));
	}

	@Override
	protected Collection<ExternalPaymentRegistration> getPersistedEntities()
	{
		userWithoutRegistration = defaultEntityFactory.persistDefaultUser("WithoutRegistration");
		userWithRegistration = defaultEntityFactory.persistDefaultUser("WithRegistration");
		externalPaymentRegistration = paymentEntityFactory.persistExternalPaymentRegistration(userWithRegistration, REGISTRATION_ID);

		return Collections.singletonList(externalPaymentRegistration);
	}

	@Override
	protected JpaRepository<ExternalPaymentRegistration, Long> getRepository()
	{
		return externalPaymentRegistrationRepository;
	}
}

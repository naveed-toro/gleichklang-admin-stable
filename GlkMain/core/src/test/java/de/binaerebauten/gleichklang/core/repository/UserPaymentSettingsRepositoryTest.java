package de.binaerebauten.gleichklang.core.repository;

import de.binaerebauten.gleichklang.core.model.payment.PaymentMethod;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.model.user.UserPaymentSettings;
import de.binaerebauten.gleichklang.core.utils.DefaultEntityFactory;
import de.binaerebauten.gleichklang.core.utils.DefaultStaticEntityFactory;
import de.binaerebauten.gleichklang.core.utils.PaymentEntityFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Unit tests for {@link UserPaymentSettingsRepository}.
 */
public class UserPaymentSettingsRepositoryTest
		extends AbstractRepositoryTest<UserPaymentSettings>
{
	private static final String EXTERNAL_PAYMENT_REGISTRATION_ID = "externalPaymentRegistrtionId";

	@Autowired
	private UserPaymentSettingsRepository userPaymentSettingsRepository;

	@Autowired
	private DefaultEntityFactory entityFactory;

	@Autowired
	private PaymentEntityFactory paymentEntityFactory;

	private UserPaymentSettings userPaymentSettings;

	private User user;

	@Test
	public void testFindByUser()
	{
		Optional<UserPaymentSettings> foundUserPaymentSettings = userPaymentSettingsRepository.findByUser(user);

		assertThat(foundUserPaymentSettings, is(Optional.of(userPaymentSettings)));
	}

	@Override
	protected Collection<UserPaymentSettings> getPersistedEntities()
	{
		user = entityFactory.persistDefaultUser(DefaultStaticEntityFactory.DEFAULT_ALIAS);

		userPaymentSettings = paymentEntityFactory.persistUserPaymentSettings(user, PaymentMethod.CREDIT_CARD);

		return Collections.singletonList(userPaymentSettings);
	}

	@Override
	protected JpaRepository<UserPaymentSettings, Long> getRepository()
	{
		return userPaymentSettingsRepository;
	}
}

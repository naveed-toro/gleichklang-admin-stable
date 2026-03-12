package de.binaerebauten.gleichklang.core.repository;

import de.binaerebauten.gleichklang.core.model.locatable.Country;
import de.binaerebauten.gleichklang.core.model.payment.BankAccount;
import de.binaerebauten.gleichklang.core.utils.DefaultEntityFactory;
import de.binaerebauten.gleichklang.core.utils.PaymentEntityFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Unit tests for the {@link BankAccountRepository}.
 */
public class BankAccountRepositoryTest
		extends AbstractRepositoryTest<BankAccount>
{
	@Autowired
	private DefaultEntityFactory defaultEntityFactory;

	@Autowired
	private PaymentEntityFactory paymentEntityFactory;

	@Autowired
	private BankAccountRepository bankAccountRepository;

	private BankAccount bankAccount;
	private Country country;

	@Test
	public void testFindByCountry()
	{
		assertThat("Expected that query is syntactically correct!",
				bankAccountRepository.findByCountry(null), equalTo(Optional.empty()));

		assertThat(bankAccountRepository.findByCountry(country), equalTo(Optional.of(bankAccount)));
	}

	@Override
	protected Collection<BankAccount> getPersistedEntities()
	{
		country = defaultEntityFactory.persistDefaultCountry();

		bankAccount = paymentEntityFactory.persistDefaultBankAccount(country);
		return Collections.singletonList(bankAccount);
	}

	@Override
	protected JpaRepository<BankAccount, Long> getRepository()
	{
		return bankAccountRepository;
	}
}

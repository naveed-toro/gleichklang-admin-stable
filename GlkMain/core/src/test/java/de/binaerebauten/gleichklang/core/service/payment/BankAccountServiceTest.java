package de.binaerebauten.gleichklang.core.service.payment;

import de.binaerebauten.gleichklang.core.model.I18NEntity;
import de.binaerebauten.gleichklang.core.model.locatable.Country;
import de.binaerebauten.gleichklang.core.model.payment.BankAccount;
import de.binaerebauten.gleichklang.core.model.user.AddressNotFoundException;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.repository.BankAccountRepository;
import de.binaerebauten.gleichklang.core.repository.BasePersistenceTest;
import de.binaerebauten.gleichklang.core.repository.LocatableRepository;
import de.binaerebauten.gleichklang.core.utils.DefaultEntityFactory;
import de.binaerebauten.gleichklang.core.utils.PaymentEntityFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link BankAccountService}.
 */
public class BankAccountServiceTest extends BasePersistenceTest
{
	@Autowired
	private DefaultEntityFactory entityFactory;
	
	@Autowired
	private PaymentEntityFactory paymentEntityFactory;
	
	@Autowired
	private BankAccountRepository bankAccountRepository;
	
	@Mock
	private LocatableRepository locatableRepository;
	
	private BankAccountService bankAccountService;
	
	private Country defaultCountry;
	
	private User user;

	@Before
	public void setup()
	{
		MockitoAnnotations.initMocks(this);
		
		defaultCountry = entityFactory.persistDefaultCountry();
		when(locatableRepository.findByCountryCode(I18NEntity.Language.DE.name())).thenReturn(defaultCountry);
		bankAccountService = new BankAccountService(bankAccountRepository, locatableRepository);
		
		user = entityFactory.persistDefaultUser("test user");
	}
	
	@After
	public void tearDown() throws Exception
	{
		entityFactory.reset();
	}
	
	@Test
	public void testFindPrepaymentBankAccount()
	{
		BankAccount someBankAccount = null;
		try
		{
			someBankAccount = paymentEntityFactory.persistDefaultBankAccount(user.getPaymentAddress().getCountry());
		}
		catch (AddressNotFoundException ignored)
		{
		}
		
		BankAccount bankAccount = bankAccountService.findPrepaymentBankAccount(user);
		
		assertThat(bankAccount, equalTo(someBankAccount));
	}

	@Test
	public void testFindDefaultPrepaymentBankAccount()
	{
		final BankAccount defaultBankAccount = paymentEntityFactory.persistDefaultBankAccount(defaultCountry);
		
		BankAccount bankAccount = bankAccountService.findPrepaymentBankAccount(user);
		
		assertThat(bankAccount, equalTo(defaultBankAccount));
	}

}

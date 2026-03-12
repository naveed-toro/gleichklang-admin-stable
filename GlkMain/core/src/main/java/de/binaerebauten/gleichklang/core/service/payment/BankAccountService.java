package de.binaerebauten.gleichklang.core.service.payment;

import de.binaerebauten.gleichklang.core.model.I18NEntity;
import de.binaerebauten.gleichklang.core.model.locatable.Country;
import de.binaerebauten.gleichklang.core.model.payment.BankAccount;
import de.binaerebauten.gleichklang.core.model.payment.BankAccountNotFoundException;
import de.binaerebauten.gleichklang.core.model.user.AddressNotFoundException;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.repository.BankAccountRepository;
import de.binaerebauten.gleichklang.core.repository.LocatableRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

/**
 * Provides common operations for bank accounts.
 */
@Service
public class BankAccountService
{
	private static final Logger logger = LoggerFactory.getLogger(BankAccountService.class);

	private static final String DEFAULT_COUNTRY_CODE = I18NEntity.Language.DE.name();
	
	private final BankAccountRepository bankAccountRepository;
	
	private final Country defaultCountry;
	
	@Autowired
	public BankAccountService(BankAccountRepository bankAccountRepository,
			LocatableRepository locatableRepository)
	{
		this.bankAccountRepository = bankAccountRepository;
		this.defaultCountry = locatableRepository.findByCountryCode(DEFAULT_COUNTRY_CODE);
	}

	/**
	 * Finds the prepayment bank account for the given user.
	 *
	 * @param user the non-null user
	 * @return the prepayment bank account
	 */
	@Transactional
	public BankAccount findPrepaymentBankAccount(User user)
	{
		Objects.requireNonNull(user, "user == null");
		
		Country country;
		try
		{
			country = user.getPaymentAddress().getCountry();
		}
		catch (AddressNotFoundException e)
		{
			logger.warn("Primary address wasn't found or is incorrect for user {}", user.getEmail());
			country = defaultCountry;
		}
		
		Optional<BankAccount> bankAccount = bankAccountRepository.findByCountry(country);
		if (!bankAccount.isPresent())
		{
			bankAccount = bankAccountRepository.findByCountry(defaultCountry);
		}
		
		return bankAccount.orElseThrow(() -> new BankAccountNotFoundException("Bank account for a prepayment not found"));
	}
	
}

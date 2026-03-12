package de.binaerebauten.gleichklang.core.repository;

import de.binaerebauten.gleichklang.core.model.locatable.Country;
import de.binaerebauten.gleichklang.core.model.payment.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for retrieving {@link de.binaerebauten.gleichklang.core.model.payment.BankAccount}
 */
@Repository
public interface BankAccountRepository extends JpaRepository<BankAccount, Long>
{
	/**
	 * Finds an active bank account for the given country.
	 *
	 * @param country the country for which the bank account is registered
	 * @return the bank account for this country
	 */
	@Query("FROM BankAccount a WHERE a.country = :country AND a.active IS TRUE")
	Optional<BankAccount> findByCountry(@Param("country") Country country);

	BankAccount findByBankName(String bankName);

}

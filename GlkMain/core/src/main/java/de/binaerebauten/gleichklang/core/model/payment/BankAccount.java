package de.binaerebauten.gleichklang.core.model.payment;

import de.binaerebauten.gleichklang.core.model.BaseEntity;
import de.binaerebauten.gleichklang.core.model.locatable.Country;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Represents a bank account that can be used for a {@link Prepayment}
 */
@Entity
@Table(name = "bank_account")
public class BankAccount extends BaseEntity
{
	@NotNull
	@Column(nullable = false)
	private String holder;

	@NotNull
	@Column(name = "bank_name", nullable = false)
	private String bankName;

	@NotNull
	@Column(name = "account_number", nullable = false)
	private String accountNumber;

	@NotNull
	@Column(name = "bank_number", nullable = false)
	private String bankNumber;

	@NotNull
	@Size(max = 34)
	@Column(length = 34, nullable = false)
	private String iban;

	@NotNull
	@Column(nullable = false)
	private String bic;

	@OneToOne
	@NotNull
	@JoinColumn(name = "country_id", nullable = false)
	private Country country;
	
	private Boolean active;

	public String getHolder()
	{
		return holder;
	}

	public void setHolder(String holder)
	{
		this.holder = holder;
	}

	public String getBankName()
	{
		return bankName;
	}

	public void setBankName(String bankName)
	{
		this.bankName = bankName;
	}

	public String getAccountNumber()
	{
		return accountNumber;
	}

	public void setAccountNumber(String accountNumber)
	{
		this.accountNumber = accountNumber;
	}

	public String getBankNumber()
	{
		return bankNumber;
	}

	public void setBankNumber(String bankNumber)
	{
		this.bankNumber = bankNumber;
	}

	public String getIban()
	{
		return iban;
	}

	public void setIban(String iban)
	{
		this.iban = iban;
	}

	public String getBic()
	{
		return bic;
	}

	public void setBic(String bic)
	{
		this.bic = bic;
	}

	public Country getCountry()
	{
		return country;
	}

	public void setCountry(Country country)
	{
		this.country = country;
	}
	
	public Boolean getActive()
	{
		return active;
	}
	
	public void setActive(Boolean active)
	{
		this.active = active;
	}
	
	/**
	 * Returns the formatted bank account.
	 *
	 * @return formatted bank account
	 */
	public String getFormattedDescription()
	{
		return I18N.BANKACCOUNT_FORMAT.msg(bankName,
				bankNumber, accountNumber,
				bic, iban);
	}
}

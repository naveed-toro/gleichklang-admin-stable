package de.binaerebauten.gleichklang.core.model.payment;

import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * This embeddable class represents a monetary amout.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@Embeddable
public class MonetaryAmount
{
    @XmlValue
	@DecimalMin("0")
	@NotNull
	private BigDecimal amount;

    @XmlAttribute
	@Enumerated(EnumType.STRING)
	@NotNull
	private AvailableCurrency currency;

	/**
	 * Required by JPA.
	 */
	public MonetaryAmount()
	{
		super();
	}

	public MonetaryAmount(BigDecimal amount, AvailableCurrency currency)
	{
		Objects.requireNonNull(amount, "amount == null");
		Objects.requireNonNull(currency, "currency == null");

		this.amount = amount;
		this.currency = currency;
	}

	public BigDecimal getAmount()
	{
		return amount;
	}

	public void setAmount(BigDecimal amount)
	{
		this.amount = amount;
	}

	public AvailableCurrency getCurrency()
	{
		return currency;
	}

	public void setCurrency(AvailableCurrency currency)
	{
		this.currency = currency;
	}
	
	public boolean isFreeOfCharge()
	{
		return BigDecimal.ZERO.compareTo(amount) == 0;
	}
	
	public boolean isRefund()
	{
		return amount.signum() < 0;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		MonetaryAmount that = (MonetaryAmount) o;

		if (amount != null ? !amount.equals(that.amount) : that.amount != null)
			return false;
		return !(currency != null ? !currency.equals(that.currency) : that.currency != null);

	}

	@Override
	public int hashCode()
	{
		int result = amount != null ? amount.hashCode() : 0;
		result = 31 * result + (currency != null ? currency.hashCode() : 0);
		return result;
	}

	/**
	 * Returns the formatted monetary amount.
	 *
	 * @return formatted monetary amount
	 */
	@Override
	public String toString()
	{
		return I18N.MONATARYAMOUNT_FORMAT.msg(amount, currency);
	}
	
}

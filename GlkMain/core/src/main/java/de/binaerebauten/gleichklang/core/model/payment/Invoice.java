package de.binaerebauten.gleichklang.core.model.payment;

import de.binaerebauten.gleichklang.core.model.BaseEntity;
import de.binaerebauten.gleichklang.core.model.BaseVersionedEntity;
import de.binaerebauten.gleichklang.core.model.user.User;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Represents an invoice of an user.
 */
@Entity
@Table(name = "invoice")
public class Invoice extends BaseVersionedEntity
{
	@NotNull
	@ManyToOne // default to load user eager is fine here!
	@JoinColumn(name = "user_id")
	private User user;

	@OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
	private Set<InvoiceItem> items = new HashSet<>();

	@OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
	private Set<AbstractPayment> payments = new HashSet<>();

	public User getUser()
	{
		return user;
	}

	public void setUser(User user)
	{
		this.user = user;
	}

	public Set<InvoiceItem> getItems()
	{
		return items;
	}

	public void setItems(Set<InvoiceItem> items)
	{
		this.items = items;
	}

	public Set<AbstractPayment> getPayments()
	{
		return payments;
	}

	public void setPayments(Set<AbstractPayment> payments)
	{
		this.payments = payments;
	}

	/**
	 * Returns the sum of the amounts of the items.
	 *
	 * @return the sum of the items
	 */
	public MonetaryAmount getAmount()
	{
		BigDecimal value = null;
		AvailableCurrency currency = null;

		for (InvoiceItem item : items)
		{
			MonetaryAmount amount = item.getAmount();

			if (value == null)
			{
				value = amount.getAmount();
				currency = amount.getCurrency();
			}
			else if (amount.getCurrency().equals(currency))
			{
				value.add(amount.getAmount());
			}
			else
			{
				String message = String.format("Can't add amounts with different currencies %s, %s",
						currency, amount.getCurrency());
				throw new IllegalStateException(message);
			}
		}

		return new MonetaryAmount(value, currency); // TODO @VK: Hier hatte ich eine NullPointer Excpetion, da value scheinbar null sein könnte.
	}

	/**
	 * Returns the optional newest payment of this invoice
	 *
	 * @return the optional newest payment
	 */
	public Optional<AbstractPayment> getNewestPayment()
	{
		return payments.stream().max(Comparator.comparing(BaseEntity::getCreateDate));
	}

	/**
	 * Returns the newest payment method.
	 *
	 * @return the optional newest payment method
	 */
	public PaymentMethod getNewestPaymentMethod()
	{
		return getNewestPayment().map(AbstractPayment::getMethod).orElse(null);
	}

	/**
	 * Returns the newest payment state for this invoice.
	 *
	 * @return the payment state of the newest payment or
	 * 	{@link PaymentState#UNKNOWN} if this invoice has no payment (yet).
	 */
	public PaymentState getNewestPaymentState()
	{
		return getNewestPayment()
				.map(AbstractPayment::getState)
				.orElse(PaymentState.UNKNOWN);
	}
	
	public Product getBaseProduct()
	{
		return getItems().stream().min(Comparator.comparing(BaseEntity::getCreateDate)).map(InvoiceItem::getProduct).orElse(null);
	}
}

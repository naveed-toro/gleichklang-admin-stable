package de.binaerebauten.gleichklang.core.model.payment;

import com.google.common.base.Preconditions;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static de.binaerebauten.gleichklang.core.model.payment.UpgradeType.DONATION;

/**
 * This entity represents an upgrade offer which can have different types.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@Entity
public class UpgradeOffer extends SubscriptionOffer
{
	private final static int DEFAULT_DURATION = 12;
	private final static int DEFAULT_DURATION_DONATION = 6;

	@XmlIDREF
	@NotNull
	@ManyToOne
	@JoinColumn(name = "auto_renewal_offer_id")
	private RenewalOffer autoRenewalOffer;

	@XmlAttribute
	@NotNull
	@Column(name = "upgrade_type")
	@Enumerated(EnumType.STRING)
	private UpgradeType upgradeType;

	@XmlElement(name = "subscriptionOffer")
	@XmlElementWrapper
	@XmlIDREF
	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "upgrade_offer_subscription_offer",
			joinColumns = { @JoinColumn(name = "upgrade_offer_id" )},
			inverseJoinColumns = { @JoinColumn(name = "subscription_offer_id" )})
	private Set<SubscriptionOffer> subscriptionOffers = new HashSet<>();

	/**
	 * Returns the auto renewal offer of this offer.
	 *
	 * @return the non-null auto renwal offer
	 */
	@Override
	public RenewalOffer getAutoRenewalOffer()
	{
		return autoRenewalOffer;
	}

	@Override
	public void setAutoRenewalOffer(RenewalOffer autoRenewalOffer)
	{
		this.autoRenewalOffer = autoRenewalOffer;
	}

	public UpgradeType getUpgradeType()
	{
		return upgradeType;
	}

	public void setUpgradeType(UpgradeType upgradeType)
	{
		this.upgradeType = upgradeType;
	}

	public Set<SubscriptionOffer> getSubscriptionOffers()
	{
		return subscriptionOffers;
	}

	public void setSubscriptionOffers(Set<SubscriptionOffer> subscriptionOffers)
	{
		this.subscriptionOffers = subscriptionOffers;
	}

	/**
	 * Calculates the price for this offer for the given date and the given subscription.
	 *
	 * @param date         the non-null date
	 * @param subscription the non-null subscription
	 * @return the amount for the given parameters
	 */
	public MonetaryAmount getAmount(LocalDateTime date, Subscription subscription)
	{
		Objects.requireNonNull(subscription, "subscription == null");

		switch (upgradeType)
		{
			case TARIFF_CHANGE:
				return getTariffChangePrice(subscription);
			case DONATION: // technically donation isn't really necessary
				return super.getAmount();
			case CATEGORY_EXTENSION:
				return getCategoryExtensionPrice(date, subscription);
		}

		throw new IllegalArgumentException("Unknown upgrade type:" + upgradeType);
	}

	private MonetaryAmount getCategoryExtensionPrice(LocalDateTime date, Subscription subscription)
	{
		long remainingDuration = getDurationUnit().chronoUnit.between(date, subscription.getEnd());

		Preconditions.checkState(remainingDuration >= 0, "remainingDuration < 0");
		Preconditions.checkState(DurationUnit.MONTHS.equals(getDurationUnit()), "durationUnit != MONTHS");
		
		// Round to the upper number of months (15 days -> 1 month)
		boolean isExactRemainingTime = date.plus(remainingDuration, getDurationUnit().chronoUnit).equals(subscription.getEnd());
		double duration = isExactRemainingTime ? remainingDuration : remainingDuration + 1;

		BigDecimal categoryExtensionAmount = getAmount().getAmount().multiply(new BigDecimal(duration / getDuration()));
		
		return new MonetaryAmount(categoryExtensionAmount, getAmount().getCurrency());
	}

	private MonetaryAmount getTariffChangePrice(Subscription subscription)
	{
		BigDecimal subscriptionOfferAmount = subscription.getOffer().getAmount().getAmount();
		BigDecimal tariffChangeAmount = getAmount().getAmount().subtract(subscriptionOfferAmount);

		return new MonetaryAmount(tariffChangeAmount, getAmount().getCurrency());
	}

	/**
	 * Creates an upgraded subscription from this offer and the given parameters.
	 *
	 * An upgraded subscription starts at the given begin date and ends at the end
	 * of the given current subscription.
	 *
	 * @param begin                  the non-null begin date, in production code {@link LocalDateTime#now()}
	 * @param currentSubscription    the non-null current subscription, subclasses may require a non-null current subscription
	 *
	 * @return an upgraded subscription
	 */
	@Override
	public Subscription createSubscription(LocalDateTime begin, Subscription currentSubscription)
	{
		Objects.requireNonNull(currentSubscription, "currentSubscription == null");

		final Subscription subscription = super.createSubscription(begin, currentSubscription);

		if (upgradeType != DONATION)
		{
			subscription.setEnd(currentSubscription.getEnd());
		}

		return subscription;
	}
	
	@Override
	public int getDefaultDuration()
	{
		return upgradeType != DONATION ? DEFAULT_DURATION : DEFAULT_DURATION_DONATION;
	}
	
	@Override
	public <T> T accept(ProductVisitor<T> visitor)
	{
		return visitor.visit(this);
	}
	
	@Override
	public ProductType getProductType()
	{
		return ProductType.UPGRADE_OFFER;
	}
}

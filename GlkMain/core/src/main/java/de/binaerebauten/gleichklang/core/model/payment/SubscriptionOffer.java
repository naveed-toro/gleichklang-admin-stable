package de.binaerebauten.gleichklang.core.model.payment;

import de.binaerebauten.gleichklang.core.model.payment.Subscription.SubscriptionState;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.*;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * This abstract class represents an offer for a subscription {@link Subscription}.
 */
@XmlTransient
@XmlSeeAlso({InitialSubscriptionOffer.class, RenewalOffer.class, UpgradeOffer.class})
@Entity
public abstract class SubscriptionOffer extends Product
{
	@XmlAttribute
	@NotNull
	@DecimalMin("1")
	private int duration;

	@XmlAttribute
	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "duration_unit")
	private DurationUnit durationUnit;

	@XmlElement(name = "category")
	@XmlElementWrapper
	@NotEmpty
	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "subscriptionOffer")
	private Set<SubscriptionOfferCategory> categories = new HashSet<>();

	@XmlAttribute
	@NotNull
	@Enumerated(EnumType.STRING)
	private Tariff tariff;

	public int getDuration()
	{
		return duration;
	}

	public void setDuration(int duration)
	{
		this.duration = duration;
	}

	public DurationUnit getDurationUnit()
	{
		return durationUnit;
	}

	public void setDurationUnit(DurationUnit durationUnit)
	{
		this.durationUnit = durationUnit;
	}

	public abstract RenewalOffer getAutoRenewalOffer();

	public abstract void setAutoRenewalOffer(RenewalOffer autoRenewalOffer);

	public Set<SubscriptionOfferCategory> getCategories()
	{
		return categories;
	}

	public void setCategories(Set<SubscriptionOfferCategory> categories)
	{
		this.categories = categories;
	}

	public Tariff getTariff()
	{
		return tariff;
	}

	public void setTariff(Tariff tariff)
	{
		this.tariff = tariff;
	}

	/**
	 * Creates a subscription from this offer and the given parameters.
	 *
	 * @param begin                  the non-null begin date, in production code {@link LocalDateTime#now()}
	 * @param currentSubscription    the nullable current subscription, subclasses may require a non-null current subscription
	 *
	 * @return the end date calculated from this object properties and the given parameters
	 */
	public Subscription createSubscription(LocalDateTime begin, Subscription currentSubscription)
	{
		Objects.requireNonNull(begin, "begin == null");

		ChronoUnit durationAsChronoUnit = durationUnit.chronoUnit;
		LocalDateTime end = durationAsChronoUnit.addTo(begin, duration);

		final Subscription subscription = new Subscription();

		subscription.setAutomaticRenewal(true);
		subscription.setOffer(this);
		subscription.setState(SubscriptionState.ACTIVE);

		subscription.setBegin(begin);
		subscription.setEnd(end);

		return subscription;
	}
	
	/**
	 * Default duration will be pre-selected by creating new products.
	 *
	 * @return default duration in months
	 */
	public abstract int getDefaultDuration();
}

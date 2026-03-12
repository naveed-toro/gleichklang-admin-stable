package de.binaerebauten.gleichklang.core.model.payment;

import com.google.common.base.Preconditions;
import de.binaerebauten.gleichklang.core.model.BaseVersionedEntity;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.model.user.UserActivityLog;
import de.binaerebauten.gleichklang.core.utils.DefaultEnumI18N;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "subscription")
public class Subscription extends BaseVersionedEntity
{
	/**
	 * Represents the state of a subscription {@link Subscription}.
	 *
	 * This state isn't persisted because it can be calculated from
	 * the current date and the {@link Subscription#end} of the newest
	 * subscription of the user.
	 */
	public enum SubscriptionState implements DefaultEnumI18N
	{
		/**
		 * Subscription is active and the user has access to all functions.
		 */
		ACTIVE,
		
		/**
		 * Subscription is not active
		 */
		PENDING,
		
		/**
		 * Subscription is in expiration period and will soon expire.
		 * User can only read and write messages and don't get any new recommendations.
		 * User has to purchase an new intial subscription offer.
		 */
		EXPIRING,
		
		/**
		 * User gets the status registration and cannot access services anymore.
		 */
		EXPIRED,
		
		/**
		 * Subscription was canceled and the member can't reactivate his account anymore.
		 */
		CANCELED,
		
		REPLACED
	}
	
	/**
	 * An user can have multiple subscriptions if he changed the subscription
	 * or if he canceled the subscription and later activated a new one.
	 */
	@NotNull
	@ManyToOne // default to load user eager is fine here!
	@JoinColumn(name = "user_id")
	private User user;
	
	/**
	 * The begin date of this subscription.
	 */
	@NotNull
	@Column
	private LocalDateTime begin;
	
	/**
	 * The end date of this subscription.
	 */
	@NotNull
	@Column
	private LocalDateTime end;
	
	/**
	 * The expiration date of this subscription.
	 */
	@NotNull
	@Column(name = "expiration_date")
	private LocalDateTime expirationDate;
	
	/**
	 * The offer of this subscription.
	 */
	@NotNull
	@ManyToOne // default to load offer eager is fine here!
	@JoinColumn(name = "offer_id")
	private SubscriptionOffer offer;
	
	@Column(name = "automatic_renewal")
	private boolean automaticRenewal = true;
	
	/**
	 * Transient attribute to store the persisted automatic renewal flag.
	 * See {@link #getUserActivityToLog()}
	 * Boolean is used to handle the initial creation of a subscription.
	 */
	private transient Boolean persistedAutomaticRenewal;
	
	/**
	 * This attribute has the type Boolean so that it can be set to null.
	 * It's used as a combined unique key
	 * together with {@link #user}. This prevents that an user has more then one
	 * current subscription at the same time.
	 */
	@Column
	private Boolean current = Boolean.TRUE;
	
	@NotNull
	@Enumerated(value = EnumType.STRING)
	private SubscriptionState state;
	
	public User getUser()
	{
		return user;
	}
	
	public void setUser(User user)
	{
		this.user = user;
	}
	
	public LocalDateTime getBegin()
	{
		return begin;
	}
	
	public void setBegin(LocalDateTime begin)
	{
		this.begin = begin;
	}
	
	public LocalDateTime getEnd()
	{
		return end;
	}
	
	public void setEnd(LocalDateTime end)
	{
		this.end = end;
	}
	
	public SubscriptionOffer getOffer()
	{
		return offer;
	}
	
	public void setOffer(SubscriptionOffer offer)
	{
		this.offer = offer;
	}
	
	public boolean isAutomaticRenewal()
	{
		return automaticRenewal;
	}
	
	public void setAutomaticRenewal(boolean automaticRenewal)
	{
		this.automaticRenewal = automaticRenewal;
	}
	
	public Boolean getCurrent()
	{
		return current;
	}
	
	/**
	 * Sets the current flag of this subscription.
	 *
	 * @param current true or null
	 * @throws IllegalArgumentException when trying to set this field to false.
	 */
	public void setCurrent(Boolean current)
	{
		Preconditions.checkArgument(Boolean.TRUE.equals(current) || current == null);
		
		this.current = current;
	}
	
	public LocalDateTime getExpirationDate()
	{
		return expirationDate;
	}
	
	public void setExpirationDate(LocalDateTime expirationDate)
	{
		this.expirationDate = expirationDate;
	}
	
	public SubscriptionState getState()
	{
		return state;
	}
	
	public void setState(SubscriptionState state)
	{
		this.state = state;
	}
	
	public boolean isAutomaticRenewalChanged()
	{
		return persistedAutomaticRenewal != null && persistedAutomaticRenewal != automaticRenewal;
	}
	
	@PostLoad
	public void storePersistedAutomaticRenewal()
	{
		persistedAutomaticRenewal = this.isAutomaticRenewal();
	}
}

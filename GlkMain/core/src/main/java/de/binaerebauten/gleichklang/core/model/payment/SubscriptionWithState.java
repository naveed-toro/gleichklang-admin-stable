package de.binaerebauten.gleichklang.core.model.payment;

import de.binaerebauten.gleichklang.core.model.payment.Subscription.SubscriptionState;

import java.util.Objects;

/**
 * Non persistent class to represent a subscription with a state.
 *
 * It's an immutable object because it's only used to view the subscription
 * with it's associated state.
 */
@Deprecated
public class SubscriptionWithState
{
	private final Subscription subscription;

	/**
	 * Creates a new subscription with state.
	 * @param subscription nullable subscription
	 */
	public SubscriptionWithState(Subscription subscription)
	{
		this.subscription = subscription;
	}

	public Subscription getSubscription()
	{
		return subscription;
	}

	public SubscriptionState getState()
	{
		return subscription != null ? subscription.getState() : SubscriptionState.PENDING;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		
		final SubscriptionWithState that = (SubscriptionWithState) o;
		
		return Objects.equals(subscription, that.subscription) && getState() == that.getState();
	}

	@Override
	public int hashCode()
	{
		int result = subscription != null ? subscription.hashCode() : 0;
		result = 31 * result + getState().hashCode();
		return result;
	}
	
	@Override
	public String toString()
	{
		return "Subscription=" + subscription + ", state=" + getState();
	}
}

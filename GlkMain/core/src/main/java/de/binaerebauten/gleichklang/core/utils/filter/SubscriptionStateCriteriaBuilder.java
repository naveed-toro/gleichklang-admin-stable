package de.binaerebauten.gleichklang.core.utils.filter;

import de.binaerebauten.gleichklang.core.model.payment.Subscription;
import de.binaerebauten.gleichklang.core.model.payment.Subscription.SubscriptionState;
import de.binaerebauten.gleichklang.core.model.payment.Subscription_;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * This class provides a JPA criteria builder for the state {@link SubscriptionState}
 * of a subscription {@link Subscription}.
 *
 * This allows the reuse of the logic.
 */
@Deprecated
public class SubscriptionStateCriteriaBuilder
{
	private final CriteriaBuilder cb;

	public SubscriptionStateCriteriaBuilder(CriteriaBuilder cb)
	{
		this.cb = Objects.requireNonNull(cb, "cb == null");
	}

	public Predicate build(final Root<Subscription> subscriptionRoot, SubscriptionState subscriptionState, LocalDateTime localDateTime)
	{
		Objects.requireNonNull(subscriptionRoot, "subscriptionRoot == null");
		Objects.requireNonNull(subscriptionState, "subscriptionState == null");
		
		return cb.and(
				cb.equal(subscriptionRoot.get(Subscription_.state), subscriptionState),
				cb.equal(subscriptionRoot.get(Subscription_.current), true));
	}
	
}

package de.binaerebauten.gleichklang.adminweb.view.filter;

import de.binaerebauten.gleichklang.core.model.payment.Subscription;
import de.binaerebauten.gleichklang.core.model.payment.Subscription_;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Filters {@link Subscription} entities with a date for which the subscription should be valid.
 */
public class SubscriptionValidFilter implements Specification<Subscription>
{
	private LocalDateTime date;

	/**
	 * Creates a new subscription filter which filters subscription entities if they're valid on the given date.
	 *
	 * @param date the date for which the subscriptions should be valid
	 */
	public SubscriptionValidFilter(LocalDate date)
	{
		Objects.requireNonNull(date, "date == null");

		this.date = date.atStartOfDay();
	}

	@Override
	public Predicate toPredicate(Root<Subscription> root, CriteriaQuery<?> query, CriteriaBuilder cb)
	{
		Path<LocalDateTime> beginPath = root.get(Subscription_.begin);
		Path<LocalDateTime> endPath = root.get(Subscription_.end);

		Predicate beginPredicate = cb.or(cb.isNull(beginPath), cb.lessThan(beginPath, date));
		Predicate endPredicate = cb.or(cb.isNull(endPath), cb.greaterThan(endPath, date));

		return cb.and(beginPredicate, endPredicate);
	}
}

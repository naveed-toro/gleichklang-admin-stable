package de.binaerebauten.gleichklang.core.view.filter;

import com.google.common.base.Strings;
import de.binaerebauten.gleichklang.core.model.payment.InitialSubscriptionOffer;
import de.binaerebauten.gleichklang.core.model.payment.InitialSubscriptionOffer_;
import de.binaerebauten.gleichklang.core.model.payment.Product.ProductType;
import de.binaerebauten.gleichklang.core.model.payment.SubscriptionOffer_;
import de.binaerebauten.gleichklang.core.model.payment.Tariff;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;

import javax.persistence.criteria.*;
import java.time.LocalDate;
import java.util.Set;

/**
 * An subscription offer filter for {@link InitialSubscriptionOffer} which
 * provides additional filtering of the offers via the given {@link #actionCode}.
 */
public class InitialSubscriptionOfferFilter
		extends SubscriptionOfferFilter<InitialSubscriptionOffer>
{
	private static final String SOCIAL_CODE_PREFIX = "SZ-";

	private final String actionCode;
	private final boolean isValidActionCode;

	/**
	 * Creates a new filter with the given parameters.
	 *
	 * @param date              the date for which the offers have to be valid, may be null
	 * @param categories        the categories of the offer
	 * @param actionCode        the action code that the offers must match, may be null
	 * @param isValidActionCode determines if the action code should be handled as invalid. An ivalid action code triggers special
	 *                          logic for showing the valid social or standard offers
	 */
	public InitialSubscriptionOfferFilter(LocalDate date, Set<RecommendationCategory> categories, String actionCode, boolean isValidActionCode)
	{
		super(date, categories, ProductType.INITIAL_SUBSCRIPTION_OFFER);
		this.actionCode = actionCode;
		this.isValidActionCode = isValidActionCode;
	}

	/**
	 * Creates a new copy with the given action code.
	 *
	 * @param actionCode        the action code that the offers must match, may be null
	 * @param isValidActionCode determines if the action code should be handled as invalid. An ivalid action code triggers special
	 *                          logic for showing the valid social or standard offers
	 *
	 * @return new filter copy with the given action code
	 */
	public InitialSubscriptionOfferFilter withActionCode(String actionCode, boolean isValidActionCode)
	{
		return new InitialSubscriptionOfferFilter(getDate(), getCategories(), actionCode, isValidActionCode);
	}

	public String getActionCode()
	{
		return actionCode;
	}

	@Override
	public Predicate toPredicate(Root<InitialSubscriptionOffer> root, CriteriaQuery<?> query, CriteriaBuilder cb)
	{
		Predicate subscriptionOfferFilter = super.toPredicate(root, query, cb);

		Path<String> actionCodePath = root.get(InitialSubscriptionOffer_.actionCode);
		Path<Tariff> tariffPath = root.get(SubscriptionOffer_.tariff);

		Predicate actionCodeFilter;

		Predicate standardTariffFilter = cb.and(cb.isNull(actionCodePath), cb.equal(tariffPath, Tariff.STANDARD));
		if (Strings.isNullOrEmpty(actionCode))
		{
			actionCodeFilter = standardTariffFilter;
		}
		else
		{
			if (isValidActionCode)
			{
				actionCodeFilter = cb.equal(actionCodePath, actionCode);
			}
			else
			{
				if (actionCode.startsWith(SOCIAL_CODE_PREFIX))
				{
					Predicate socialTariffFilter = cb.equal(tariffPath, Tariff.SOCIAL);
					actionCodeFilter = cb.or(socialTariffFilter, standardTariffFilter);
				}
				else
				{
					actionCodeFilter = standardTariffFilter;
				}
			}
		}

		return cb.and(subscriptionOfferFilter, actionCodeFilter);
	}
}

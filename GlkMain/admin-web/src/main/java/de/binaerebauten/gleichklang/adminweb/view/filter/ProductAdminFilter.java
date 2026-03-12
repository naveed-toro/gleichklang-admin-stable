package de.binaerebauten.gleichklang.adminweb.view.filter;

import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import de.binaerebauten.gleichklang.core.model.payment.Product;
import de.binaerebauten.gleichklang.core.model.payment.Product.ProductType;
import de.binaerebauten.gleichklang.core.model.payment.Product_;
import de.binaerebauten.gleichklang.core.view.filter.ProductValidFilter;

import javax.persistence.criteria.*;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This filter extends {@link ProductValidFilter} with prefix filtering for the name and the action code.
 *
 * @param <T> the product type
 */
public class ProductAdminFilter<T extends Product> extends ProductValidFilter<T>
{
	private final String namePrefix;

	private final String actionCodePrefix;

	/**
	 * Creates an empty filter.
	 */
	public ProductAdminFilter()
	{
		this(null, null, Collections.EMPTY_SET);
	}

	/**
	 * Creates a new filter which filters products which have the specified name as prefix.
	 *
	 * @param namePrefix the name prefix used to filter the products, may be null
	 * @param types      restricts the types
	 */
	public ProductAdminFilter(LocalDate date, String namePrefix, Set<ProductType> types)
	{
		super(date, types);
		this.namePrefix = namePrefix;
		this.actionCodePrefix = null;
	}

	/**
	 * Creates a new filter which filters products which have the specified name and action code as prefix.
	 *
	 * @param namePrefix       the name prefix used to filter the products, may be null
	 * @param actionCodePrefix the action code prefix used to filter the products,
	 *                         null or an empty string disable the action code prefix filtering
	 * @param types            restricts the types
	 */
	public ProductAdminFilter(LocalDate date, String namePrefix, String actionCodePrefix, Set<ProductType> types)
	{
		super(date, types);
		this.namePrefix = namePrefix;
		this.actionCodePrefix = actionCodePrefix;
	}

	/**
	 * Creates a new filter with the given date.
	 *
	 * @param date new filter date
	 * @return new filter with the given date
	 */
	public ProductAdminFilter<T> withDate(LocalDate date)
	{
		return new ProductAdminFilter(date, this.namePrefix, getTypes());
	}

	/**
	 * Creates a new filter with the given action code prefix.
	 *
	 * @param actionCodePrefix new action code prefix
	 * @return new filter with the given action code
	 */
	public ProductAdminFilter<T> withActionCodePrefix(String actionCodePrefix)
	{
		return new ProductAdminFilter(getDate(), this.namePrefix, actionCodePrefix, getTypes());
	}

	/**
	 * Creates a new filter with the given name prefix.
	 *
	 * @param namePrefix new name prefix
	 * @return new filter with the given action code
	 */
	public ProductAdminFilter<T> withNamePrefix(String namePrefix)
	{
		return new ProductAdminFilter(getDate(), namePrefix, getTypes());
	}

	/**
	 * Creates a new filter restricted to the given types.
	 *
	 * @param types new types
	 * @return new filter restricted to the given types
	 */
	public ProductAdminFilter<T> withTypes(ProductType... types)
	{
		return new ProductAdminFilter(getDate(), this.namePrefix, Arrays.stream(types).collect(Collectors.toSet()));
	}

	/**
	 * Creates a new filter restricted to the given types.
	 *
	 * @param types new types
	 * @return new filter restricted to the given types
	 */
	public ProductAdminFilter<T> withTypes(Set<ProductType> types)
	{
		return withTypes(Iterables.toArray(types, ProductType.class));
	}

	public String getNamePrefix()
	{
		return namePrefix;
	}

	public String getActionCodePrefix()
	{
		return actionCodePrefix;
	}

	@Override
	public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb)
	{
		Predicate productValidFilter = super.toPredicate(root, query, cb);
		List<Predicate> predicates = Lists.newArrayList(productValidFilter);

		if (!Strings.isNullOrEmpty(namePrefix))
		{
			Predicate nameLike = cb.like(root.get(Product_.name), namePrefix + "%");

			predicates.add(nameLike);
		}

		if (!Strings.isNullOrEmpty(actionCodePrefix))
		{
			Path<String> actionCodePath =
					// getName is safe here and is used to avoid type errors
					root.get(Product_.actionCode);
			Predicate actionCodeLike = cb.like(actionCodePath, actionCodePrefix + "%");

			Predicate actionCodeFilter = cb.and(cb.isNotNull(actionCodePath), actionCodeLike);

			predicates.add(actionCodeFilter);
		}

		return cb.and(Iterables.toArray(predicates, Predicate.class));
	}
}

package de.binaerebauten.gleichklang.core.view.filter;

import com.google.common.collect.Iterables;
import de.binaerebauten.gleichklang.core.model.payment.Product;
import de.binaerebauten.gleichklang.core.model.payment.Product.ProductType;
import de.binaerebauten.gleichklang.core.model.payment.Product_;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Filters {@link Product} entities with a date for which the product should be valid.
 * The filter can also be restricted to sub types of product.
 *
 * @param <T> the product type
 */
public class ProductValidFilter<T extends Product> implements Specification<T>
{
	private final LocalDate date;

	private final Set<ProductType> types;

	/**
	 * Creates an empty filter.
	 */
	public ProductValidFilter()
	{
		this(null, Collections.EMPTY_SET);
	}
	
	/**
	 * Creates a new product filter which filters product entities.
	 *
	 * @param types restricts the set of the result to these sub types of Product
	 */
	public ProductValidFilter(ProductType... types)
	{
		this(null, Arrays.stream(types).collect(Collectors.toSet()));
	}

	/**
	 * Creates a new product filter which filters product entities if they're valid on the given date.
	 *
	 * @param date  the date for which the products should be valid, may be null
	 * @param types restricts the set of the result to these sub types of Product
	 */
	public ProductValidFilter(LocalDate date, ProductType... types)
	{
		this(date, Arrays.stream(types).collect(Collectors.toSet()));
	}

	public ProductValidFilter(LocalDate date, Set<ProductType> types)
	{
		this.date = date;
		this.types = types;
	}

	/**
	 * Creates a new filter restricted to the given types.
	 *
	 * @param types new types
	 * @return new filter restricted to the given types
	 */
	public ProductValidFilter<T> withTypes(ProductType... types)
	{
		return new ProductValidFilter(getDate(), types);
	}

	/**
	 * Creates a new filter restricted to the given types.
	 *
	 * @param types new types
	 * @return new filter restricted to the given types
	 */
	public ProductValidFilter<T> withTypes(Set<ProductType> types)
	{
		return new ProductValidFilter(getDate(), types);
	}

	/**
	 * Creates a new filter with the given date.
	 *
	 * @param date new filter date
	 * @return new filter with the given date
	 */
	public ProductValidFilter<T> withDate(LocalDate date)
	{
		return new ProductValidFilter(date, getTypes());
	}

	public LocalDate getDate()
	{
		return date;
	}

	public Set<ProductType> getTypes()
	{
		return types;
	}

	@Override
	public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb)
	{
		query.distinct(true);

		List<Predicate> predicates = new ArrayList<>();

		if (types.size() > 0)
		{
			Set<Class<? extends Product>> types = this.types.stream().map(ProductType::getProductClass).collect(Collectors.toSet());
			Predicate typeSelect = root.type().in(types);
			predicates.add(typeSelect);
		}

		if (date != null)
		{
			LocalDateTime atStartOfDay = date.atStartOfDay();

			Path<LocalDateTime> beginPath = root.get(Product_.begin);
			Predicate beginPredicate = cb.lessThanOrEqualTo(beginPath, atStartOfDay);
			predicates.add(beginPredicate);

			Path<LocalDateTime> endPath = root.get(Product_.end);
			Predicate endPredicate = cb.or(cb.isNull(endPath), cb.greaterThanOrEqualTo(endPath, atStartOfDay));
			predicates.add(endPredicate);
		}

		return cb.and(Iterables.toArray(predicates, Predicate.class));
	}
}

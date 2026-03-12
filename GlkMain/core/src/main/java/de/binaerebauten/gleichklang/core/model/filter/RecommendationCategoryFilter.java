package de.binaerebauten.gleichklang.core.model.filter;

import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.utils.filter.FilterVisitor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

/**
 * This filter filters users based on the recommendation category that they are subscribed to.
 *
 * This means that the filter uses to the current {@link de.binaerebauten.gleichklang.core.model.payment.Subscription#current}
 * subscription offer {@link de.binaerebauten.gleichklang.core.model.payment.Subscription#offer} of an user.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@Entity
public class RecommendationCategoryFilter extends EnumFilter<RecommendationCategory>
{
    @XmlAttribute
	@Column(name = "enum_value")
	@Enumerated(EnumType.STRING)
	@NotNull
	private RecommendationCategory enumValue;

	@Override
	public RecommendationCategory getEnumValue()
	{
		return enumValue;
	}

	@Override
	public void setEnumValue(RecommendationCategory enumValue)
	{
		this.enumValue = enumValue;
	}

	@Override
	public <T> T accept(FilterVisitor<T> filterVisitor)
	{
		return filterVisitor.visit(this);
	}
}

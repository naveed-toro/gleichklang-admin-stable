package de.binaerebauten.gleichklang.core.model.filter;

import de.binaerebauten.gleichklang.core.utils.filter.FilterVisitor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlIDREF;

@XmlAccessorType(XmlAccessType.FIELD)
@Entity
public class BinaryOperatorFilter extends AbstractFilter
{
	public enum BinaryOperator
	{
		UNION,
		INTERSECTION
	}

	@XmlIDREF
	@ManyToOne
	@JoinColumn(name = "left_filter_id")
	@NotNull
	private AbstractFilter leftFilter;

	@XmlIDREF
	@ManyToOne
	@JoinColumn(name = "right_filter_id")
	@NotNull
	private AbstractFilter rightFilter;

	@XmlAttribute
	@Column(name = "binary_operator")
	@Enumerated(EnumType.STRING)
	@NotNull
	private BinaryOperator binaryOperator = BinaryOperator.INTERSECTION;

	public AbstractFilter getLeftFilter()
	{
		return leftFilter;
	}

	public void setLeftFilter(AbstractFilter leftFilter)
	{
		this.leftFilter = leftFilter;
	}

	public AbstractFilter getRightFilter()
	{
		return rightFilter;
	}

	public void setRightFilter(AbstractFilter rightFilter)
	{
		this.rightFilter = rightFilter;
	}

	public BinaryOperator getBinaryOperator()
	{
		return binaryOperator;
	}

	public void setBinaryOperator(BinaryOperator binaryOperator)
	{
		this.binaryOperator = binaryOperator;
	}

	@Override
	public <T> T accept(FilterVisitor<T> filterVisitor)
	{
		return filterVisitor.visit(this);
	}
}

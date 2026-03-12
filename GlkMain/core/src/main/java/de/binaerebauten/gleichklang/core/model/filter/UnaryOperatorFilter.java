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
public class UnaryOperatorFilter extends AbstractFilter
{
	public enum UnaryOperator
	{
		NOT
	}

	@XmlIDREF
	@ManyToOne
	@JoinColumn(name = "single_filter_id")
	@NotNull
	private AbstractFilter filter;

	@XmlAttribute
	@Column(name = "unary_operator")
	@Enumerated(EnumType.STRING)
	@NotNull
	private UnaryOperator unaryOperator = UnaryOperator.NOT;

	public AbstractFilter getFilter()
	{
		return filter;
	}

	public void setFilter(AbstractFilter filter)
	{
		this.filter = filter;
	}

	@Transient
	public boolean audioNotFrindShipPartnerFilter=false;

	public boolean isAudioNotFrindShipPartnerFilter() {
		return audioNotFrindShipPartnerFilter;
	}

	public void setAudioNotFrindShipPartnerFilter(boolean audioNotFrindShipPartnerFilter) {
		this.audioNotFrindShipPartnerFilter = audioNotFrindShipPartnerFilter;
	}

	public UnaryOperator getUnaryOperator()
	{
		return unaryOperator;
	}

	public void setUnaryOperator(UnaryOperator unaryOperator)
	{
		this.unaryOperator = unaryOperator;
	}

	@Override
	public <T> T accept(FilterVisitor<T> filterVisitor)
	{
		return filterVisitor.visit(this);
	}
}

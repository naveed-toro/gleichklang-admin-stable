package de.binaerebauten.gleichklang.core.model.filter;

import de.binaerebauten.gleichklang.core.model.locatable.Zip;
import de.binaerebauten.gleichklang.core.utils.filter.FilterVisitor;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlIDREF;

@XmlAccessorType(XmlAccessType.FIELD)
@Entity
public class ProximityFilter extends UserFilter
{
	@XmlAttribute
	@Min(1)
	@NotNull
	private int radius;

	@XmlIDREF
	@ManyToOne
	@JoinColumn(name = "zip_id")
	@NotNull
	private Zip zip;

	public int getRadius()
	{
		return radius;
	}

	public void setRadius(int radius)
	{
		this.radius = radius;
	}

	public Zip getZip()
	{
		return zip;
	}

	public void setZip(Zip zip)
	{
		this.zip = zip;
	}

	@Override
	public <T> T accept(FilterVisitor<T> filterVisitor)
	{
		return filterVisitor.visit(this);
	}

	@Override
	public String getName()
	{
		return "Radius: " + getRadius();
	}
}

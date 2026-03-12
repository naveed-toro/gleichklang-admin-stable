package de.binaerebauten.gleichklang.core.model.filter;

import de.binaerebauten.gleichklang.core.model.locatable.LocatableEntity;
import de.binaerebauten.gleichklang.core.utils.filter.FilterVisitor;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlIDREF;

@XmlAccessorType(XmlAccessType.FIELD)
@Entity
public class RegionFilter extends UserFilter
{
    @XmlIDREF
    @XmlAttribute
	@ManyToOne
	@JoinColumn(name = "locatable_id")
	@NotNull
	private LocatableEntity locatableEntity;

	public LocatableEntity getLocatableEntity()
	{
		return locatableEntity;
	}

	public void setLocatableEntity(LocatableEntity locatableEntity)
	{
		this.locatableEntity = locatableEntity;
	}

	@Override
	public <T> T accept(FilterVisitor<T> filterVisitor)
	{
		return filterVisitor.visit(this);
	}

	@Override
	public String getName()
	{
		final UserFilterType userFilterType = UserFilterType.valueOf(getClass());
		final String userFilterTypeStr = userFilterType == null ? getClass().getSimpleName() : userFilterType.toString();

		return userFilterTypeStr + ": " + getLocatableEntity().getName();
	}
}

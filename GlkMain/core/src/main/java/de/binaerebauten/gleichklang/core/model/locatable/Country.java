package de.binaerebauten.gleichklang.core.model.locatable;

import de.binaerebauten.gleichklang.core.model.I18NEntity.BaseName;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD)
@Entity
public class Country extends LocatableEntity
{
	@XmlAttribute
	@Column(name = "sort_order")
	private Integer sortOrder;


	@Override
	protected BaseName doGetBaseName()
	{
		return BaseName.COUNTRY;
	}

	// TODO @MW please add separate column for country code
	public String getCountryCode(){
		return getI18nKey();
	}
	
	@Override
	public Continent getParent()
	{
		return (Continent) super.getParent();
	}

	public Integer getSortOrder()
	{
		return sortOrder;
	}

	public void setSortOrder(Integer sortOrder)
	{
		this.sortOrder = sortOrder;
	}
}

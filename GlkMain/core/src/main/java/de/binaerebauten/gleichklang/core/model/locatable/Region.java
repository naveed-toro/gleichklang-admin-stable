package de.binaerebauten.gleichklang.core.model.locatable;

import de.binaerebauten.gleichklang.core.model.I18NEntity.BaseName;

import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.FIELD)
@Entity
public class Region extends LocatableEntity
{
	@Override
	protected BaseName doGetBaseName()
	{
		return BaseName.REGION;
	}
	
	@Override
	public Country getParent()
	{
		return (Country) super.getParent();
	}
}

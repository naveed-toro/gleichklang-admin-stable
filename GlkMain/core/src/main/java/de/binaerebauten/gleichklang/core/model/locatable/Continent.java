package de.binaerebauten.gleichklang.core.model.locatable;

import de.binaerebauten.gleichklang.core.model.I18NEntity.BaseName;

import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.FIELD)
@Entity
public class Continent extends LocatableEntity
{
	@Override
	protected BaseName doGetBaseName()
	{
		return BaseName.CONTINENT;
	}
	
	@Override
	public LocatableEntity getParent()
	{
		// TODO @FH throw exception
		return null;
	}
}

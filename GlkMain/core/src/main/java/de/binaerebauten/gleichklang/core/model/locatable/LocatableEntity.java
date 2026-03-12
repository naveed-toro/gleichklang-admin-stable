package de.binaerebauten.gleichklang.core.model.locatable;

import de.binaerebauten.gleichklang.core.model.LocalizedEntity;
import de.binaerebauten.gleichklang.core.model.SortableEntity;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;

@XmlTransient
@XmlSeeAlso({ Continent.class, Country.class, Region.class, Zip.class })
@Table(name = "locatable")
@Entity
public abstract class LocatableEntity extends LocalizedEntity
{
	@XmlIDREF
	@XmlAttribute
	@ManyToOne(fetch = FetchType.EAGER)
	private LocatableEntity parent;
	
	public LocatableEntity getParent()
	{
		return parent;
	}
	
	public void setParent(LocatableEntity parent)
	{
		this.parent = parent;
	}
	
	@Override
	public String getName()
	{
		return msg(doGetBaseName());
	}
}

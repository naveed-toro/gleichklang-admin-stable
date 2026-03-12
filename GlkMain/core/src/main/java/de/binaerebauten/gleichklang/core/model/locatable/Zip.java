package de.binaerebauten.gleichklang.core.model.locatable;

import de.binaerebauten.gleichklang.core.model.I18NEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
@Entity
public class Zip extends LocatableEntity
{
	@XmlAttribute
	@Column
	private String zip;
	
	@XmlIDREF
	@XmlAttribute
	@NotNull
	@ManyToOne(fetch = FetchType.EAGER)
	private Region region;
	
	@XmlAttribute
	@Column
	private Double latitude;
	
	@XmlAttribute
	@Column
	private Double longitude;
	
	@XmlID
	@XmlAttribute(name = "uniqueKey")
	@Override
	public String getUniqueXmlKey()
	{
		return this.getClass().getSimpleName() + "_" + region.getUniqueXmlKey() + "_" + zip;
	}
	
	public String getZip()
	{
		return zip;
	}
	
	public void setZip(String zip)
	{
		this.zip = zip;
	}
	
	public Double getLatitude()
	{
		return latitude;
	}
	
	public void setLatitude(Double latitude)
	{
		this.latitude = latitude;
	}
	
	public Double getLongitude()
	{
		return longitude;
	}
	
	public void setLongitude(Double longitude)
	{
		this.longitude = longitude;
	}
	
	public Region getRegion()
	{
		return region;
	}
	
	public void setRegion(Region region)
	{
		this.region = region;
	}
	
	@Override
	protected I18NEntity.BaseName doGetBaseName()
	{
		return I18NEntity.BaseName.NONE;
	}
	
	@Override
	public String getName()
	{
		return zip;
	}
	
	@Override
	public Country getParent()
	{
		return (Country) super.getParent();
	}
}

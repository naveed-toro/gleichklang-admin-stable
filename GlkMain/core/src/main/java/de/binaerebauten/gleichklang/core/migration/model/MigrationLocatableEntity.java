package de.binaerebauten.gleichklang.core.migration.model;

public class MigrationLocatableEntity extends MigrationLocalisedBaseEntity
{
	private String zip;
	private Double latitude;
	private Double longitude;
	private MigrationLocalisedBaseEntity parent;
	private Integer sortOrder;
	private LOCATABLE_TYPE type;
	private Long parentId;
	private String regionName;
	private Long regionId;

	public MigrationLocatableEntity(LOCATABLE_TYPE locatableType,
			Integer sortOrder){
		this.setType(locatableType);
		this.setSortOrder(sortOrder);
	}

	public void setName(String name){
		if(type.equals(LOCATABLE_TYPE.Zip)){
			this.setZip(name);
		}
		else{
			this.setLegacyId(name);
			this.setI18nKey(name);
		}
	}

	public Integer getSortOrder()
	{
		return sortOrder;
	}

	public void setSortOrder(Integer sortOrder)
	{
		this.sortOrder = sortOrder;
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

	public MigrationLocalisedBaseEntity getParent()
	{
		return parent;
	}

	public void setParent(MigrationLocalisedBaseEntity parent)
	{
		this.parent = parent;
	}

	public LOCATABLE_TYPE getType()
	{
		return type;
	}

	public void setType(LOCATABLE_TYPE type)
	{
		this.type = type;
	}

	public Long getParentId()
	{
		return parentId;
	}

	public void setParentId(Long parentId)
	{
		this.parentId = parentId;
	}

	public String getRegionName()
	{
		return regionName;
	}

	public void setRegionName(String regionName)
	{
		this.regionName = regionName;
	}

	public Long getRegionId()
	{
		return regionId;
	}

	public void setRegionId(Long regionId)
	{
		this.regionId = regionId;
	}

	public enum LOCATABLE_TYPE
	{
		Continent,
		Country,
		Region,
		Zip
	}
}

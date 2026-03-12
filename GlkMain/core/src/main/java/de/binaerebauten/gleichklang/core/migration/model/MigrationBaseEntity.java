package de.binaerebauten.gleichklang.core.migration.model;

import java.time.LocalDateTime;
import java.util.Objects;

public abstract class MigrationBaseEntity
{
	protected LocalDateTime createDate;
	protected LocalDateTime changeDate;

	private Long id;
	private String legacyId;
	
	public Long getId()
	{
		return id;
	}
	
	public void setId(Long id)
	{
		this.id = id;
	}
	
	protected void onCreate()
	{
		createDate = LocalDateTime.now();
	}
	
	protected void onUpdate()
	{
		changeDate = LocalDateTime.now();
	}
	
	public LocalDateTime getCreateDate()
	{
		return createDate == null ? LocalDateTime.now() : createDate;
	}
	
	public void setCreateDate(LocalDateTime createDate)
	{
		this.createDate = createDate;
	}
	
	public LocalDateTime getChangeDate()
	{
		return changeDate == null ? LocalDateTime.now() : changeDate;
	}
	
	public void setChangeDate(LocalDateTime changeDate)
	{
		this.changeDate = changeDate;
	}

	public String getLegacyId()
	{
		return legacyId;
	}

	public void setLegacyId(String legacyId)
	{
		this.legacyId = legacyId;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		
		final MigrationBaseEntity other = (MigrationBaseEntity) obj;
		return Objects.equals(id, other.getId());
	}
	
	@Override
	public int hashCode()
	{
		if (id == null)
			return super.hashCode();
		return Objects.hash(getClass(), id);
	}
}

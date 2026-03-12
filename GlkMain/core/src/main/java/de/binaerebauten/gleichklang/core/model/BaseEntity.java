package de.binaerebauten.gleichklang.core.model;

import de.binaerebauten.gleichklang.core.security.EntityContentSanitizer;
import de.binaerebauten.gleichklang.core.utils.XmlLocalDateTimeAdapter;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.LocalDateTime;
import java.util.Objects;

@XmlTransient
@MappedSuperclass
@EntityListeners(EntityContentSanitizer.class)
public abstract class BaseEntity implements IdEntity<Long>
{
	@XmlAttribute
	@XmlJavaTypeAdapter(XmlLocalDateTimeAdapter.class)
	@Column(name = "create_date")
	protected LocalDateTime createDate;
	
	@XmlTransient
	@Column(name = "change_date")
	protected LocalDateTime changeDate;
	
	@XmlTransient
	@Id
	@GeneratedValue
	@Column(unique = true, nullable = false)
	private Long id;
	
	public Long getId()
	{
		return id;
	}
	
	public void setId(Long id)
	{
		this.id = id;
	}
	
	@PrePersist
	protected void onCreate()
	{
		if (createDate == null)
			createDate = LocalDateTime.now();
	}
	
	@PreUpdate
	protected void onUpdate()
	{
		changeDate = LocalDateTime.now();
	}
	
	public LocalDateTime getCreateDate()
	{
		return createDate;
	}
	
	public void setCreateDate(LocalDateTime createDate)
	{
		this.createDate = createDate;
	}
	
	public LocalDateTime getChangeDate()
	{
		return changeDate;
	}
	
	public void setChangeDate(LocalDateTime changeDate)
	{
		this.changeDate = changeDate;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		
		final BaseEntity other = (BaseEntity) obj;
		return Objects.equals(id, other.getId());
	}
	
	@Override
	public int hashCode()
	{
		if (id == null) return super.hashCode();
		return Objects.hash(getClass(), id);
	}
	
	@Override
	public String toString()
	{
		return super.toString();
	}
}

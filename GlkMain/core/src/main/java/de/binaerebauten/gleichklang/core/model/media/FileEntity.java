package de.binaerebauten.gleichklang.core.model.media;

import de.binaerebauten.gleichklang.core.model.BaseEntity;

import javax.persistence.*;

@Entity
@Table(name = "file")
public class FileEntity extends BaseEntity
{
	public enum FileType
	{
		IMAGE,
		VIDEO
	}
	
	@Column
	private String name;

	@Column
	@Enumerated(EnumType.STRING)
	private FileType type;

	@Column
	private String description;
	
	public String getName()
	{
		return name;
	}
	
	public void setName(String name)
	{
		this.name = name;
	}
	
	public FileType getType()
	{
		return type;
	}
	
	public void setType(FileType type)
	{
		this.type = type;
	}

	public String getDescription() {return this.description;}

	public void setDescription(String description) {this.description = description;}
}

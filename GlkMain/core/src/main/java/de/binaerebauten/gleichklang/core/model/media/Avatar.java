package de.binaerebauten.gleichklang.core.model.media;

import de.binaerebauten.gleichklang.core.model.BaseEntity;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.model.user.User;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "avatar")
public class Avatar extends BaseEntity
{
	@NotNull
	@ManyToOne
	private User user;

	@NotNull
	@Enumerated(EnumType.STRING)
	private RecommendationCategory category;

	@NotNull
	@ManyToOne
	private FileEntity file;

	@OneToOne
	private FileEntity thumbnail;

	@OneToOne
	private FileEntity mediafile;

	public User getUser()
	{
		return user;
	}

	public void setUser(User user)
	{
		this.user = user;
	}

	public RecommendationCategory getCategory()
	{
		return category;
	}

	public void setCategory(RecommendationCategory category)
	{
		this.category = category;
	}

	public FileEntity getFile()
	{
		return file;
	}

	public void setFile(FileEntity file)
	{
		this.file = file;
	}

	public FileEntity getThumbnail()
	{
		return thumbnail;
	}

	public void setThumbnail(FileEntity file)
	{
		this.thumbnail = file;
	}

	public FileEntity getMediafile()
	{
		return mediafile;
	}

	public void setMediafile(FileEntity file)
	{
		this.mediafile = file;
	}
}

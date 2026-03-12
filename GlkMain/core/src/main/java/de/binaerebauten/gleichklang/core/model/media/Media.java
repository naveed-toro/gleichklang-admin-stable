package de.binaerebauten.gleichklang.core.model.media;

import de.binaerebauten.gleichklang.core.model.BaseEntity;
import de.binaerebauten.gleichklang.core.model.DeletableEntity;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "media")
public class Media extends BaseEntity implements DeletableEntity<Long>
{
	@NotNull
	@ManyToOne
	@JoinColumn(name = "media_gallery_id")
	private MediaGallery mediaGallery;

	@NotNull
	@ManyToOne
	private FileEntity file;

	private boolean deleted;

	public MediaGallery getMediaGallery()
	{
		return mediaGallery;
	}

	public void setMediaGallery(MediaGallery mediaGallery)
	{
		this.mediaGallery = mediaGallery;
	}

	public FileEntity getFile()
	{
		return file;
	}

	public void setFile(FileEntity file)
	{
		this.file = file;
	}

	@Override
	public boolean isDeleted()
	{
		return deleted;
	}

	@Override
	public void setDeleted(boolean deleted)
	{
		this.deleted = deleted;
	}
}

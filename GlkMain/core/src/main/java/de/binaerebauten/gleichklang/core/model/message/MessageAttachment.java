package de.binaerebauten.gleichklang.core.model.message;

import de.binaerebauten.gleichklang.core.model.BaseEntity;
import de.binaerebauten.gleichklang.core.model.media.FileEntity;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "message_attachment")
public class MessageAttachment extends BaseEntity
{
	@NotNull
	@ManyToOne
	private FileEntity file;

	@NotNull
	@ManyToOne
	private Message message;
	
	public FileEntity getFile()
	{
		return file;
	}
	
	public void setFile(FileEntity file)
	{
		this.file = file;
	}
	
	public Message getMessage()
	{
		return message;
	}
	
	public void setMessage(Message message)
	{
		this.message = message;
	}
}

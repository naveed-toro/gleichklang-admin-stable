package de.binaerebauten.gleichklang.core.service.file;

import de.binaerebauten.gleichklang.core.model.media.FileEntity;
import de.binaerebauten.gleichklang.core.model.message.Message;
import de.binaerebauten.gleichklang.core.model.message.MessageAttachment;

import java.nio.file.Path;
import java.util.Objects;

public class MessageUploadFile extends AbstractUploadFile
{
	public static final String NAME = "name";

	private MessageAttachment messageAttachment;

	public MessageUploadFile(MessageAttachment messageAttachment, FileService fileService)
	{
		super(fileService);

		Objects.requireNonNull(messageAttachment);
		Objects.requireNonNull(messageAttachment.getFile());

		this.messageAttachment = messageAttachment;
	}

	public MessageUploadFile(Message message, FileService fileService)
	{
		super(fileService);

		Objects.requireNonNull(message);

		messageAttachment = new MessageAttachment();
		messageAttachment.setMessage(message);
		messageAttachment.setFile(new FileEntity());
	}

	public MessageAttachment getMessageAttachment()
	{
		return messageAttachment;
	}

	@Override
	public FileEntity getFileEntity()
	{
		return messageAttachment.getFile();
	}

	@Override
	protected void saveConcrete()
	{
		messageAttachment = fileService.saveMessageUploadFile(this);
	}
	
	@Override
	public void delete()
	{
		fileService.deleteMessageUploadFile(this);
	}
	
	@Override
	protected boolean afterUploadSucceeded(Path path)
	{
		return true;
	}
	
	@Override
	public String getFilename()
	{
		if(getFileEntity() != null && getFileEntity().getName() != null) return getFileEntity().getName();
		if(path != null) return path.getFileName().toString();
		
		return null;
	}
}

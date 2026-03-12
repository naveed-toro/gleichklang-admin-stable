package de.binaerebauten.gleichklang.core.repository.message;

import de.binaerebauten.gleichklang.core.model.message.MessageAttachment;
import de.binaerebauten.gleichklang.core.repository.AbstractRepositoryTest;
import de.binaerebauten.gleichklang.core.utils.DefaultEntityFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Collections;

public class MessageAttachmentRepositoryTest extends AbstractRepositoryTest<MessageAttachment>
{
	@Autowired
	private MessageAttachmentRepository messageAttachmentRepository;

	@Autowired
	private DefaultEntityFactory entityFactory;

	public MessageAttachmentRepositoryTest()
	{
	}

	@Override
	protected Collection<MessageAttachment> getPersistedEntities()
	{
		return Collections.singletonList(entityFactory.persistDefaultMessageAttachment());
	}

	@Override
	protected JpaRepository<MessageAttachment, Long> getRepository()
	{
		return messageAttachmentRepository;
	}
}

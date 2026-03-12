package de.binaerebauten.gleichklang.core.repository.mail;

import de.binaerebauten.gleichklang.core.model.mail.UndeliverableMail;
import de.binaerebauten.gleichklang.core.model.mail.UndeliverableMailReason;
import de.binaerebauten.gleichklang.core.repository.AbstractRepositoryTest;
import de.binaerebauten.gleichklang.core.repository.user.UserRepository;
import de.binaerebauten.gleichklang.core.utils.DefaultEntityFactory;
import de.binaerebauten.gleichklang.core.utils.DefaultStaticEntityFactory;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

/**
 * Unit tests for {@link UndeliverableMailRepository}.
 */
public class UndeliverableMailRepositoryTest extends AbstractRepositoryTest<UndeliverableMail>
{
	@Autowired
	private UndeliverableMailRepository undeliverableMailRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private DefaultEntityFactory defaultEntityFactory;
	
	private UndeliverableMail undeliverableMail;

	@Test
	public void testFindByRecipientEmail()
	{
		assertThat(undeliverableMailRepository.findByRecipientEmail(null).size(), equalTo(0));
		assertThat(undeliverableMailRepository.findByRecipientEmail(DefaultStaticEntityFactory.DEFAULT_EMAIL), CoreMatchers.hasItem(undeliverableMail));
	}
	
	@Test
	public void testFindByRecipientEmailAndUndeliverableMailReason()
	{
		final UndeliverableMailReason undeliverableMailReason = UndeliverableMailReason.MAILBOX_FULL;
		
		assertThat(undeliverableMailRepository.findByRecipientEmailAndUndeliverableMailReason(null, undeliverableMailReason), nullValue());
		assertThat(undeliverableMailRepository.findByRecipientEmailAndUndeliverableMailReason(DefaultStaticEntityFactory.DEFAULT_EMAIL, undeliverableMailReason), is(undeliverableMail));
	}

	@Override
	protected Collection<UndeliverableMail> getPersistedEntities()
	{
		undeliverableMail = new UndeliverableMail();

		undeliverableMail.setRecipientEmail(DefaultStaticEntityFactory.DEFAULT_EMAIL);
		undeliverableMail.setUndeliverableMailReason(UndeliverableMailReason.MAILBOX_FULL);
		undeliverableMailRepository.save(undeliverableMail);
		
		defaultEntityFactory.persistDefaultUser(DefaultStaticEntityFactory.DEFAULT_EMAIL, DefaultStaticEntityFactory.DEFAULT_ALIAS);

		return Collections.singleton(undeliverableMail);
	}

	@Override
	protected JpaRepository<UndeliverableMail, Long> getRepository()
	{
		return undeliverableMailRepository;
	}
}

package de.binaerebauten.gleichklang.core.repository.mail;

import de.binaerebauten.gleichklang.core.model.mail.Newsletter;
import de.binaerebauten.gleichklang.core.repository.AbstractRepositoryTest;
import de.binaerebauten.gleichklang.core.utils.DefaultEntityFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Unit tests for {@link Newsletter}.
 */
public class NewsletterRepositoryTest extends AbstractRepositoryTest<Newsletter>
{
	@Autowired
	private NewsletterRepository newsletterRepository;
	
	@Autowired
	private DefaultEntityFactory defaultEntityFactory;
	
	@Override
	protected Collection<Newsletter> getPersistedEntities()
	{
		return Collections.singleton(defaultEntityFactory.persistDefaultNewsletter());
	}
	
	@Override
	protected JpaRepository<Newsletter, Long> getRepository()
	{
		return newsletterRepository;
	}
	
	@Test
	public void findByEmailInTest()
	{
		final List<String> emails = new ArrayList<>();
		final List<Newsletter> newsletters = new ArrayList<>();
		
		final int count = 100;
		
		for (int i = 0; i < count; i++)
		{
			final String email = i + "test@example.com";
			
			final Newsletter newsletter = new Newsletter();
			newsletter.setEmail(email);
			
			emails.add(email);
			newsletters.add(newsletter);
		}
		
		newsletterRepository.save(newsletters);
		
		final Set<Newsletter> actualNewsletters = newsletterRepository.findByEmailIn(emails);
		assertThat(actualNewsletters.size(), equalTo(count));
	}
	
	@Test
	public void deleteByEmailTest()
	{
		final String email = "test@example.com";
		
		final Newsletter newsletter = new Newsletter();
		newsletter.setEmail(email);
		
		final long sizeBefore = newsletterRepository.count();
		newsletterRepository.save(newsletter);
		final long sizeAfter = newsletterRepository.count();
		
		assertThat(sizeBefore, not(equalTo(sizeAfter)));
		
		newsletterRepository.deleteByEmail(email);
		
		assertThat(newsletterRepository.count(), equalTo(sizeBefore));
	}
}

//package de.binaerebauten.gleichklang.core.repository;
//
//import de.binaerebauten.gleichklang.core.model.BaseEntity;
//import de.binaerebauten.gleichklang.core.model.user.EmailLink;
//import de.binaerebauten.gleichklang.core.repository.user.EmailLinkRepository;
//import de.binaerebauten.gleichklang.core.utils.DefaultEntityFactory;
//import org.junit.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.jpa.repository.JpaRepository;
//
//import java.time.LocalDateTime;
//import java.time.temporal.ChronoUnit;
//import java.util.Collection;
//import java.util.Collections;
//import java.util.List;
//import java.util.stream.Collectors;
//
//import static org.hamcrest.CoreMatchers.equalTo;
//import static org.junit.Assert.assertThat;
//
//public class EmailLinkRepositoryTest extends AbstractRepositoryTest<EmailLink>
//{
//	@Autowired
//	private EmailLinkRepository emailLinkRepository;
//
//	@Autowired
//	private DefaultEntityFactory entityFactory;
//
//	public EmailLinkRepositoryTest()
//	{
//	}
//
//	@Override
//	protected Collection<EmailLink> getPersistedEntities()
//	{
//		return Collections.singletonList(entityFactory.persistDefaultEmailLink());
//	}
//
//	@Override
//	protected JpaRepository<EmailLink, Long> getRepository()
//	{
//		return emailLinkRepository;
//	}
//
//	@Test
//	public void findExpiredIdsTest()
//	{
//		final EmailLink emailLink = emailLinkRepository.findAll().get(0);
//		List<Long> ids;
//
//		ids = emailLinkRepository.findExpiredIds(LocalDateTime.now().minus(1, ChronoUnit.DAYS));
//		assertThat(ids.size(), equalTo(0));
//
//		emailLink.setCreateDate(LocalDateTime.now().minus(1, ChronoUnit.HALF_DAYS));
//		emailLinkRepository.save(emailLink);
//
//		ids = emailLinkRepository.findExpiredIds(LocalDateTime.now().minus(1, ChronoUnit.DAYS));
//		assertThat(ids.size(), equalTo(0));
//
//		emailLink.setCreateDate(LocalDateTime.now().minus(1, ChronoUnit.DAYS));
//		emailLinkRepository.save(emailLink);
//
//		ids = emailLinkRepository.findExpiredIds(LocalDateTime.now().minus(1, ChronoUnit.DAYS));
//		assertThat(ids.size(), equalTo(1));
//		assertThat(ids.iterator().next(), equalTo(emailLink.getId()));
//	}
//
//	@Test
//	public void deletedInTest()
//	{
//		assertThat(emailLinkRepository.count(), equalTo(1L));
//
//		final List<Long> ids = emailLinkRepository.findAll().stream().map(BaseEntity::getId).collect(Collectors.toList());
//		emailLinkRepository.deleteByIdIn(ids);
//
//		assertThat(emailLinkRepository.count(), equalTo(0L));
//	}
//
//	@Test
//	public void findByUniqueTokenTest()
//	{
//		final EmailLink expectedEmailLink = emailLinkRepository.findAll().get(0);
//		final EmailLink actualEmailLink = emailLinkRepository.findByUniqueToken(expectedEmailLink.getUniqueToken());
//
//		assertThat(actualEmailLink, equalTo(expectedEmailLink));
//	}
//
//	@Test
//	public void deleteByUserAndContextTest()
//	{
//		assertThat(emailLinkRepository.count(), equalTo(1L));
//
//		final EmailLink emailLink = emailLinkRepository.findAll().get(0);
//		emailLinkRepository.deleteByUserAndContext(emailLink.getUser(), emailLink.getContext());
//
//		assertThat(emailLinkRepository.count(), equalTo(0L));
//	}
//}

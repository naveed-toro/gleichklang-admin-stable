package de.binaerebauten.gleichklang.core.repository;

import de.binaerebauten.gleichklang.core.model.news.News;
import de.binaerebauten.gleichklang.core.model.news.UserNews;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.utils.DefaultEntityFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class UserNewsRepositoryTest extends AbstractRepositoryTest<UserNews>
{
	@Autowired
	private UserNewsRepository userNewsRepository;

	@Autowired
	private DefaultEntityFactory entityFactory;
	
	@Autowired
	private NewsRepository newsRepository;

	private User user;

	private News news;

	public UserNewsRepositoryTest()
	{
	}

	@Override
	protected Collection<UserNews> getPersistedEntities()
	{
		user = entityFactory.persistDefaultUser("test");
		news = entityFactory.persistDefaultNews();

		return Collections.singletonList(entityFactory.persistDefaultUserNews(news, user));
	}

	@Override
	protected JpaRepository<UserNews, Long> getRepository()
	{
		return userNewsRepository;
	}

	@Test
	public void testFindNewNews()
	{
		final List<UserNews> userNews = userNewsRepository.findByUser(user);

		assertThat(userNews.size(), equalTo(1));
		assertThat(userNews.iterator().next().getNews(), equalTo(news));
	}
	
	@Test
	public void testFindUserIdsByNews()
	{
		final Set<Long> userIds = userNewsRepository.findUserIdsByNews(news);
		
		assertThat(userIds.size(), equalTo(1));
		assertThat(userIds.iterator().next(), equalTo(user.getId()));
	}
	
	@Test
	public void testFindNewsToSendViaEmail()
	{
		List<UserNews> userNews;
		
		userNews = userNewsRepository.findNewsToSendViaEmail(user);
		assertThat(userNews.size(), equalTo(0));
		
		news.setEmailNotification(true);
		newsRepository.save(news);
		
		userNews = userNewsRepository.findNewsToSendViaEmail(user);
		assertThat(userNews.size(), equalTo(1));
		
		final UserNews un = userNewsRepository.findAll().iterator().next();
		un.setNotified(true);
		userNewsRepository.save(un);
		
		userNews = userNewsRepository.findNewsToSendViaEmail(user);
		assertThat(userNews.size(), equalTo(0));
	}
}

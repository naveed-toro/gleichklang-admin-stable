package de.binaerebauten.gleichklang.core.repository;

import de.binaerebauten.gleichklang.core.model.news.News;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.utils.DefaultEntityFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class NewsRepositoryTest extends AbstractRepositoryTest<News>
{
	@Autowired
	private NewsRepository newsRepository;

	@Autowired
	private DefaultEntityFactory entityFactory;

	public NewsRepositoryTest()
	{
	}

	@Override
	protected Collection<News> getPersistedEntities()
	{
		return Collections.singletonList(entityFactory.persistDefaultNews());
	}

	@Override
	protected JpaRepository<News, Long> getRepository()
	{
		return newsRepository;
	}

	@Test
	public void testFindNewNews()
	{
		final User user = entityFactory.persistDefaultUser("test");

		assertThat(newsRepository.findNewNews(user).size(), equalTo(0));

		final News news = entityFactory.persistDefaultNews();
		news.setActive(true);
		newsRepository.save(news);

		assertThat(newsRepository.findNewNews(user).size(), equalTo(1));

		entityFactory.persistDefaultUserNews(news, user);

		assertThat(newsRepository.findNewNews(user).size(), equalTo(0));
	}
}

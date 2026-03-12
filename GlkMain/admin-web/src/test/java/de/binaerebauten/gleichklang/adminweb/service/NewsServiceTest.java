package de.binaerebauten.gleichklang.adminweb.service;

import de.binaerebauten.gleichklang.adminweb.config.AdminTestConfig;
import de.binaerebauten.gleichklang.adminweb.view.popup.NewsPopup.SaveCallback;
import de.binaerebauten.gleichklang.core.model.filter.AbstractFilter;
import de.binaerebauten.gleichklang.core.model.news.News;
import de.binaerebauten.gleichklang.core.repository.BasePersistenceTest;
import de.binaerebauten.gleichklang.core.repository.FilterRepository;
import de.binaerebauten.gleichklang.core.repository.NewsRepository;
import de.binaerebauten.gleichklang.core.repository.UserNewsRepository;
import de.binaerebauten.gleichklang.core.service.FilterService;
import de.binaerebauten.gleichklang.core.utils.DefaultEntityFactory;
import de.binaerebauten.gleichklang.core.utils.FilterEntityFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.support.TransactionTemplate;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

@ContextConfiguration(classes = { AdminTestConfig.class })
public class NewsServiceTest extends BasePersistenceTest
{
	@Autowired
	private DefaultEntityFactory entityFactory;
	
	@Autowired
	private FilterEntityFactory filterEntityFactory;
	
	@Autowired
	private NewsService newsService;
	
	@Autowired
	private NewsRepository newsRepository;
	
	@Autowired
	private UserNewsRepository userNewsRepository;
	
	@Autowired
	private FilterRepository filterRepository;
	
	@Autowired
	private FilterService filterService;
	
	@Autowired
	private TransactionTemplate transactionTemplate;
	
	@Before
	public void setup()
	{
		assertThat(newsRepository.findAll().size(), equalTo(0));
		
		final News news = entityFactory.persistDefaultNews();
		news.setFilter(filterEntityFactory.persistDefaultMemberStatusFilter());
		newsRepository.save(news);
		entityFactory.persistDefaultUserNews(news, entityFactory.persistDefaultUser("test"));
		
		assertThat(newsRepository.findAll().size(), equalTo(1));
		assertThat(userNewsRepository.findAll().size(), equalTo(1));
		assertThat(filterRepository.findAll().size(), equalTo(1));
	}
	
	@After
	public void teardown()
	{
		entityFactory.reset();
		filterEntityFactory.reset();
	}
	
	private News getNews()
	{
		return transactionTemplate.execute(status ->
		{
			final News news = newsRepository.findAll().iterator().next();
			assertThat(news, notNullValue());
			assertThat(news.getFilter(), notNullValue());
			return news;
		});
	}
	
	@Test
	public void createSaveCallbackTest()
	{
		final News news = getNews();
		
		final SaveCallback saveCallback = newsService.createSaveCallback(news);
		assertThat(saveCallback, notNullValue());
		
		final AbstractFilter newFilter = filterEntityFactory.persistDefaultRecommendationCategoryFilter();
		
		assertThat(filterRepository.findAll().size(), equalTo(2));
		
		news.setFilter(newFilter);
		saveCallback.saveNewNews(news);
		
		assertThat(filterRepository.findAll().size(), equalTo(1));
	}
	
	@Test
	public void deleteNews()
	{
		final News news = getNews();
		
		newsService.deleteNews(news);
		
		assertThat(newsRepository.findAll().size(), equalTo(0));
		assertThat(userNewsRepository.findAll().size(), equalTo(0));
		assertThat(filterRepository.findAll().size(), equalTo(0));
	}
}

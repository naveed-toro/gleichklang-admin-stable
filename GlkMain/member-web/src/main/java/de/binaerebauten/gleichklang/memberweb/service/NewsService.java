package de.binaerebauten.gleichklang.memberweb.service;

import de.binaerebauten.gleichklang.core.model.filter.AbstractFilter;
import de.binaerebauten.gleichklang.core.model.news.News;
import de.binaerebauten.gleichklang.core.model.news.UserNews;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.repository.NewsRepository;
import de.binaerebauten.gleichklang.core.repository.UserNewsRepository;
import de.binaerebauten.gleichklang.core.repository.user.UserRepository;
import de.binaerebauten.gleichklang.core.utils.CheckedTransactional;
import de.binaerebauten.gleichklang.core.utils.filter.FilterSpecificationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class NewsService
{
	private static final Logger LOG = LoggerFactory.getLogger(NewsService.class);
	
	private final NewsRepository newsRepository;
	
	private final UserNewsRepository userNewsRepository;
	
	private final UserRepository userRepository;
	
	private final FilterSpecificationBuilder filterSpecificationBuilder;
	
	@Autowired
	public NewsService(NewsRepository newsRepository, UserNewsRepository userNewsRepository,
			UserRepository userRepository, FilterSpecificationBuilder filterSpecificationBuilder)
	{
		this.newsRepository = newsRepository;
		this.userNewsRepository = userNewsRepository;
		this.userRepository = userRepository;
		this.filterSpecificationBuilder = filterSpecificationBuilder;
	}
	
	@CheckedTransactional
	public List<UserNews> getNewsForUser(User user)
	{
		final List<News> newsList = newsRepository.findNewNews(user);

		List<UserNews> userNewsList = new ArrayList<>();

		for (News news : newsList)
		{
			final AbstractFilter filter = news.getFilter();
			final Specification<User> spec = Specifications.where(filterSpecificationBuilder.build(filter,""))
					.and((root, query, cb) -> root.in(user));
			
			if (userRepository.count(spec) > 0)
			{
				try
				{
					final UserNews userNews = new UserNews();
					userNews.setUser(user);
					userNews.setNews(news);
					if(LocalDateTime.now().compareTo(news.getValidFrom())>=0 && LocalDateTime.now().compareTo(news.getValidTo())<=0)
					{
						userNewsRepository.saveAndFlush(userNews);
					}
					userNewsRepository.findByUser(user);
				}
				catch (Exception ex)
				{
					LOG.warn("can't create userNews for user " + user.getId() + " and news " + news.getId() + " maybe already exists because of concurrent overlapping", ex);
				}
			}
		}

		for (UserNews userNews : userNewsRepository.findByUser(user))
		{
			final AbstractFilter filter = userNews.getNews().getFilter();
			final Specification<User> spec = Specifications.where(filterSpecificationBuilder.build(filter,""))
					.and((root, query, cb) -> root.in(user));

			if (userRepository.count(spec) > 0)
			{
				if(LocalDateTime.now().compareTo(userNews.getNews().getValidFrom())>=0 && LocalDateTime.now().compareTo(userNews.getNews().getValidTo())<=0)
				{
					userNewsList.add(userNews);
				}
			}
		}
		return userNewsList;
	}
}

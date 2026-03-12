package de.binaerebauten.gleichklang.adminweb.service;

import de.binaerebauten.gleichklang.adminweb.view.popup.NewsPopup.SaveCallback;
import de.binaerebauten.gleichklang.core.model.filter.AbstractFilter;
import de.binaerebauten.gleichklang.core.model.mail.UserMailTemplate;
import de.binaerebauten.gleichklang.core.model.news.News;
import de.binaerebauten.gleichklang.core.model.news.UserNews;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.repository.NewsRepository;
import de.binaerebauten.gleichklang.core.repository.UserNewsRepository;
import de.binaerebauten.gleichklang.core.repository.user.UserRepository;
import de.binaerebauten.gleichklang.core.service.FilterService;
import de.binaerebauten.gleichklang.core.service.mail.MailQueueService;
import de.binaerebauten.gleichklang.core.utils.StringUtils;
import de.binaerebauten.gleichklang.core.utils.filter.FilterSpecificationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.concurrent.ListenableFuture;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static java.time.Duration.between;

@Service
public class NewsService
{
	private static final Logger LOG = LoggerFactory.getLogger(NewsService.class);
	
	@Autowired
	private NewsRepository newsRepository;
	
	@Autowired
	private FilterService filterService;
	
	@Autowired
	private UserNewsRepository userNewsRepository;
	
	@Autowired
	private TransactionTemplate transactionTemplate;
	
	@Autowired
	private MailQueueService mailQueueService;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private FilterSpecificationBuilder filterSpecificationBuilder;
	
	public SaveCallback createSaveCallback(News oldNews)
	{
		final List<AbstractFilter> oldFilters = filterService.getFilterList(oldNews);
		return news -> saveNews(news, oldFilters);
	}
	
	private void saveNews(News news, List<AbstractFilter> oldFilters)
	{
		transactionTemplate.execute(status ->
		{
			final Collection<AbstractFilter> removeFilters = filterService.saveFilterChain(news, oldFilters);
			newsRepository.save(news);
			filterService.deleteFilters(removeFilters);
			return null;
		});
	}
	
	@Transactional
	public void deleteNews(News news)
	{
		if (news == null) return;
		
		newsRepository.delete(news);
		filterService.deleteFilters(filterService.getFilterList(news));
	}
	
	@Async
	public ListenableFuture<Void> enqueueNewsAsync(News news)
	{
		final LocalDateTime startTime = LocalDateTime.now();
		
		final AbstractFilter filter = news.getFilter();
		final Specification<User> spec = filterSpecificationBuilder.build(filter,"");
		
		final List<Long> userIds = userRepository.findAllWithIdsOnly(spec);
		final Set<Long> userWithUserNews = userNewsRepository.findUserIdsByNews(news);
		
		for (Long userId : userIds)
		{
			try
			{
				if (!userWithUserNews.contains(userId))
				{
					final UserNews userNews = new UserNews();
					userNews.setUser(userRepository.findById(userId));
					userNews.setNews(news);
					userNewsRepository.saveAndFlush(userNews);
				}
			}
			catch (Exception ex)
			{
				LOG.warn("can't create userNews for user " + userId + " and news " + news.getId() + " maybe already exists because of concurrent overlapping", ex);
			}
			
			mailQueueService.enqueue(userId, UserMailTemplate.NEWS_FROM_GLEICHKLANG);
		}
		
		final Duration duration = between(startTime, LocalDateTime.now());
		LOG.info("news mail enqueue duration: {} for {} users", StringUtils.durationToString(duration), userIds.size());
		
		return new AsyncResult<>(null);
	}
}
package de.binaerebauten.gleichklang.adminweb.presenter;

import com.vaadin.server.Page;
import com.vaadin.ui.Notification;
import de.binaerebauten.gleichklang.adminweb.service.NewsService;
import de.binaerebauten.gleichklang.adminweb.view.I18N;
import de.binaerebauten.gleichklang.adminweb.view.NewsView;
import de.binaerebauten.gleichklang.adminweb.view.popup.NewsPopup;
import de.binaerebauten.gleichklang.core.model.filter.TemplateContext;
import de.binaerebauten.gleichklang.core.model.news.News;
import de.binaerebauten.gleichklang.core.model.user.Admin;
import de.binaerebauten.gleichklang.core.presenter.NavigatePresenter;
import de.binaerebauten.gleichklang.core.presenter.filter.DefaultFilterControlHandler;
import de.binaerebauten.gleichklang.core.repository.NewsRepository;
import de.binaerebauten.gleichklang.core.service.AdminService;
import de.binaerebauten.gleichklang.core.service.FilterControlService;
import de.binaerebauten.gleichklang.core.utils.filter.FilterSpecificationBuilder;
import org.springframework.context.ApplicationContext;

import java.time.LocalDateTime;

public class NewsPresenter extends NavigatePresenter implements NewsView.NewsViewListener
{
	private final NewsRepository newsRepository;
	private final NewsService newsService;
	private final NewsView view;
	private final DefaultFilterControlHandler filterControlHandler;
	private final Admin currentAdmin;
	private final FilterSpecificationBuilder filterSpecificationBuilder;
	
	public NewsPresenter(ApplicationContext ctx, NewsView view)
	{
		super(view);
		
		newsRepository = ctx.getBean(NewsRepository.class);
		newsService = ctx.getBean(NewsService.class);
		
		filterControlHandler = new DefaultFilterControlHandler(ctx.getBean(FilterControlService.class));
		filterControlHandler.setTemplateContext(TemplateContext.NEWS);
		
		currentAdmin = ctx.getBean(AdminService.class).getCurrentUser();
		
		filterSpecificationBuilder = ctx.getBean(FilterSpecificationBuilder.class);
		
		this.view = view;
		this.view.setListener(this);
	}
	
	@Override
	public void deleteNews(News news)
	{
		newsService.deleteNews(news);
		refreshView();
		Page.getCurrent().reload();
		Notification.show(I18N.NEWS_PRESENTER_DELETE.msg(), Notification.Type.TRAY_NOTIFICATION);
	}
	
	@Override
	public void editNews(News news)
	{
		final NewsPopup popup = new NewsPopup(news, newsService.createSaveCallback(news), filterControlHandler, filterSpecificationBuilder);
		popup.addCloseListener(event -> refreshView());
		tryOpenPopup(popup);
	}
	
	@Override
	public void createNews()
	{
		final News news = new News();
		news.setActive(false);
		editNews(news);
	}
	
	@Override
	public void sendNewsViaEmail(News news)
	{
		if (!news.isEmailNotification())
		{
			news.setEmailNotification(true);
			news.setAdmin(currentAdmin);
			news.setEmailSendDate(LocalDateTime.now());
			newsRepository.save(news);
			newsService.enqueueNewsAsync(news);
			refreshView();
		}
	}
	
	@Override
	public void enter(String parameters)
	{
		refreshView();
	}
	
	@Override
	public void leave()
	{
		this.view.setNewsHandler(null);
		
		super.leave();
	}
	
	private void refreshView()
	{
		this.view.setNewsHandler(newsRepository::findAll);
		view.disableSendEmailButton();
	}
	
}

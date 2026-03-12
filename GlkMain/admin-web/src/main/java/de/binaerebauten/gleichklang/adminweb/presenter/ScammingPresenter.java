package de.binaerebauten.gleichklang.adminweb.presenter;

import de.binaerebauten.gleichklang.adminweb.presenter.handler.UserControlHandler;
import de.binaerebauten.gleichklang.adminweb.view.ScammingView;
import de.binaerebauten.gleichklang.adminweb.view.ScammingView.ScammingViewListener;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.presenter.NavigatePresenter;
import de.binaerebauten.gleichklang.core.service.ScammingService;
import de.binaerebauten.gleichklang.core.view.component.MessageBox;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import java.util.List;

public class ScammingPresenter extends NavigatePresenter implements ScammingViewListener
{
	private final ScammingService scammingService;
	private final ScammingView view;
	private final UserControlHandler userControlHandler;
	
	public ScammingPresenter(ApplicationContext ctx, ScammingView view)
	{
		super(view);
		
		this.view = view;
		
		scammingService = ctx.getBean(ScammingService.class);
		
		userControlHandler = new UserControlHandler(ctx, this);
		
		final Environment environment = ctx.getBean(Environment.class);
		final long messageCount = environment.getProperty("scamming.message.count", Long.class);
		final long durationSeconds = environment.getProperty("scamming.message.duration", Long.class);
		final boolean containsEmail = environment.getProperty("scamming.contains.mail", Boolean.class);
		final String keywords = environment.getProperty("scamming.keywords", String.class);
		
		view.initValues(messageCount, durationSeconds, containsEmail, keywords);
		
		view.setListener(this);
	}
	
	@Override
	public void enter(String parameters)
	{
		view.addResult(scammingService.createScammingHandler());
	}
	
	@Override
	public void search(long messageCount, long durationSeconds, boolean containsEmail, List<String> keywords)
	{
		if(scammingService.isUpdateScammingInProcess())
		{
			MessageBox.show("Suche läuft noch - bitte warten ...");
		}
		else
		{
			scammingService.updateScamming(messageCount, durationSeconds, containsEmail, keywords);
		}
	}
	
	@Override
	public void refresh()
	{
		if(scammingService.isUpdateScammingInProcess())
		{
			MessageBox.show("Suche läuft noch - bitte warten ...");
		}
		else
		{
			view.addResult(scammingService.createScammingHandler());
		}
	}
	
	@Override
	public void openUser(User user)
	{
		userControlHandler.openUser(user);
	}
}

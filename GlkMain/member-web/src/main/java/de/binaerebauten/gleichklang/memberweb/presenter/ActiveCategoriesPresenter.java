package de.binaerebauten.gleichklang.memberweb.presenter;

import com.vaadin.ui.UI;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.presenter.NavigatePresenter;
import de.binaerebauten.gleichklang.core.service.SubscriptionService;
import de.binaerebauten.gleichklang.core.service.UserService;
import de.binaerebauten.gleichklang.core.service.validator.ValidationException;
import de.binaerebauten.gleichklang.memberweb.view.ActiveCategoriesView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

public class ActiveCategoriesPresenter extends NavigatePresenter implements ActiveCategoriesView.ActiveCategoriesViewListener
{
	private static final Logger LOG = LoggerFactory.getLogger(ActiveCategoriesPresenter.class);
	
	private final ActiveCategoriesView view;
	private final UserService userService;
	private final SubscriptionService subscriptionService;
	
	public ActiveCategoriesPresenter(ApplicationContext ctx, ActiveCategoriesView view)
	{
		super(view);
		this.view = view;
		
		userService = ctx.getBean(UserService.class);
		subscriptionService = ctx.getBean(SubscriptionService.class);
		
		this.view.setListener(this);
	}
	
	@Override
	public void enter(String parameters)
	{
		refreshView();
	}
	
	@Override
	public void saveCategories(Set<RecommendationCategory> recommendationCategories) throws ValidationException
	{
		// User can deselect both the categories-GR3-39
//		if (recommendationCategories == null || recommendationCategories.isEmpty())
//		{
//			throw new ValidationException(de.binaerebauten.gleichklang.memberweb.view.I18N.ACCOUNT_WARN_NO_CATEGORIES.msg());
//		}
		final User currentUser = userService.getCurrentUser();
		currentUser.setCategories(recommendationCategories);
		
		userService.save(currentUser);
		UI.getCurrent().getPage().reload();
	}
	
	private void refreshView()
	{
		final User currentUser = userService.getCurrentUser();
		
		final SortedMap<RecommendationCategory, Boolean> categories = new TreeMap<>();
		final Set<RecommendationCategory> visibleCategories = subscriptionService.getCurrentSubscriptionOfferCategories(currentUser);
		
		for (RecommendationCategory category : visibleCategories)
		{
			categories.put(category, currentUser.getCategories().contains(category));
		}
		
		this.view.setCategories(categories);
	}
}

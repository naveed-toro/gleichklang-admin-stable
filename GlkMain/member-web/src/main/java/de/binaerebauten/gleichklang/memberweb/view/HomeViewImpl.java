package de.binaerebauten.gleichklang.memberweb.view;

import com.google.common.collect.Multimap;
import com.vaadin.annotations.JavaScript;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.*;
import de.binaerebauten.gleichklang.core.initializer.AppUI;
import de.binaerebauten.gleichklang.core.model.audio.UserAudio;
import de.binaerebauten.gleichklang.core.model.matching.Relationship;
import de.binaerebauten.gleichklang.core.model.news.UserNews;
import de.binaerebauten.gleichklang.core.model.questionnaire.Question.Requirement;
import de.binaerebauten.gleichklang.core.model.questionnaire.Questionnaire;
import de.binaerebauten.gleichklang.core.model.user.ClientInformation.Device;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.navigation.DefaultNavigator;
import de.binaerebauten.gleichklang.core.service.SubscriptionService;
import de.binaerebauten.gleichklang.core.service.UserService;
import de.binaerebauten.gleichklang.core.service.content.AdvertisementContentTemplate;
import de.binaerebauten.gleichklang.core.utils.LocaleAware;
import de.binaerebauten.gleichklang.core.view.AbstractNavigateView;
import de.binaerebauten.gleichklang.core.view.component.ComponentReplacer;
import de.binaerebauten.gleichklang.core.view.component.LazyBeanItemContainer;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;
import de.binaerebauten.gleichklang.memberweb.navigation.DefaultNavigatorFactory;
import de.binaerebauten.gleichklang.memberweb.view.HomeView.HomeViewListener;
import de.binaerebauten.gleichklang.memberweb.view.component.*;

import java.util.*;
public class HomeViewImpl extends AbstractNavigateView<HomeViewListener> implements HomeView, LocaleAware
{
	private final NewsComponent newsComponent;
	private final NewUserMessagesComponent userMessagesComponent;
	private final NewFootprintsComponent footprintsComponent;
	private final UserProfileReportDownloadComponent reportDownloadComponent;
	private final ActivateProlongationComponent activateProlongationComponent;
	private final MyAccountComponent myAccountComponent;
	private final MyProfileComponent myProfileComponent;

	private final VerticalLayout root;
	private VerticalLayout content;
	private final ComponentReplacer<Component> staticContentOne;
	private final ComponentReplacer<Component> staticContentTwo;
	private final ComponentReplacer<Component> staticContentThree;
	private final SubscriptionService subscriptionService;
	private final UserService userService;

	public HomeViewImpl(Device device)
	{
		root = new VerticalLayout();
		root.setSizeFull();
		root.setStyleName(CssStyle.HOME_VIEW_WRAPPER.getStyleName());

		subscriptionService = AppUI.getApplicationContext().getBean(SubscriptionService.class);
		userService = AppUI.getApplicationContext().getBean(UserService.class);

		content = new VerticalLayout();
		content.setSizeFull();

		newsComponent = new NewsComponent(usernews -> getListener().showFullNews(usernews));
		newsComponent.addStyleName(CssStyle.PANEL_WRAPPER.getStyleName());
		newsComponent.addStyleName(CssStyle.PANEL_STEP_GROW.getStyleName());

		myAccountComponent = new MyAccountComponent(
				address -> getListener().showIncompleteAddress(address),
				() -> getListener().showSubscription(),
				() -> getListener().onGotoActivateCategoryClicked(),
				() -> getListener().onGotoRecommendationBreakClicked());

		myAccountComponent.setStyleName(CssStyle.PANEL_WRAPPER.getStyleName());
		myAccountComponent.addStyleName(CssStyle.PANEL_STEP_GROW.getStyleName());

		userMessagesComponent = new NewUserMessagesComponent(category -> getListener().showMessages(category),
				category -> getListener().showMatches(category),() -> getListener().onGotoActivateCategoryClicked());

		userMessagesComponent.addStyleName(CssStyle.PANEL_WRAPPER.getStyleName());
		userMessagesComponent.addStyleName(CssStyle.HOME_VIEW_OVERVIEW.getStyleName());

		reportDownloadComponent = new UserProfileReportDownloadComponent();
		reportDownloadComponent.addStyleName(CssStyle.PANEL_WRAPPER.getStyleName());
		reportDownloadComponent.addStyleName(CssStyle.PANEL_STEP_GROW.getStyleName());

		activateProlongationComponent = new ActivateProlongationComponent(device);
		activateProlongationComponent.addStyleName(CssStyle.PANEL_WRAPPER.getStyleName());
		activateProlongationComponent.addStyleName(CssStyle.PANEL_STEP_GROW.getStyleName());

		footprintsComponent = new NewFootprintsComponent(device, relationship -> getListener().showRelationship(relationship, device));

		myProfileComponent = new MyProfileComponent
				(
						(incompleteQuestionnaire, requirements) -> getListener().showIncompleteQuestions(incompleteQuestionnaire, requirements),
						category -> getListener().showAvatar(category),category -> getListener().navigateTo(DefaultNavigatorFactory.MemberMenuItem.AUDIO)
				);
		myProfileComponent.addStyleName(CssStyle.PANEL_STEP_GROW.getStyleName());

		staticContentOne = new ComponentReplacer<>();
		staticContentTwo = new ComponentReplacer<>();
		staticContentThree = new ComponentReplacer<>();

		setCompositionRoot(root);
		initResponsiveView(device);

		// create Step-wise resize helper and resize elements
		final JSHelperHorizontalStepGrow jsHelperHorizontalStepGrow = new JSHelperHorizontalStepGrow(CssStyle.PANEL_STEP_GROW.getStyleName(), 275);
		jsHelperHorizontalStepGrow.update();
	}

	private Component createOfferPlaceholder()
	{
		staticContentOne.addStyleName(CssStyle.PANEL_WRAPPER.getStyleName());

		return staticContentOne;
	}

	private Component createAffiliatePlaceholder()
	{
		staticContentTwo.addStyleName(CssStyle.PANEL_WRAPPER.getStyleName());

		return staticContentTwo;
	}

	private Component createSecondOfferPlaceholder()
	{
		staticContentThree.addStyleName(CssStyle.PANEL_WRAPPER.getStyleName());

		return staticContentThree;
	}

	private void createDesktopView()
	{
		userMessagesComponent.createLayout();

		final MultiColumnLayout multiColumnLayout = new MultiColumnLayout(3);
		multiColumnLayout.setWidth(100, Unit.PERCENTAGE);

		multiColumnLayout.addComponent(newsComponent, 0);
		multiColumnLayout.addComponent(myAccountComponent, 0);
		multiColumnLayout.addComponent(createOfferPlaceholder(), 0);

		multiColumnLayout.addComponent(userMessagesComponent, 1);

		if(subscriptionService.findCurrentSubscription(userService.getCurrentUser())!=null &&
				subscriptionService.findCurrentSubscription(userService.getCurrentUser()).isPresent() &&
				!subscriptionService.findCurrentSubscription(userService.getCurrentUser()).get().isAutomaticRenewal()) {
			multiColumnLayout.addComponent(activateProlongationComponent, 1);
		}
		multiColumnLayout.addComponent(reportDownloadComponent, 1);
		multiColumnLayout.addComponent(createSecondOfferPlaceholder(), 1);

		multiColumnLayout.addComponent(footprintsComponent, 2);
		multiColumnLayout.addComponent(myProfileComponent, 2);
		multiColumnLayout.addComponent(createAffiliatePlaceholder(), 2);

		content.addComponent(multiColumnLayout);
		content.setComponentAlignment(multiColumnLayout, Alignment.TOP_CENTER);

		root.addComponent(content);
		root.setComponentAlignment(content, Alignment.TOP_CENTER);
	}

	private void createMobileView()
	{

		final HorizontalLayout panelWrapper = new HorizontalLayout();
		panelWrapper.setSizeFull();

		final VerticalLayout column = new VerticalLayout();
		column.setSizeFull();

		userMessagesComponent.createLayout();
		if(subscriptionService.findCurrentSubscription(userService.getCurrentUser())!=null &&
				subscriptionService.findCurrentSubscription(userService.getCurrentUser()).isPresent() &&
				!subscriptionService.findCurrentSubscription(userService.getCurrentUser()).get().isAutomaticRenewal()) {
			column.addComponents(newsComponent, userMessagesComponent,activateProlongationComponent, footprintsComponent, myAccountComponent, myProfileComponent,
					createOfferPlaceholder(), reportDownloadComponent, createSecondOfferPlaceholder(), createAffiliatePlaceholder());
		}
		else {
			column.addComponents(newsComponent, userMessagesComponent, footprintsComponent, myAccountComponent, myProfileComponent,
					createOfferPlaceholder(), reportDownloadComponent, createSecondOfferPlaceholder(), createAffiliatePlaceholder());
		}

		panelWrapper.addComponent(column);

		content.addComponent(panelWrapper);
		content.setComponentAlignment(panelWrapper, Alignment.TOP_CENTER);
		root.addComponent(content);
		root.setComponentAlignment(content, Alignment.TOP_CENTER);
	}

	private void createTabletView()
	{
		final MultiColumnLayout multiColumnLayout = new MultiColumnLayout(2);
		multiColumnLayout.setWidth(100, Unit.PERCENTAGE);

		userMessagesComponent.createLayout();

		multiColumnLayout.addComponent(newsComponent, 0);
		multiColumnLayout.addComponent(myAccountComponent, 0);
		multiColumnLayout.addComponent(myProfileComponent, 0);

		multiColumnLayout.addComponent(createSecondOfferPlaceholder(), 0);

		multiColumnLayout.addComponent(userMessagesComponent, 1);
		if(subscriptionService.findCurrentSubscription(userService.getCurrentUser())!=null &&
				subscriptionService.findCurrentSubscription(userService.getCurrentUser()).isPresent() &&
				!subscriptionService.findCurrentSubscription(userService.getCurrentUser()).get().isAutomaticRenewal()) {
			multiColumnLayout.addComponent(activateProlongationComponent,1);
		}
		multiColumnLayout.addComponent(footprintsComponent, 1);
		multiColumnLayout.addComponent(createOfferPlaceholder(), 1);
		multiColumnLayout.addComponent(reportDownloadComponent, 1);
		multiColumnLayout.addComponent(createAffiliatePlaceholder(), 1);

		content.addComponent(multiColumnLayout);
		content.setComponentAlignment(multiColumnLayout, Alignment.TOP_CENTER);
		root.addComponent(content);
		root.setComponentAlignment(content, Alignment.TOP_CENTER);
	}

	private void initResponsiveView(Device device)
	{
		root.removeComponent(content);
		content = new VerticalLayout();
		content.setSizeFull();

		if (device == Device.DESKTOP)
			createDesktopView();
		else if (device == Device.TABLET)
			createTabletView();
		else if (device == Device.MOBILE)
			createMobileView();
	}

	@Override
	public void onDeviceChanged(Device device)
	{
		initResponsiveView(device);
	}

	@Override
	public void setIncompleteQuestionnaires(Multimap<Questionnaire, Requirement> incompleteQuestionnaires)
	{
		myProfileComponent.setIncompleteQuestionnaires(incompleteQuestionnaires);
	}

	@Override
	public void setNewUserMessages(HashMap<RecommendationCategory, Long> messages)
	{
		userMessagesComponent.setNewMessages(messages);
	}

	@Override
	public void setNewSuggestions(HashMap<RecommendationCategory, Long> suggestions)
	{
		userMessagesComponent.setNewSuggestions(suggestions);
	}
	@Override
	public void setNewUserMessagesUser(HashMap<String, Long> messages)
	{
		userMessagesComponent.setNewMessagesUser(messages);
	}

	@Override
	public void setNewSuggestionsUser(HashMap<String, Long> suggestions)
	{
		userMessagesComponent.setNewSuggestionsUser(suggestions);
	}
	@Override
	public void setVisibleCategories(Set<RecommendationCategory> visibleCategories)
	{
		userMessagesComponent.setVisibleCategories(visibleCategories);
	}

	@Override
	public void setUserReccats(Set<RecommendationCategory> reccats)
	{
		userMessagesComponent.setUserRecCats(reccats);
	}

	@Override
	public void setMissingAvatars(List<RecommendationCategory> recommendationCategories)
	{
		myProfileComponent.setMissingAvatars(recommendationCategories);
	}

	@Override
	public void setMissingAudio(Set<RecommendationCategory> recommendationCategories)
	{
		myProfileComponent.setMissingAudio(recommendationCategories);
	}

	@Override
	public void setNewFootprints(List<Relationship> relationships)
	{
		footprintsComponent.setFootprints(relationships);
		footprintsComponent.setVisible(!relationships.isEmpty());
	}

	@Override
	public void setNewsList(List<UserNews> newsList)
	{
		newsComponent.setUserNewsList(newsList);
		newsComponent.setVisible(!newsList.isEmpty());
	}

	public void setUserReportDownloader(Map<String, StreamResource> sourceList)
	{
		reportDownloadComponent.createUserReportDownloader(sourceList);
	}

	@Override
	public void setNewUserAdminMessages(Long nrOfMessages)
	{
		myAccountComponent.setNewAdminMessages(nrOfMessages);
		if (myAccountComponent.getUserAdminMessageButton() != null)
			myAccountComponent.getUserAdminMessageButton().addClickListener((Button.ClickListener) event -> fireEvent(HomeViewListener::showAdminMessages));
	}

	@Override
	public void setDisabledCategories(Collection<RecommendationCategory> deactivatedCategories, Collection<RecommendationCategory> recommendationBreaks)
	{
		myAccountComponent.setDisabledCategories(deactivatedCategories, recommendationBreaks);
	}

	@Override
	public void setSubscriptionInfo(String infoText, String buttonText)
	{
		myAccountComponent.setSubscriptionInfo(infoText, buttonText);
	}

	@Override
	public void setCurrentUser(User user)
	{
		myAccountComponent.setCurrentUser(user);
	}

	@Override
	public void checkPanelContent()
	{
		myProfileComponent.checkContent();
		myAccountComponent.checkContent();
	}

	@Override
	public void setStaticContent(Map<AdvertisementContentTemplate, Component> staticContent) {
		for (Map.Entry<AdvertisementContentTemplate, Component> item : staticContent.entrySet()) {
			switch (item.getKey()) {
				case AKTUELLES:
					staticContentThree.setComponent(item.getValue());
					break;

				case COMMUNITY:
					staticContentTwo.setComponent(item.getValue());
					break;

				case MOEGLICHKEITEN:
					staticContentOne.setComponent(item.getValue());
			}
		}
	}

	@Override
	public void setInvalidEmail(boolean isBlocked, String email, DefaultNavigator navigator, UserService userService, User currentUser)
	{
		myAccountComponent.setInvalidEmail(isBlocked, email, navigator, userService, currentUser);
	}

}

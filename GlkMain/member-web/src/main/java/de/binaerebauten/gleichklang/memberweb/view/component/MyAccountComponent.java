package de.binaerebauten.gleichklang.memberweb.view.component;

import com.google.common.base.Strings;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.Reindeer;
import com.vaadin.ui.themes.ValoTheme;
import de.binaerebauten.gleichklang.core.model.user.Address;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.navigation.DefaultNavigator;
import de.binaerebauten.gleichklang.core.service.UserService;
import de.binaerebauten.gleichklang.core.service.validator.UniqueValidationException;
import de.binaerebauten.gleichklang.core.view.component.MessageBox;
import de.binaerebauten.gleichklang.core.view.component.NavigationEnum;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;
import de.binaerebauten.gleichklang.memberweb.navigation.DefaultNavigatorFactory;
import de.binaerebauten.gleichklang.memberweb.view.I18N;
import de.binaerebauten.gleichklang.memberweb.view.UserDataView;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Created by rgoerner on 20.09.16.
 */
public class MyAccountComponent extends CustomComponent
{
	public interface ShowIncompleteAddressHandler
	{
		void onAddressClicked(Address address);
	}
	
	public interface ShowSubscriptionHandler
	{
		void onSubscriptionClicked();
	}
	
	public interface GotoActivateCategoryHandler
	{
		void onGotoActivateCategoryClicked();
	}
	
	public interface GotoRecommendationBreakHandler
	{
		void onGotoRecommendationBreakClicked();
	}


	
	private final VerticalLayout myAccountContent;
	private final NewUserAdminMessagesComponent adminMessagesComponent;
	private final VerticalLayout incompleteAddresses;
	private final VerticalLayout subscriptionInfoLayout;
	private final HorizontalLayout invalidEmailWrapper;
	private final VerticalLayout disabledCategoriesLayout;
	
	private final ShowIncompleteAddressHandler incompleteAddressHandler;
	private final ShowSubscriptionHandler subscriptionHandler;
	private final GotoActivateCategoryHandler gotoActivateCategoryHandler;
	private final GotoRecommendationBreakHandler gotoRecommendationBreakHandler;
	
	private int nrOfIncompleteAddresses;
	private int nrOfAdminMessages;
	private boolean hasInvalidEmail;
	private DefaultNavigator navigator;
	private UserService userService;
	private User currentUser;
	
	public MyAccountComponent(
			ShowIncompleteAddressHandler incompleteAddressHandler,
			ShowSubscriptionHandler subscriptionHandler,
			GotoActivateCategoryHandler gotoActivateCategoryHandler,
			GotoRecommendationBreakHandler gotoRecommendationBreakHandler)
	{
		final VerticalLayout myAccountPanel = new VerticalLayout();
		myAccountPanel.setSizeFull();
		
		myAccountContent = new VerticalLayout();
		myAccountContent.setSizeFull();
		myAccountContent.setStyleName(CssStyle.PANEL_MY_ACCOUNT.getStyleName());
		
		adminMessagesComponent = new NewUserAdminMessagesComponent();
		incompleteAddresses = new VerticalLayout();
		subscriptionInfoLayout = new VerticalLayout();
		invalidEmailWrapper = new HorizontalLayout();
		disabledCategoriesLayout = new VerticalLayout();
		
		this.incompleteAddressHandler = incompleteAddressHandler;
		this.subscriptionHandler = subscriptionHandler;
		this.gotoActivateCategoryHandler = gotoActivateCategoryHandler;
		this.gotoRecommendationBreakHandler = gotoRecommendationBreakHandler;
		
		final Component picture = createHeaderImage();
		myAccountPanel.addComponent(picture);
		myAccountPanel.setComponentAlignment(picture, Alignment.TOP_CENTER);
		myAccountPanel.addComponent(createHeaderText());
		
		myAccountContent.addComponents(adminMessagesComponent, incompleteAddresses, subscriptionInfoLayout, invalidEmailWrapper, disabledCategoriesLayout);
		
		myAccountPanel.addComponents(myAccountContent);
		
		setCompositionRoot(myAccountPanel);
		
		nrOfIncompleteAddresses = 0;
		nrOfAdminMessages = 0;
		hasInvalidEmail = false;

	}
	
	private Component createHeaderImage()
	{
		final HorizontalLayout pic = new HorizontalLayout();
		pic.setStyleName(CssStyle.ACCOUNT_PLACEHOLDER.getStyleName());
		return pic;
	}
	
	private void initInvalidAddresses(User user)
	{
		incompleteAddresses.removeAllComponents();
		
		user.getAddresses().stream().filter(address -> !address.isChecked()).forEach(address ->
		{
			nrOfIncompleteAddresses++;
			incompleteAddresses.addComponent(getIncompleteButtonForAddress(address));
		});
		
		if (incompleteAddresses.getComponentCount() == 0)
			myAccountContent.removeComponent(incompleteAddresses);
	}
	
	private Button getIncompleteButtonForAddress(Address address)
	{
		final Button incompleteButton = new Button(I18N.INCOMPLETE_QUESTIONS_CHECK_ADDRESS_WARN.msg());
		incompleteButton.setData(address);
		incompleteButton.setStyleName(ValoTheme.BUTTON_LINK);
		incompleteButton.setIcon(FontAwesome.CHEVRON_RIGHT);
		
		incompleteButton.addClickListener(event -> incompleteAddressHandler.onAddressClicked(address));
		return incompleteButton;
	}
	
	private Component createHeaderText()
	{
		final HorizontalLayout header = new HorizontalLayout();
		header.setSizeFull();
		header.setStyleName(CssStyle.PANEL_HEADER.getStyleName());
		header.addStyleName(CssStyle.AFFILIATE_GREEN.getStyleName());
		header.addComponent(new Label(I18N.HOMEVIEW_PLACEHOLDER_HEADER.msg()));
		
		return header;
	}
	
	public void setDisabledCategories(Collection<RecommendationCategory> deactivatedCategories, Collection<RecommendationCategory> recommendationBreaks)
	{
		disabledCategoriesLayout.removeAllComponents();
		
		boolean visible = false;
		
		if(!recommendationBreaks.isEmpty())
		{
			final Label recommendationBreakLabel = new Label();
			final String parameter = recommendationBreaks.stream().sorted().map(RecommendationCategory::toString).collect(Collectors.joining(", "));
			recommendationBreakLabel.setValue(I18N.MYACCOUNT_CAPTION_RECOMMENDATIONBREAKS.msg(parameter));
			
			final Button link = new Button(I18N.MYACCOUNT_ACTION_GOTORECOMMENDATIONBREAK.msg());
			link.setStyleName(Reindeer.BUTTON_LINK);
			link.addClickListener(event -> gotoRecommendationBreakHandler.onGotoRecommendationBreakClicked());
			link.setVisible(gotoRecommendationBreakHandler != null);
			
			disabledCategoriesLayout.addComponents(recommendationBreakLabel, link);
			
			visible = true;
		}
		
		if(!deactivatedCategories.isEmpty())
		{
			final Label deactivatedLabel = new Label();
			final String parameter = deactivatedCategories.stream().sorted().map(RecommendationCategory::toString).collect(Collectors.joining(", "));
			deactivatedLabel.setValue(I18N.MYACCOUNT_CAPTION_DEACTIVATEDCATEGORIES.msg(parameter));
			
			final Button link = new Button(I18N.MYACCOUNT_ACTION_GOTOACTIVATECATEGORY.msg());
			link.setStyleName(Reindeer.BUTTON_LINK);
			link.addClickListener(event -> gotoActivateCategoryHandler.onGotoActivateCategoryClicked());
			link.setVisible(gotoActivateCategoryHandler != null);
			
			disabledCategoriesLayout.addComponents(deactivatedLabel, link);
			
			visible = true;
		}
		
		disabledCategoriesLayout.setVisible(visible);
	}
	
	public void setSubscriptionInfo(String infoText, String buttonText)
	{
		subscriptionInfoLayout.removeAllComponents();
		
		boolean visible = false;
		
		if (!Strings.isNullOrEmpty(infoText))
		{
			final Label infoLabel = new Label(infoText);
			subscriptionInfoLayout.addComponent(infoLabel);
			visible = true;
		}
		
		if (!Strings.isNullOrEmpty(buttonText))
		{
			
			final Button subscriptionButton = new Button(buttonText);
			subscriptionButton.addClickListener(event -> subscriptionHandler.onSubscriptionClicked());
			subscriptionButton.setStyleName(ValoTheme.BUTTON_LINK);
			subscriptionButton.setIcon(FontAwesome.CHEVRON_RIGHT);
			
			subscriptionInfoLayout.addComponent(subscriptionButton);
			visible = true;
		}
		
		subscriptionInfoLayout.setVisible(visible);
	}
	
	private void initInvalidEmail(String email)
	{
		HorizontalLayout layoutForButtons = new HorizontalLayout();
		VerticalLayout verticalLayout = new VerticalLayout();
		invalidEmailWrapper.removeAllComponents();
		
		final Label invalidEmailLabel = new Label();
		invalidEmailLabel.setContentMode(ContentMode.HTML);
		invalidEmailLabel.setValue(I18N.MYACCOUNT_INVALID_EMAIL.msg(email));

		Button button = new Button("Email ist aktiv");
		Button button1 = new Button("Meine Email ändern");
		button1.addClickListener(event -> {
			navigator.navigateTo(getViewKey(DefaultNavigatorFactory.MemberMenuItem.USER_DATA, UserDataView.UserDataTab.USERDATA));
			navigator.navigateTo(getViewKey(DefaultNavigatorFactory.MemberMenuItem.USER_DATA, UserDataView.UserDataTab.EMAIL));

		});

		button.addClickListener(event -> {
			try {
				userService.sendChangeMailNew(currentUser);
				UI.getCurrent().getPage().reload();
				//MessageBox.show(I18N.EMAIL_IS_CORRECT_AND_ACTIVE.msg(), MessageBox.MessageBoxButtons.OK, MessageBox.MessageBoxStyle.NONE, null);
			} catch (UniqueValidationException e) {
				e.printStackTrace();
			}
		});
		Label space = new Label();
		layoutForButtons.addComponent(button);
		layoutForButtons.addComponent(space);
		layoutForButtons.addComponent(button1);

		verticalLayout.addComponent(invalidEmailLabel);
		Label space1 = new Label();
		verticalLayout.addComponent(space1);
		verticalLayout.addComponent(layoutForButtons);

		invalidEmailWrapper.addComponent(verticalLayout);
	}
	
	public void setCurrentUser(User currentUser)
	{
		initInvalidAddresses(currentUser);
	}
	
	public Button getUserAdminMessageButton()
	{
		return adminMessagesComponent.getMessagesButton();
	}
	
	public void setNewAdminMessages(Long nrOfMessages)
	{
		if (nrOfMessages > 0)
		{
			nrOfAdminMessages = nrOfMessages.intValue();
			adminMessagesComponent.setNewAdminMessages();
		}
	}
	
	public void setInvalidEmail(Boolean isBlocked, String email, DefaultNavigator navigator, UserService userService, User currentUser)
	{
		this.userService = userService;
		this.currentUser = currentUser;
		this.navigator = navigator;
		if (isBlocked)
		{
			hasInvalidEmail = true;
			initInvalidEmail(email);
		}
	}
	
	public void checkContent()
	{
		adminMessagesComponent.setVisible(nrOfAdminMessages > 0);
		incompleteAddresses.setVisible(nrOfIncompleteAddresses > 0);
		invalidEmailWrapper.setVisible(hasInvalidEmail);
	}

	public String getViewKey(NavigationEnum... navigationEnums)
	{
		return Arrays.stream(navigationEnums)
				.map(NavigationEnum::getPath)
				.collect(Collectors.joining("/"));
	}
}

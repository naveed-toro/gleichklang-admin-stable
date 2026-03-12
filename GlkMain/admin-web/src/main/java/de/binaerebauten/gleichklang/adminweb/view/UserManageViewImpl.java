package de.binaerebauten.gleichklang.adminweb.view;

import com.vaadin.server.*;
import com.vaadin.server.StreamResource.StreamSource;
import com.vaadin.ui.*;
import de.binaerebauten.gleichklang.adminweb.presenter.CsvSeparator;
import de.binaerebauten.gleichklang.adminweb.view.UserManageView.UserManageViewListener;
import de.binaerebauten.gleichklang.adminweb.view.component.UserQuickBar;
import de.binaerebauten.gleichklang.adminweb.view.component.UserQuickBar.UserQuickBarListener;
import de.binaerebauten.gleichklang.adminweb.view.component.UserResetBar;
import de.binaerebauten.gleichklang.core.initializer.AppUI;
import de.binaerebauten.gleichklang.core.model.mail.Newsletter;
import de.binaerebauten.gleichklang.core.model.mail.Newsletter_;
import de.binaerebauten.gleichklang.core.model.mail.UndeliverableMail;
import de.binaerebauten.gleichklang.core.model.mail.UndeliverableMail_;
import de.binaerebauten.gleichklang.core.model.user.*;
import de.binaerebauten.gleichklang.core.repository.user.UserStatisticRepository;
import de.binaerebauten.gleichklang.core.service.file.InMemoryUpload;
import de.binaerebauten.gleichklang.core.utils.filter.FilterSpecificationBuilder;
import de.binaerebauten.gleichklang.core.view.AbstractNavigateView;
import de.binaerebauten.gleichklang.core.view.component.*;
import de.binaerebauten.gleichklang.core.view.component.FilterControlComponent.FilterControlHandler;
import de.binaerebauten.gleichklang.core.view.component.LazyBeanItemContainer.LazyBeanFilteredItemsHandler;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;
import de.binaerebauten.gleichklang.core.view.filter.SimpleStringFilter;
import de.binaerebauten.gleichklang.core.view.filter.SimpleUserFilter.SimpleUserMailFilter;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;


public class UserManageViewImpl extends AbstractNavigateView<UserManageViewListener> implements UserManageView
{
	private static final String EXPORT_DATA_RESOURCE_KEY = "data";
	private static final String EXPORT_FILE_NAME = "export.csv";

	private final LazyBeanTable<User> userTable;
	private final LazyBeanTable<User> blockedUserTable;
	private final LazyBeanPagingComponent<UserStatistic> userStatisticTable;
	private final LazyBeanTable<UndeliverableMail> undeliverableMailTable;
	private final LazyBeanTable<Newsletter> newsletterTable;
	private final ComponentReplacer<FilterControlComponent> userFilterComponent, blockedUserFilterComponent;
	private final ComponentReplacer<FilterControlComponent> undeliverableMailFilterComponent;
	private final UserQuickBar userQuickBar, blockedUserQuickBar;
	private final UserResetBar userResetBar;

	private final SimpleUserMailFilter<UndeliverableMail> undeliverableMailFilter;
	private final NavigationComponent<UserManageTab> navigationComponent;
	private final UserStatisticRepository userStatisticRepository;

	public UserManageViewImpl()
	{
		userQuickBar = new UserQuickBar();
		blockedUserQuickBar = new UserQuickBar();
		userResetBar = new UserResetBar(null);
		if(userQuickBar.confirmBlock != null)
			userQuickBar.confirmBlock.setVisible(false);

		if(blockedUserQuickBar.confirmBlock != null)
			blockedUserQuickBar.confirmBlock.setVisible(false);


		userTable = createUserTable();

		userStatisticRepository= AppUI.getApplicationContext().getBean(UserStatisticRepository.class);
		userStatisticTable =createUserStatisticTable();
		userStatisticTable.setHandler(((LazyBeanItemContainer.LazyBeanFilteredItemsHandler<UserStatistic>)userStatisticRepository::findAll));

		blockedUserTable = createBlockedUserTable();
		undeliverableMailTable = createUndeliverableMailTable();
		newsletterTable = createNewsletterTable();

		userFilterComponent = new ComponentReplacer<>();
		blockedUserFilterComponent = new ComponentReplacer<>();
		undeliverableMailFilterComponent = new ComponentReplacer<>();
		undeliverableMailFilter = new SimpleUserMailFilter<>(UndeliverableMail_.recipientEmail);
		undeliverableMailFilter.setItemComponent(undeliverableMailTable);

		navigationComponent = createNavigationComponent();
		setCompositionRoot(navigationComponent);
	}

	private NavigationComponent<UserManageTab> createNavigationComponent()
	{
		final NavigationComponent<UserManageTab> navigationComponent = new NavigationComponent<>(UserManageTab.class);
		navigationComponent.addNavigation(UserManageTab.USER_CONTROL, createUsersTab());
		navigationComponent.addNavigation(UserManageTab.BLACKLIST, createUndeliverableMailsTab());
		navigationComponent.addNavigation(UserManageTab.BLOCKED, createBlockedUserTab());
		navigationComponent.addNavigation(UserManageTab.NEWSLETTER, createNewsletterTab());
		//navigationComponent.addNavigation(UserManageTab.USER_STATISTICS, createUserStatisticTab());
		return navigationComponent;
	}

	private Component createUsersTab()
	{
		final AbstractOrderedLayout layout = new VerticalLayout();
		layout.setSpacing(true);
		layout.setMargin(true);

		final TableControl<User> tableControl = new TableControl<>(userTable);
		tableControl.addButton(I18N.USERMANAGE_ACTION_RESETOGINS.msg(), () -> fireEvent(UserManageViewListener::resetLogins));
		tableControl.addButton(I18N.USERMANAGE_ACTION_ADDTOBLACKLIST.msg(), item -> fireEvent(action -> action.addToBlacklist(item)));
		tableControl.addButton(I18N.USERMANAGE_ACTION_EXPORT.msg(), () ->
				fireEvent(action ->

				{
					byte[] data = action.export(userTable.getSpecification());

					/*
					 * A workaround to download a dynamically created content without FileDownloader.
					 * See https://vaadin.com/forum#!/thread/3991491 for details.
					 */
					Resource resource = new StreamResource(() -> new ByteArrayInputStream(data), EXPORT_FILE_NAME);
					setResource(EXPORT_DATA_RESOURCE_KEY, resource);
					ResourceReference ref = ResourceReference.create(resource, this, EXPORT_DATA_RESOURCE_KEY);
					Page.getCurrent().open(ref.getURL(), null);
				}));
		tableControl.addButton(I18N.USERMANAGE_ACTION_REFRESH.msg(), userTable::refresh);
		tableControl.addButton(I18N.USERMANAGE_ACTION_BLOCK.msg(),item -> fireEvent(action -> action.addToBlockList(item)));
		tableControl.addButton(I18N.ADMINMESSAGEVIEW_MESSAGE_ANSWER.msg(),item -> fireEvent(eventAction -> eventAction.writeMessage(item)));
		tableControl.addButton("Reminder" ,item -> fireEvent(eventAction -> eventAction.openReminderPopup(item)));
		tableControl.addButton("Show Pin" ,item -> fireEvent(eventAction -> eventAction.showPin(item)));

		layout.addComponent(userFilterComponent);
		layout.addComponent(userQuickBar);
		layout.addComponent(tableControl);

		return layout;
	}

	private Component createUndeliverableMailsTab()
	{
		final AbstractOrderedLayout layout = new VerticalLayout();
		layout.setSpacing(true);
		layout.setMargin(true);

		final ComboBox separatorComboBox = ComponentFactory.getInstance().createField(CsvSeparator.class, ComboBox.class);
		separatorComboBox.setValue(CsvSeparator.COMMA);

		final UploadComponent<InMemoryUpload> uploadComponent = new UploadComponent<>("", "Upload CSV", new InMemoryUpload());
		uploadComponent.setUploadFileChangedListener((uploadFile, uploadResult) -> getListener().onBlackListUpload(uploadFile, uploadResult, (CsvSeparator) separatorComboBox.getValue()));

		final TableControl<UndeliverableMail> tableControl = new TableControl<>(undeliverableMailTable);
		tableControl.setDeleteCallback(item -> fireEvent(e -> e.removeFromBlacklist(item)));
		tableControl.setButtonCaptions("", "", I18N.USERMANAGE_ACTION_DELETEFROMBLACKLIST.msg());
		// Adding the button for viewing the user profile
		tableControl.addButton(I18N.USERMANAGE_ACTION_SHOW.msg(), item -> fireEvent(e -> e.ShowBlackListedUserProfile(item)));
		layout.addComponent(undeliverableMailFilterComponent);
		layout.addComponent(separatorComboBox);
		layout.addComponent(uploadComponent);
		layout.addComponent(tableControl);

		return layout;
	}

	private Component createNewsletterTab()
	{
		final AbstractOrderedLayout layout = new VerticalLayout();
		layout.setSpacing(true);
		layout.setMargin(true);

		final SimpleStringFilter<Newsletter> newsletterMailFilter = new SimpleStringFilter<>(Newsletter_.email);
		newsletterMailFilter.setItemComponent(newsletterTable);

		final TextField mailTextField = ComponentFactory.getInstance().createField(TextField.class, "E-Mail");
		mailTextField.addTextChangeListener(event -> newsletterMailFilter.setValue(event.getText()));
		mailTextField.setSizeUndefined();

		final UploadComponent<InMemoryUpload> uploadComponent = new UploadComponent<>("", "Upload CSV", new InMemoryUpload());
		uploadComponent.setUploadFileChangedListener((uploadFile, uploadResult) -> getListener().onNewsletterUpload(uploadFile, uploadResult));

		final TableControl<Newsletter> tableControl = new TableControl<>(newsletterTable);
		tableControl.addButton(I18N.USERMANAGE_ACTION_DELETENEWSLETTER.msg(), item -> getListener().deleteFromNewsletter(item));

		final Button exportButton = new Button("CSV Export");

		final FileDownloader fileDownloader = new FileDownloader(new StreamResource((StreamSource) () -> getListener().exportNewsletter(), EXPORT_FILE_NAME));
		fileDownloader.extend(exportButton);

		final HorizontalLayout csvLayout = new HorizontalLayout();
		csvLayout.setSpacing(true);
		csvLayout.addComponents(uploadComponent, exportButton);

		layout.addComponent(mailTextField);
		layout.addComponent(csvLayout);
		layout.addComponent(tableControl);

		return layout;
	}


	private Component createBlockedUserTab()
	{
		final AbstractOrderedLayout layout = new VerticalLayout();
		layout.setSpacing(true);
		layout.setMargin(true);

		final TableControl<User> tableControl = new TableControl<>(blockedUserTable);
		tableControl.addButton("Unblock", item -> fireEvent(action -> action.addToUnblockList(item)));
		tableControl.addButton("Confirm block", item -> fireEvent(action -> action.adminBlocked(item)));

		//Adding the button for view the user profile (Same like show button functionality)
		tableControl.addButton(I18N.USERMANAGE_ACTION_SHOW.msg(), item -> fireEvent(action -> action.ShowUserProfile(item)));

		layout.addComponent(blockedUserFilterComponent);
		layout.addComponent(tableControl);


		return layout;

	}

	/*private Component createUserStatisticTab()
	{
		final AbstractOrderedLayout layout = new VerticalLayout();
		layout.setSpacing(true);
		layout.setMargin(true);

		final TableControl<UserStatistic> tableControl = new TableControl<>(userStatisticTable);
		layout.addComponent(tableControl);


		return layout;

	}*/
	private LazyBeanTable<User> createUserTable()
	{
		final LazyBeanTable<User> table = new LazyBeanTable<>();
		table.setSelectable(true);
		table.setSizeFull();

		table.addContainerProperty(I18N.USERMANAGE_HEADER_EMAIL.msg(), User_.email);
		table.addContainerProperty(I18N.USERMANAGE_HEADER_ALIAS.msg(), User_.alias);
		table.addContainerProperty(I18N.USERMANAGE_HEADER_LASTNAME.msg(), User_.lastName);
		table.addContainerProperty(I18N.USERMANAGE_HEADER_FIRSTNAME.msg(), User_.firstName);
		table.addContainerProperty(I18N.USERMANAGE_HEADER_STATUS.msg(), User_.memberStatus);
		table.addContainerProperty(I18N.USERMANAGE_HEADER_CONFIRMED.msg(), User_.emailConfirmed);
		table.addGeneratedColumn(I18N.USERMANAGE_HEADER_BLACKLISTED.msg(), user -> getListener().isBlacklisted(user));
		table.addContainerProperty("Blocked State", User_.blockedStatus);
		table.addGeneratedColumn("Subscription State", user -> getListener().getCurrentSubscription(user).getState());
		table.addGeneratedColumn("Last Login", user -> getListener(). lastLoginDate(user));
		table.addValueChangeListener(values -> userQuickBar.onUserChanged(table.getValue()));
		table.setCellStyleGenerator((source, itemId, propertyId) ->
		{
			if (User_.emailConfirmed.getName().equals(propertyId))
				return itemId.isEmailConfirmed() ? null : CssStyle.DANGER;

			if (I18N.USERMANAGE_HEADER_BLACKLISTED.msg().equals(propertyId))
				return getListener().isBlacklisted(itemId) ? CssStyle.DANGER : null;

			if(User_.blockedStatus.getName().equals(propertyId))
				return (itemId.getBlockedStatus().equals(BlockedStatus.BLOCKED)) ? CssStyle.DANGER : (itemId.getBlockedStatus().equals(BlockedStatus.ADMIN_BLOCKED))? CssStyle.DANGER : null;

			return null;
		});

		return table;
	}

	private LazyBeanPagingComponent<UserStatistic> createUserStatisticTable()
	{
		final LazyBeanPagingComponent<UserStatistic> table = new LazyBeanPagingComponent<>();

		table.setSizeFull();
		table.setSelectable(false);
		table.setSortPropertyId(false, UserStatistic_.createDate);
		table.addGeneratedColumn(I18N.USERSTATISTICTABLE_HEADER_STARTDATE.msg(), UserStatistic::getStartDate);
		table.addGeneratedColumn(I18N.USERSTATISTICTABLE_HEADER_ENDDATE.msg(), UserStatistic::getEndDate);
		table.addGeneratedColumn(I18N.USERSTATISTICTABLE_HEADER_DURATION.msg(), UserStatistic::getDuration);
		table.addGeneratedColumn(I18N.USERSTATISTICTABLE_HEADER_STATUS.msg(), UserStatistic::getStatus);

		return table;
	}
	private LazyBeanTable<User> createBlockedUserTable()
	{
		final LazyBeanTable<User> table = new LazyBeanTable<>();
		table.setSelectable(true);
		table.setSizeFull();


		table.addContainerProperty(I18N.USERMANAGE_HEADER_EMAIL.msg(), User_.email);
		table.addContainerProperty(I18N.USERMANAGE_HEADER_ALIAS.msg(), User_.alias);
		table.addContainerProperty(I18N.USERMANAGE_HEADER_LASTNAME.msg(), User_.lastName);
		table.addContainerProperty(I18N.USERMANAGE_HEADER_FIRSTNAME.msg(), User_.firstName);
		table.addContainerProperty(I18N.USERMANAGE_HEADER_STATUS.msg(), User_.memberStatus);
		table.addContainerProperty(I18N.USERMANAGE_HEADER_CONFIRMED.msg(), User_.emailConfirmed);
		//table.addContainerProperty("Is Blocked", User_.isBlocked);
		table.addGeneratedColumn(I18N.USERMANAGE_HEADER_BLACKLISTED.msg(), user -> getListener().isBlacklisted(user));
		/* auskommentiert, da der Chat aktuell nicht aktiviert ist */
		//		userTable.addGeneratedColumn(I18N.USERMANAGE_HEADER_ONLINE.msg(), user -> getListener().isOnline(user));

		table.addContainerProperty(I18N.USERMANAGE_HEADER_BLOCKEDDATE.msg(), User_.blockedDate);
		table.addContainerProperty(I18N.USERMANAGE_HEADER_ADMINBLOCKEDDATE.msg(), User_.adminBlockedDate);
		table.addContainerProperty("Blocked Status", User_.blockedStatus);

		table.addValueChangeListener(values -> userQuickBar.onUserChanged(table.getValue()));
		table.setCellStyleGenerator((source, itemId, propertyId) ->
		{
			if (User_.emailConfirmed.getName().equals(propertyId))
				return itemId.isEmailConfirmed() ? null : CssStyle.DANGER;

			if (I18N.USERMANAGE_HEADER_BLACKLISTED.msg().equals(propertyId))
				return getListener().isBlacklisted(itemId) ? CssStyle.DANGER : null;

			return null;
		});


		return table;
	}

	private LazyBeanTable<UndeliverableMail> createUndeliverableMailTable()
	{
		final LazyBeanTable<UndeliverableMail> table = new LazyBeanTable<>();
		table.setSelectable(true);
		table.setSizeFull();

		table.addContainerProperty(I18N.USERMANAGE_HEADER_EMAIL.msg(), UndeliverableMail_.recipientEmail);
		table.addContainerProperty(I18N.USERMANAGE_HEADER_UNDELIVERABLEREASON.msg(), UndeliverableMail_.undeliverableMailReason);
		table.addContainerProperty(I18N.USERMANAGE_HEADER_INCIDENTS.msg(), UndeliverableMail_.incidents);
		table.addContainerProperty(I18N.USERMANAGE_HEADER_CREATEDATE.msg(), UndeliverableMail_.createDate);
		table.addContainerProperty(I18N.USERMANAGE_HEADER_CHANGEDATE.msg(), UndeliverableMail_.changeDate);

		return table;
	}

	private LazyBeanTable<Newsletter> createNewsletterTable()
	{
		final LazyBeanTable<Newsletter> table = new LazyBeanTable<>();
		table.setSelectable(true);
		table.setSizeFull();

		table.addContainerProperty(I18N.USERMANAGE_HEADER_EMAIL.msg(), Newsletter_.email);
		table.addContainerProperty(I18N.USERMANAGE_HEADER_CONFIRMATIONIP.msg(), Newsletter_.confirmationIp);
		table.addContainerProperty(I18N.USERMANAGE_HEADER_CONFIRMATIONDATE.msg(), Newsletter_.confirmationDate);

		return table;
	}

	@Override
	public void setUserHandler(LazyBeanFilteredItemsHandler<User> handler)
	{
		userTable.setHandler(handler);
		userTable.clearValue();
	}

	@Override
	public void setBlockedUserHandler(LazyBeanFilteredItemsHandler<User> handler) {

		blockedUserTable.setHandler(handler);


	}

	@Override
	public void setUndeliverableMailHandler(LazyBeanFilteredItemsHandler<UndeliverableMail> handler)
	{
		undeliverableMailTable.setHandler(handler);
	}

	@Override
	public void setNewsletterHandler(LazyBeanFilteredItemsHandler<Newsletter> handler)
	{
		newsletterTable.setHandler(handler);
	}

	@Override
	public void setBlockedUserFilterHandlerAndBuilder(FilterControlHandler filterControlHandler, FilterSpecificationBuilder filterSpecificationBuilder) {

		blockedUserTable.removeAllFilters();

		if (filterControlHandler == null)
		{
			blockedUserFilterComponent.setComponent(null);
		}
		else
		{
			final FilterControlComponent filterControlComponent = new FilterControlComponent(null, filterControlHandler, filterSpecificationBuilder);
			filterControlComponent.addFilterChangedListener(blockedUserTable::setFilter);
			blockedUserFilterComponent.setComponent(filterControlComponent);
		}
	}


	@Override
	public void setUserFilterHandlerAndBuilder(FilterControlHandler filterControlHandler, FilterSpecificationBuilder filterSpecificationBuilder)
	{
		userTable.removeAllFilters();

		if (filterControlHandler == null)
		{
			userFilterComponent.setComponent(null);
		}
		else
		{
			final FilterControlComponent filterControlComponent = new FilterControlComponent(null, filterControlHandler, filterSpecificationBuilder);
			filterControlComponent.addFilterChangedListener(userTable::setFilter);
			userFilterComponent.setComponent(filterControlComponent);
		}
	}

	@Override
	public void setUndeliverableMailFilterHandlerAndBuilder(FilterControlHandler filterControlHandler, FilterSpecificationBuilder filterSpecificationBuilder)
	{
		undeliverableMailFilter.setValue(null);
		undeliverableMailFilterComponent.setComponent(null);

		if (filterControlHandler != null)
		{
			final FilterControlComponent filterControlComponent = new FilterControlComponent(null, filterControlHandler, filterSpecificationBuilder);
			filterControlComponent.addFilterChangedListener(undeliverableMailFilter::setValue);
			undeliverableMailFilterComponent.setComponent(filterControlComponent);
		}
	}

	@Override
	public void setListener(UserManageViewListener listener)
	{
		super.setListener(listener);
		navigationComponent.setSubNavigationListener(listener);
	}

	@Override
	public void selectSubNavigation(UserManageTab navigationEnum)
	{
		navigationComponent.setSelectedNavigation(navigationEnum);
	}

	@Override
	public void setUserQuickBarListener(UserQuickBarListener userQuickBarListener)
	{
		userQuickBar.setListener(userQuickBarListener);
	}

	@Override
	public void refreshBlockedUsers() {
		blockedUserTable.refresh();
	}

}

package de.binaerebauten.gleichklang.adminweb.view.popup.usermanage;

import com.vaadin.server.FileDownloader;
import com.vaadin.server.StreamResource;
import com.vaadin.server.StreamResource.StreamSource;
import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.ui.*;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import de.binaerebauten.gleichklang.adminweb.view.component.*;
import de.binaerebauten.gleichklang.adminweb.view.component.AdminWorkItemTable.Directory;
import de.binaerebauten.gleichklang.adminweb.view.component.InvoiceTable.InvoiceHandler;
import de.binaerebauten.gleichklang.adminweb.view.component.SubscriptionTable.SubscriptionHandler;
import de.binaerebauten.gleichklang.adminweb.view.model.UserControlData;
import de.binaerebauten.gleichklang.adminweb.view.popup.I18N;
import de.binaerebauten.gleichklang.adminweb.view.popup.usermanage.EmailComponent.EmailComponentListener;
import de.binaerebauten.gleichklang.adminweb.view.popup.usermanage.OverviewComponent.OverviewComponentListener;
import de.binaerebauten.gleichklang.core.model.ActiveDeactiveSubscription;
import de.binaerebauten.gleichklang.core.model.BaseEntity_;
import de.binaerebauten.gleichklang.core.model.matching.MatchStatistic_;
import de.binaerebauten.gleichklang.core.model.matching.Relationship;
import de.binaerebauten.gleichklang.core.model.matching.Relationship_;
import de.binaerebauten.gleichklang.core.model.message.AdminWorkItem;
import de.binaerebauten.gleichklang.core.model.message.Message;
import de.binaerebauten.gleichklang.core.model.payment.Invoice;
import de.binaerebauten.gleichklang.core.model.payment.Subscription;
import de.binaerebauten.gleichklang.core.model.user.*;
import de.binaerebauten.gleichklang.core.model.user.ClientInformation.Device;
import de.binaerebauten.gleichklang.core.presenter.question.QuestionnairePresenter;
import de.binaerebauten.gleichklang.core.service.LocatableHandler;
import de.binaerebauten.gleichklang.core.service.validator.ValidationException;
import de.binaerebauten.gleichklang.core.utils.StringUtils;
import de.binaerebauten.gleichklang.core.view.QuestionnaireView;
import de.binaerebauten.gleichklang.core.view.component.*;
import de.binaerebauten.gleichklang.core.view.component.ComponentReplacer.SimpleReplacer;
import de.binaerebauten.gleichklang.core.view.component.LazyBeanItemContainer.LazyBeanFilteredItemsHandler;
import de.binaerebauten.gleichklang.core.view.component.UserProfile.UserProfileData;
import de.binaerebauten.gleichklang.core.view.component.UserProfile.UserProfileListener;
import de.binaerebauten.gleichklang.core.view.component.converter.LocalDateConverter;
import de.binaerebauten.gleichklang.core.view.component.message.MessageTable;
import de.binaerebauten.gleichklang.core.view.component.validator.SaveHelper;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;
import de.binaerebauten.gleichklang.core.view.filter.SimpleDateFilter;
import de.binaerebauten.gleichklang.core.view.filter.SimpleUserFilter.SimpleUserIdFilter;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static de.binaerebauten.gleichklang.core.model.user.SignableUser_.alias;

public class UserManagePopup extends Popup
{
	public interface UserControlListener extends UserProfileListener, LocatableHandler, SubscriptionHandler, InvoiceHandler,ActivateDeactivateTable.ActivateDeactivateHandler,ClientInformationTable.ClientInformationHandler, OverviewComponentListener, EmailComponentListener
	{
		void save(UserControlData userControlData) throws ValidationException;

		void openMessage(Message message, User user);

		void openAdminWorkItem(AdminWorkItem item, Directory directory);

		List<QuestionnairePresenter> getQuestionnairePresenter(RecommendationCategory category, User user);

		UserProfileData getUserProfileData(User user, RecommendationCategory category);

		void newRelationship(UserManagePopup sender, User user);

		void deleteRelationship(UserManagePopup sender, Relationship relationship);

		void undeleteRelationship(UserManagePopup sender, Relationship relationship);
		InputStream exportUserData(User user);
		InputStream exportQuestionsAndAnswers(User user);
		InputStream exportImagesAndAvatars(User user);
	}
	private String menuClicked;
	// helper for better readable
	private class MenuItem extends de.binaerebauten.gleichklang.core.view.component.MenuItem<Supplier<Component>>
	{
		public MenuItem(String caption, Supplier<Component> content)
		{
			super(caption, content);
		}
	}

	private final SubscriptionTable subscriptionTable;
	private final InvoiceTable invoiceTable;
	private final ActivateDeactivateTable activateDeactivateTable;
	private final ClientInformationTable clientInformationTable;
	private final LazyBeanPagingComponent<Relationship> relationshipTable;
	private final UserControlListener userControlListener;
	private final UserControlData userControlData;
	private final Map<QuestionnaireView, QuestionnairePresenter> cachedQuestionnairePresenters = new HashMap<>();
	private final SimpleReplacer content = new SimpleReplacer();
	private final MenuTree menuTree;
	private final Admin currentAdmin;

	public UserManagePopup(UserControlData userControlData, UserControlListener userControlListener, Admin currentAdmin)
	{
		Objects.requireNonNull(userControlListener);
		Objects.requireNonNull(userControlData);
		this.currentAdmin = currentAdmin;
		this.userControlListener = userControlListener;
		this.userControlData = userControlData;

		this.subscriptionTable = new SubscriptionTable();
		this.subscriptionTable.setSubscriptionHandler(userControlListener);
		this.subscriptionTable.setNewSubscriptionListener(() -> userControlListener.newSubscription(userControlData.getUser()));

		this.invoiceTable = new InvoiceTable();
		this.activateDeactivateTable= new ActivateDeactivateTable();
		this.clientInformationTable = new ClientInformationTable();
		this.invoiceTable.addControlButtons(userControlListener);

		//	this.memoText = createMemoTextTab();

		relationshipTable = createRelationshipTable();

		addStyleName(CssStyle.ADMIN_USERMANAGEPOPUP_WRAPPER.getStyleName());

		final HorizontalSplitPanel layout = new HorizontalSplitPanel();
		layout.setSizeFull();

		menuTree = createMenuTree();
		layout.addComponent(menuTree);
		layout.addComponent(content);

		layout.setSplitPosition(25);

		setContent(layout);

		menuTree.selectFirstChild();
	}

	private MenuItem addChild(String child, Supplier<Component> content, MenuItem parent)
	{
		final MenuItem menuItem = new MenuItem(child, content);
		parent.addChild(menuItem);
		return menuItem;
	}

	private MenuTree createMenuTree()
	{
		final MenuTree menuTree = new MenuTree();

		final MenuItem overview = new MenuItem(I18N.USERMANAGEPOPUP_CAPTION_OVERVIEW.msg(), null);
		addChild(I18N.USERMANAGEPOPUP_CAPTION_HOMETAB.msg(), this::createHomeTab, overview);
		addChild(I18N.USERMANAGEPOPUP_CAPTION_STATISTICTAB.msg(), this::createStatisticTab, overview);

		final MenuItem userData = new MenuItem(I18N.USERMANAGEPOPUP_CAPTION_USERDATE.msg(), null);
		addChild(I18N.USERMANAGEPOPUP_CAPTION_REGISTERTAB.msg(), this::createRegisterTab, userData);
		addChild(I18N.USERMANAGEPOPUP_CAPTION_USERTAB.msg(), this::createUserTab, userData);
		addChild(I18N.USERMANAGEPOPUP_CAPTION_WEBCLIENTS.msg(),this::initWebClientsTable, userData);
		addChild(I18N.USERMANAGEPOPUP_CAPTION_DATAINFORMATION.msg(), this::createDataInformationTab, userData);

		//final MenuItem memoText = new MenuItem(I18N.USERMANAGE_POPUP_MEMO_TEXT.msg(), this::createMemoTextTab);


		final MenuItem subscription = new MenuItem(I18N.USERMANAGEPOPUP_CAPTION_SUBSCRIPTIONSANDINVOICES.msg(), null);
		addChild(I18N.USERMANAGEPOPUP_CAPTION_SUBSCRIPTIONTAB.msg(), this::initSubscriptionTable, subscription);
		addChild("ActivateDeactivate", this::activateDeactivateTable, subscription);
		addChild(I18N.USERMANAGEPOPUP_CAPTION_INVOICETAB.msg(), this::initInvoiceTable, subscription);
		addChild(I18N.USERMANAGEPOPUP_CAPTION_MAILTAB.msg(), this::createEmailComponent, subscription);

		final MenuItem communication = new MenuItem(I18N.USERMANAGEPOPUP_CAPTION_COMMUNICATIONWITHGK.msg(), null);
		addChild(I18N.USERMANAGEPOPUP_CAPTION_ADMINMESSAGETAB.msg(), this::createAdminMessageTab, communication);

		final MenuItem matching = new MenuItem(I18N.USERMANAGEPOPUP_CAPTION_MATCHING.msg(), null);
		addChild(I18N.USERMANAGEPOPUP_CAPTION_PERSONALPROFILETAB.msg(), () -> createCategoryQuestionnaireTabSheet(null), matching);

		final User user = userControlData.getUser();
		for (RecommendationCategory category : user.getOrderedCategories())
		{
			final MenuItem categoryMenuItem = addChild(category.toString(), null, matching);
			addChild(I18N.USERMANAGEPOPUP_CAPTION_PROFILETAB.msg(category.msg()), () -> createUserProfile(category), categoryMenuItem);

			addChild(I18N.USERMANAGEPOPUP_CAPTION_QUESTIONTAB.msg(category.msg()), () -> createCategoryQuestionnaireTabSheet(category), categoryMenuItem);
		}

		final MenuItem memoText = new MenuItem(I18N.USERMANAGE_POPUP_MEMO_TEXT.msg(), this::createMemoTextTab);//todo change by anil

		addChild(I18N.USERMANAGEPOPUP_CAPTION_MESSAGETAB.msg(), this::createMessageTab, matching);
		addChild(I18N.USERMANAGEPOPUP_CAPTION_RELATIONSHIPTAB.msg(), this::createRelationshipAdministration, matching);
		addChild(I18N.USERMANAGEPOPUP_CAPTION_REPORTSTAB.msg(), this::createUserReportsTab, matching);

		menuTree.addMenuItem(overview);
		menuTree.addMenuItem(userData);
		menuTree.addMenuItem(subscription);
		menuTree.addMenuItem(communication);
		menuTree.addMenuItem(matching);
		menuTree.addMenuItem(memoText);
		menuTree.setAutoExpandSelection(true);
		menuTree.setAutoSelector(true);

		menuTree.addValueChangeListener(event -> {
			menuClicked=((Field.ValueChangeEvent) event).getSource().toString();
			refreshView();
		});

		return menuTree;
	}

	private EmailComponent createEmailComponent()
	{
		return new EmailComponent(userControlListener, userControlData.getUser());
	}

	private Component initSubscriptionTable()
	{
		userControlListener.refreshSubscriptionTable();
		return subscriptionTable;
	}

	private Component initWebClientsTable(){
		userControlListener.refreshClientInformationTable();
		return clientInformationTable;
	}

	private Component initInvoiceTable()
	{
		userControlListener.refreshInvoiceTable();
		return invoiceTable;
	}

	private Component activateDeactivateTable()
	{
		userControlListener.refreshActivateDeactivateTable();
		return activateDeactivateTable;
	}

	public void refreshView()
	{
		if (menuTree.getValue() instanceof MenuItem)
		{
			final MenuItem menuItem = (MenuItem) menuTree.getValue();
			if (menuItem.getContent() != null)
				content.setComponent(menuItem.getContent().get());
		}
	}

	private Component createHomeTab()
	{
		return new OverviewComponent(this, userControlListener, userControlData);
	}

	private LazyBeanPagingComponent<Relationship> createRelationshipTable()
	{
		final LazyBeanPagingComponent<Relationship> table = new LazyBeanPagingComponent<>();
		table.setSizeFull();
		table.setSelectable(true);

		table.addGeneratedColumn(de.binaerebauten.gleichklang.adminweb.view.I18N.RELATIONSHIPTABLE_HEADER_SOURCEUSER.msg(), item->item.getSourceUser().getAlias());
		table.addGeneratedColumn(de.binaerebauten.gleichklang.adminweb.view.I18N.RELATIONSHIPTABLE_HEADER_TARGETUSER.msg(), item->item.getTargetUser().getAlias());
		table.addGeneratedColumn(de.binaerebauten.gleichklang.adminweb.view.I18N.RELATIONSHIPTABLE_HEADER_AFFILIATION.msg(), item->item.getAffiliation());
		table.addGeneratedColumn(de.binaerebauten.gleichklang.adminweb.view.I18N.RELATIONSHIPTABLE_HEADER_VIEWED.msg(), item->item.isViewed());
		table.addGeneratedColumn("Create Date", item->item.getCreateDate());
		table.setSortPropertyId(false, BaseEntity_.createDate);
		table.addGeneratedColumn(de.binaerebauten.gleichklang.adminweb.view.I18N.TABLE_HEADER_DELETED.msg(),  item->item.isDeleted());
		table.addGeneratedColumn(de.binaerebauten.gleichklang.adminweb.view.I18N.RELATIONSHIPTABLE_HEADER_CATEGORY.msg(), (relationship) -> relationship.getCategories().stream().map(RecommendationCategory::getName).collect(Collectors.joining(", ")));
		table.addGeneratedColumn("First Viewed", item->item.getFirstViewed());
		table.addGeneratedColumn("Deleted Date", item->item.getDeleteDate());
		table.addGeneratedColumn("Deleted By", item->item.getDeletedBy());

		return table;
	}

	private Component createRelationshipAdministration()
	{
		final VerticalLayout layout = new VerticalLayout();
		layout.setMargin(true);
		layout.setSpacing(true);

		final VerticalLayout layout1 = new VerticalLayout();
		layout.setMargin(true);
		layout.setSpacing(true);

		final VerticalLayout layout2 = new VerticalLayout();
		layout.setMargin(true);
		layout.setSpacing(true);

		HorizontalLayout horizontalLayout = new HorizontalLayout();
		layout.setMargin(true);
		layout.setSpacing(true);

		final SimpleUserIdFilter<Relationship> relationshipFilter = new SimpleUserIdFilter<>(Arrays.asList(Relationship_.sourceUserId, Relationship_.targetUserId));
		relationshipFilter.setItemComponent(relationshipTable);

		final SimpleDateFilter<Relationship> dateFilter = new SimpleDateFilter<>(Relationship_.createDate);
		dateFilter.setItemComponent(relationshipTable);

        final SimpleDateFilter<Relationship> dateFilterDeleted = new SimpleDateFilter<>(Relationship_.deleteDate);
        dateFilterDeleted.setItemComponent(relationshipTable);

		final DateField dateFieldFrom = ComponentFactory.getInstance().createField(DateField.class, I18N.USERMANAGEPOPUP_CAPTION_CREATEDATEFILTER_FROM.msg());

		final DateField dateFieldTo = ComponentFactory.getInstance().createField(DateField.class, I18N.USERMANAGEPOPUP_CAPTION_CREATEDATEFILTER_TO.msg());

        final DateField dateFieldDeletedFrom = ComponentFactory.getInstance().createField(DateField.class, "Deleted From");

        final DateField dateFieldDeletedTo = ComponentFactory.getInstance().createField(DateField.class, "Deleted To");

		dateFieldFrom.setSizeUndefined();
		dateFieldFrom.setResolution(Resolution.DAY);
		dateFieldFrom.setConverter(new LocalDateConverter());
		dateFieldFrom.addValueChangeListener(event -> dateFilter.setValue((LocalDate) dateFieldFrom.getConvertedValue(), (LocalDate) dateFieldTo.getConvertedValue(),true));

		dateFieldTo.setSizeUndefined();
		dateFieldTo.setResolution(Resolution.DAY);
		dateFieldTo.setConverter(new LocalDateConverter());
		dateFieldTo.addValueChangeListener(event -> dateFilter.setValue((LocalDate) dateFieldFrom.getConvertedValue(), (LocalDate) dateFieldTo.getConvertedValue(),true));


        dateFieldDeletedFrom.setSizeUndefined();
        dateFieldDeletedFrom.setResolution(Resolution.DAY);
        dateFieldDeletedFrom.setConverter(new LocalDateConverter());
        dateFieldDeletedFrom.addValueChangeListener(event -> dateFilterDeleted.setValue((LocalDate) dateFieldDeletedFrom.getConvertedValue(), (LocalDate) dateFieldDeletedTo.getConvertedValue(),true));

        dateFieldDeletedTo.setSizeUndefined();
        dateFieldDeletedTo.setResolution(Resolution.DAY);
        dateFieldDeletedTo.setConverter(new LocalDateConverter());
        dateFieldDeletedTo.addValueChangeListener(event -> dateFilterDeleted.setValue((LocalDate) dateFieldDeletedFrom.getConvertedValue(), (LocalDate) dateFieldDeletedTo.getConvertedValue(),true));


        final FilterControlComponent relationshipFilterComponent = new FilterControlComponent(userControlData.getRelationshipFilterControlHandler(), userControlData.getFilterSpecificationBuilder());
		relationshipFilterComponent.addFilterChangedListener(relationshipFilter::setValue);
		if(menuClicked!=null) {
			relationshipTable.setDescription(menuClicked);
		}
		final TableControl<Relationship> tableControl = new TableControl<>(relationshipTable);

		tableControl.setCurrenSelectedUser(userControlData.getUser());
		tableControl.onlyDeletedSuggestions(userControlData.getUser());
		tableControl.setNewCallback(() -> userControlListener.newRelationship(this, userControlData.getUser()));
		tableControl.setDeleteCallback(item -> userControlListener.deleteRelationship(this, item));
		tableControl.setUndeleteCallback(item -> userControlListener.undeleteRelationship(this, item));
		layout1.addComponents(dateFieldFrom,dateFieldTo);
		Label label = new Label("           ");
		layout2.addComponents(dateFieldDeletedFrom,dateFieldDeletedTo);
		horizontalLayout.addComponents(layout1,label,layout2);
		layout.addComponents(relationshipFilterComponent, horizontalLayout, tableControl);

		relationshipTable.setHandler(userControlData.getRelationshipHandler());

		return layout;
	}

	private TabSheet createAdminMessageTab()
	{
		final TabSheet tabSheet = new TabSheet();
		tabSheet.setStyleName(CssStyle.MESSAGE_VIEW_PANEL.getStyleName());

		for (Directory directory : userControlData.getAdminMessageHandlerMap().keySet())
		{
			final AdminWorkItemTable adminMessageTable = new AdminWorkItemTable(directory);
			adminMessageTable.addItemClickListener(item -> userControlListener.openAdminWorkItem(item, directory));
			tabSheet.addTab(adminMessageTable, directory.toString());
		}

		tabSheet.addSelectedTabChangeListener(event -> onAdminMessageTabClicked(event.getTabSheet()));
		onAdminMessageTabClicked(tabSheet);

		return tabSheet;
	}

	private void onAdminMessageTabClicked(TabSheet adminMessageTabSheet)
	{
		final Component selectedTab = adminMessageTabSheet.getSelectedTab();
		if(selectedTab instanceof AdminWorkItemTable)
		{
			final AdminWorkItemTable adminMessageTable = ((AdminWorkItemTable) selectedTab);
			adminMessageTable.setAdminWorkItemsHandler(userControlData.getAdminMessageHandlerMap().get(adminMessageTable.getDirectory()));
		}
	}

	private TabSheet createCategoryQuestionnaireTabSheet(RecommendationCategory category)
	{
		final List<QuestionnairePresenter> questionnairePresenters = userControlListener.getQuestionnairePresenter(category, userControlData.getUser());

		final TabSheet tabSheet = new TabSheet();
		tabSheet.addSelectedTabChangeListener(event -> loadQuestions(event.getTabSheet()));
		cachedQuestionnairePresenters.clear();

		for (QuestionnairePresenter questionnairePresenter : questionnairePresenters)
		{
			final QuestionnaireView questionnaireView = questionnairePresenter.getQuestionnaireView();
			questionnaireView.setMargin(true);
			tabSheet.addTab(questionnaireView, questionnaireView.getQuestionnaire().getName());

			cachedQuestionnairePresenters.put(questionnaireView, questionnairePresenter);
		}

		loadQuestions(tabSheet);

		return tabSheet;
	}

	private void loadQuestions(TabSheet categoryTabSheet)
	{
		final Component selectedChildTab = categoryTabSheet.getSelectedTab();

		if (selectedChildTab instanceof QuestionnaireView)
		{
			final QuestionnairePresenter questionnairePresenter = cachedQuestionnairePresenters.get(selectedChildTab);
			if (questionnairePresenter != null)
				questionnairePresenter.createSubViews();
		}
	}

	private void onMessageTabClicked(SelectedTabChangeEvent event)
	{
		final MessageTable messageTable = ((MessageTable) event.getTabSheet().getSelectedTab());
		messageTable.setMessagesHandler(userControlData.getMessageHandlerMap().get(messageTable.getDirectory()));
	}

	private TabSheet createMessageTab()
	{
		final TabSheet tabSheet = new TabSheet();
		tabSheet.setStyleName(CssStyle.MESSAGE_VIEW_PANEL.getStyleName());

		for (MessageTable.Directory directory : userControlData.getMessageHandlerMap().keySet())
		{
			final MessageTable messageTable = new MessageTable(directory, false, Device.getDefault(), currentAdmin);
			messageTable.setOpenButtonClickListener((message) -> userControlListener.openMessage(message, userControlData.getUser()));
			tabSheet.addTab(messageTable, directory.toString());
		}

		tabSheet.addSelectedTabChangeListener(this::onMessageTabClicked);
		tabSheet.setSelectedTab(0);

		return tabSheet;
	}

	private Component createUserTab()
	{
		final ComponentGroup<UserControlData> componentGroup = new ComponentGroup<>(UserControlData.class, userControlData);
		final List<AddressPanel> addressPanels = new ArrayList<>();

		final SaveHelper saveHelper = new SaveHelper(() -> userControlListener.save(componentGroup.getItemDataSource().getBean()));
		saveHelper.setIgnoreEmptyValueExceptions(true);

		final VerticalLayout layout = new VerticalLayout();
		layout.setMargin(true);
		layout.setSpacing(true);

		layout.addComponent(saveHelper.getValidationComponent());

		layout.addComponent(componentGroup.buildAndBind(I18N.USERMANAGE_POPUP_ALIAS.msg(), UserControlData.user_, User_.alias));
		layout.addComponent(componentGroup.buildAndBind(I18N.USERMANAGE_POPUP_LASTNAME.msg(), UserControlData.user_, User_.lastName));
		layout.addComponent(componentGroup.buildAndBind(I18N.USERMANAGE_POPUP_FIRSTNAME.msg(), UserControlData.user_, User_.firstName));
		layout.addComponent(componentGroup.buildAndBind(I18N.USERMANAGE_POPUP_SEX.msg(), LabelField.class, UserControlData.sex_));
		layout.addComponent(componentGroup.buildAndBind(I18N.USERMANAGE_POPUP_AGE.msg(), LabelField.class, UserControlData.age_));

		final BirthDateField birthDateField = new BirthDateField(true);
		componentGroup.bind(birthDateField, UserControlData.user_, User_.birthDate);

		layout.addComponent(birthDateField);
		layout.addComponent(componentGroup.buildAndBind(I18N.USERMANAGE_POPUP_EMAIL.msg(), UserControlData.user_, User_.email));
		layout.addComponent(componentGroup.buildAndBind(I18N.USERMANAGE_POPUP_PASSWORD.msg(), LabelField.class, UserControlData.user_, User_.password));
		layout.addComponent(componentGroup.buildAndBind(I18N.USERMANAGE_POPUP_PASSWORD.msg(), UserControlData.password_));
		layout.addComponent(componentGroup.buildAndBind(I18N.USERMANAGE_POPUP_EMAIL_CONFIRMED.msg(), UserControlData.user_, User_.emailConfirmed));

		int addressIdx = 0;
		for (Address address : userControlData.getUser().getAddresses())
		{
			final AddressPanel addressPanel = new AddressPanel(address, userControlListener, addressIdx);
			addressPanels.add(addressPanel);
			layout.addComponent(addressPanel);
			addressIdx++;
		}

		layout.addComponent(new BoldLabel(I18N.USERMANAGE_POPUP_INFORMATIONS.msg()));
		layout.addComponent(componentGroup.buildAndBind(I18N.USERMANAGE_POPUP_AWARETHROUGH.msg(), LabelField.class, UserControlData.awareThrough_));
		layout.addComponent(componentGroup.buildAndBind(I18N.USERMANAGE_POPUP_FREETEXT.msg(), LabelField.class, UserControlData.freeText_));

		saveHelper.addFields(componentGroup);
		addressPanels.forEach(addressPanel -> saveHelper.addFields(addressPanel.getFields()));

		layout.addComponent(saveHelper.getSaveButton());

		return layout;
	}

	private Component createDataInformationTab()
	{
		final VerticalLayout layout = new VerticalLayout();
		layout.setMargin(true);
		layout.setSpacing(true);
		layout.addComponent(new BoldLabel(I18N.USERMANAGEPOPUP_CAPTION_USERDATA.msg()));
		layout.addComponent(createExportButton(I18N.USERMANAGEPOPUP_ACTION_EXPORTUSERDATA.msg(), "export.txt", () -> userControlListener.exportUserData(userControlData.getUser())));
		layout.addComponent(new BoldLabel(I18N.USERMANAGEPOPUP_CAPTION_QUESTIONSANDANSWERS.msg()));
		layout.addComponent(createExportButton(I18N.USERMANAGEPOPUP_ACTION_EXPORTQUESTIONSANDANSWERS.msg(), "export.csv", () -> userControlListener.exportQuestionsAndAnswers(userControlData.getUser())));
		layout.addComponent(new BoldLabel(I18N.USERMANAGEPOPUP_CAPTION_IMAGESANDAVATARS.msg()));
		layout.addComponent(createExportButton(I18N.USERMANAGEPOPUP_ACTION_EXPORTIMAGESANDAVATARS.msg(), "export.zip", () -> userControlListener.exportImagesAndAvatars(userControlData.getUser())));
		return layout;
	}
	private Button createExportButton(String buttonCaption, String filename, StreamSource streamSource)
	{
		final Button exportButton = new Button(buttonCaption);
		final FileDownloader fileDownloader = new FileDownloader(new StreamResource(streamSource, filename));
		fileDownloader.extend(exportButton);
		return exportButton;
	}
	public void setSubscriptionTableHandler(LazyBeanFilteredItemsHandler<Subscription> subscriptionTableHandler)
	{
		subscriptionTable.setTableHandler(subscriptionTableHandler);
	}

	public void setInvoiceTableHandler(LazyBeanFilteredItemsHandler<Invoice> invoiceTableHandler)
	{
		invoiceTable.setTableHandler(invoiceTableHandler);
	}
	public void setActiveDeactiveTableHandler(LazyBeanFilteredItemsHandler<ActiveDeactiveSubscription> activeDeactiveHandler)
	{
		activateDeactivateTable.setTableHandler(activeDeactiveHandler);
	}

	public void setCleintInformationTableHandler(LazyBeanFilteredItemsHandler<ClientInformation> cleintInformationTableHandler)
	{
		clientInformationTable.setTableHandler(cleintInformationTableHandler);
	}

	private Component createUserProfile(RecommendationCategory category)
	{
		final UserProfileData userProfileData = userControlListener.getUserProfileData(userControlData.getUser(), category);
		return new UserProfile(userProfileData, userControlListener);
	}

	private Component createUserReportsTab()
	{
		final VerticalLayout layout = new VerticalLayout();
		layout.setSpacing(true);
		layout.setMargin(true);

		final Panel profileDownloadComponent = new FileViewerComponent(userControlData.getUserReportsSourceList());
		profileDownloadComponent.setSizeFull();
		layout.addComponent(profileDownloadComponent);

		return layout;
	}

	private Component createStatisticTab()
	{
		final AdminFormPanel layout = new AdminFormPanel();

		final Map<String, Map<RecommendationCategory, ?>> statisticMatrix = new HashMap<>();
		statisticMatrix.put(I18N.USERMANAGEPOPUP_CAPTION_STATISTICMATCHES.msg(), userControlData.getNoOfMatches());
		statisticMatrix.put(I18N.USERMANAGEPOPUP_CAPTION_STATISTICRECEIVEDMESSAGES.msg(), userControlData.getNoOfReceivedMessages());
		statisticMatrix.put(I18N.USERMANAGEPOPUP_CAPTION_STATISTICSENTMESSAGES.msg(), userControlData.getNoOfSendMessages());

		layout.addHeadline(I18N.USERMANAGEPOPUP_CAPTION_STATISTICGENERALTITLE.msg());
		layout.addMatrix(statisticMatrix);

		layout.addHeadline(I18N.USERMANAGEPOPUP_CAPTION_ADDITIONALSTATISTICS.msg());
		userControlData.getAdditionalStatistics().forEach((statisticData, s) -> layout.addLine(statisticData.toString(), s));

		return layout;
	}

	private Component createRegisterTab()
	{
		final VerticalLayout layout = new VerticalLayout();
		layout.setSpacing(true);
		layout.setMargin(true);

		final ComponentGroup<User> componentGroup = new ComponentGroup<>(User.class, userControlData.getUser());

		layout.addComponent(componentGroup.buildAndBind(I18N.USERMANAGEPOPUP_CAPTION_REGISTERID.msg(), LabelField.class, User_.id));
		layout.addComponent(componentGroup.buildAndBind(I18N.USERMANAGEPOPUP_CAPTION_REGISTEREMAIL.msg(), LabelField.class, User_.email));
		layout.addComponent(componentGroup.buildAndBind(I18N.USERMANAGEPOPUP_CAPTION_REGISTERDATE.msg(), LabelField.class, User_.createDate));

		final HorizontalLayout registerLayout = new HorizontalLayout();
		registerLayout.setSpacing(true);
		registerLayout.setCaption(I18N.USERMANAGEPOPUP_CAPTION_REGISTERIP.msg());
		layout.addComponent(registerLayout);

		registerLayout.addComponent(componentGroup.buildAndBind(LabelField.class, User_.registerIp));
		registerLayout.addComponent(new Label("-"));
		registerLayout.addComponent(new Label(StringUtils.timeToString(userControlData.getUser().getCreateDate())));

		final HorizontalLayout confirmationLayout = new HorizontalLayout();
		confirmationLayout.setSpacing(true);
		confirmationLayout.setCaption(I18N.USERMANAGEPOPUP_CAPTION_CONFIRMATIONIP.msg());
		layout.addComponent(confirmationLayout);

		confirmationLayout.addComponent(componentGroup.buildAndBind(LabelField.class, User_.confirmationIp));
		confirmationLayout.addComponent(new Label("-"));
		confirmationLayout.addComponent(new Label(StringUtils.timeToString(userControlData.getUser().getConfirmationDate())));

		return layout;
	}


	private  Component createMemoTextTab()
	{
		{

			final ComponentGroup<UserControlData> componentGroup = new ComponentGroup<>(UserControlData.class, userControlData);
			final SaveHelper saveHelper = new SaveHelper(() ->  {userControlListener.save(componentGroup.getItemDataSource().getBean()); });
			final TextArea memoTextArea = new TextArea();
			componentGroup.bind(memoTextArea, UserControlData.user_, User_.adminNotes);
			//nameTextField.setHeight("200px");
			//final TextArea memoTextArea = componentGroup.buildAndBind(I18N.USERMANAGE_POPUP_MEMO_TEXT.msg(), TextArea.class, User_.adminNotes);
			//final TextArea memoTextArea = new TextArea(); // TODO Change for memo text
			memoTextArea.setRequired(false);
			//memoTextArea.setIcon(new ThemeResource("img/thumbtack_pushpin_2.svg"));
			memoTextArea.setStyleName(CssStyle.MEMO_TEXTFIELD.getStyleName());
			memoTextArea.setWidth(100, Unit.PERCENTAGE);
			memoTextArea.setHeight("250px");
			final VerticalLayout layout = new VerticalLayout();
			layout.setSpacing(true);
			layout.setMargin(true);
			saveHelper.addFields(componentGroup);
			saveHelper.setShowUnsavedNotification(false);
			layout.addComponents(saveHelper.getValidationComponent(), memoTextArea, saveHelper.getSaveButton());
			return layout;
		}
	}

	public UserControlData getUserControlData()
	{
		return userControlData;
	}

	public void refreshRelationshipTable()
	{
		relationshipTable.refresh();
	}
}

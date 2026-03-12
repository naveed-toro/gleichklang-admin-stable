package de.binaerebauten.gleichklang.adminweb.presenter.handler;

import com.vaadin.server.Sizeable;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import de.binaerebauten.gleichklang.adminweb.presenter.I18N;
import de.binaerebauten.gleichklang.adminweb.presenter.UserManagePresenter;
import de.binaerebauten.gleichklang.adminweb.service.AdminUserLoginService;
import de.binaerebauten.gleichklang.adminweb.service.ExportService;
import de.binaerebauten.gleichklang.adminweb.service.matching.GenerateSuggestionService;
import de.binaerebauten.gleichklang.adminweb.view.component.AdminWorkItemTable;
import de.binaerebauten.gleichklang.adminweb.view.component.AdminWorkItemTable.Directory;
import de.binaerebauten.gleichklang.adminweb.view.component.UserQuickBar.UserQuickBarListener;
import de.binaerebauten.gleichklang.adminweb.view.component.UserResetBar;
import de.binaerebauten.gleichklang.adminweb.view.model.UserControlData;
import de.binaerebauten.gleichklang.adminweb.view.model.UserControlData.UserControlDataBuilder;
import de.binaerebauten.gleichklang.adminweb.view.popup.AdminReminderPopup;
import de.binaerebauten.gleichklang.adminweb.view.popup.PaybackPopup;
import de.binaerebauten.gleichklang.adminweb.view.popup.RelationshipPopup;
import de.binaerebauten.gleichklang.adminweb.view.popup.usermanage.GenerateLoginPopup;
import de.binaerebauten.gleichklang.adminweb.view.popup.usermanage.RecommendationBreakPopup;
import de.binaerebauten.gleichklang.adminweb.view.popup.usermanage.UserManagePopup;
import de.binaerebauten.gleichklang.adminweb.view.popup.usermanage.UserManagePopup.UserControlListener;
import de.binaerebauten.gleichklang.core.model.NaturalKeyEntity.NaturalKey;
import de.binaerebauten.gleichklang.core.model.locatable.Continent;
import de.binaerebauten.gleichklang.core.model.locatable.Country;
import de.binaerebauten.gleichklang.core.model.locatable.Region;
import de.binaerebauten.gleichklang.core.model.locatable.Zip;
import de.binaerebauten.gleichklang.core.model.mail.UserMailTemplate;
import de.binaerebauten.gleichklang.core.model.matching.Relationship;
import de.binaerebauten.gleichklang.core.model.media.MediaGallery;
import de.binaerebauten.gleichklang.core.model.message.AdminWorkItem;
import de.binaerebauten.gleichklang.core.model.message.Message;
import de.binaerebauten.gleichklang.core.model.payment.Invoice;
import de.binaerebauten.gleichklang.core.model.payment.Product;
import de.binaerebauten.gleichklang.core.model.payment.Subscription;
import de.binaerebauten.gleichklang.core.model.questionnaire.Answer;
import de.binaerebauten.gleichklang.core.model.questionnaire.Questionnaire;
import de.binaerebauten.gleichklang.core.model.questionnaire.QuestionnaireActivation;
import de.binaerebauten.gleichklang.core.model.user.*;
import de.binaerebauten.gleichklang.core.presenter.PopupOpener;
import de.binaerebauten.gleichklang.core.presenter.filter.DefaultFilterControlHandler;
import de.binaerebauten.gleichklang.core.presenter.media.MediaGalleryHandler;
import de.binaerebauten.gleichklang.core.presenter.question.QuestionnairePresenter;
import de.binaerebauten.gleichklang.core.presenter.question.QuestionnairePresenter.ActivatorLevel;
import de.binaerebauten.gleichklang.core.repository.ProductRepository;
import de.binaerebauten.gleichklang.core.repository.message.MessageRepository;
import de.binaerebauten.gleichklang.core.repository.user.CompleteUserRepository;
import de.binaerebauten.gleichklang.core.security.AuthenticationService;
import de.binaerebauten.gleichklang.core.service.*;
import de.binaerebauten.gleichklang.core.service.MessageService.MessageDirectory;
import de.binaerebauten.gleichklang.core.service.file.MediaService;
import de.binaerebauten.gleichklang.core.service.file.MediaUploadFile;
import de.binaerebauten.gleichklang.core.service.file.MessageUploadFile;
import de.binaerebauten.gleichklang.core.service.mail.MailSendService;
import de.binaerebauten.gleichklang.core.service.mail.UndeliverableMailService;
import de.binaerebauten.gleichklang.core.service.mail.UserMailTemplateService;
import de.binaerebauten.gleichklang.core.service.payment.InvoiceService;
import de.binaerebauten.gleichklang.core.service.payment.PaymentService;
import de.binaerebauten.gleichklang.core.service.report.UserProfileReportService;
import de.binaerebauten.gleichklang.core.service.validator.UniqueValidationException;
import de.binaerebauten.gleichklang.core.service.validator.ValidationException;
import de.binaerebauten.gleichklang.core.utils.AppUrlBuilder;
import de.binaerebauten.gleichklang.core.utils.UserProfileUtil.UserInfo;
import de.binaerebauten.gleichklang.core.utils.filter.FilterSpecificationBuilder;
import de.binaerebauten.gleichklang.core.view.QuestionnaireView;
import de.binaerebauten.gleichklang.core.view.QuestionnaireView.QuestionnaireActivationListener;
import de.binaerebauten.gleichklang.core.view.QuestionnaireViewImpl;
import de.binaerebauten.gleichklang.core.view.commit_strategy.IgnoreEmptyValuesValidationStrategy;
import de.binaerebauten.gleichklang.core.view.component.LazyBeanItemContainer.LazyBeanFilteredItemsHandler;
import de.binaerebauten.gleichklang.core.view.component.MessageBox;
import de.binaerebauten.gleichklang.core.view.component.MessageBox.DialogResult;
import de.binaerebauten.gleichklang.core.view.component.MessageBox.MessageBoxButtons;
import de.binaerebauten.gleichklang.core.view.component.OnDemandStreamSource;
import de.binaerebauten.gleichklang.core.view.component.Popup;
import de.binaerebauten.gleichklang.core.view.component.UserProfile;
import de.binaerebauten.gleichklang.core.view.component.UserProfile.UserProfileData;
import de.binaerebauten.gleichklang.core.view.component.message.MessageTable;
import de.binaerebauten.gleichklang.core.view.component.message.NewMessagePopup;
import de.binaerebauten.gleichklang.core.view.component.message.ShowMessagePopup;
import de.binaerebauten.gleichklang.core.view.component.question.QuestionGroupVerticalLayout;
import de.binaerebauten.gleichklang.core.view.popup.GenericPopup;
import de.binaerebauten.gleichklang.core.view.popup.InfoPopup;
import de.binaerebauten.gleichklang.core.view.popup.MediaGalleryPopup;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import javax.validation.constraints.NotNull;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static de.binaerebauten.gleichklang.core.model.filter.TemplateContext.SUGGESTION;
import static de.binaerebauten.gleichklang.core.model.filter.UserFilter.UserFilterType.*;
import static de.binaerebauten.gleichklang.core.view.component.FilterControlComponent.FilterControlFeature.PREVIEW;
import static de.binaerebauten.gleichklang.core.view.popup.I18N.MEDIAGALLERYPOPUP_CAPTION_GALLERY;

public class UserControlHandler implements UserControlListener, QuestionnaireActivationListener, UserQuickBarListener, UserResetBar.UserResetBarListener
		, UserResetBar.UserUnblockListner, UserResetBar.AbuserLoginListener, UserResetBar.ReminderLoginListener
{

	public interface ValueChangedListener
	{
		void valueChanged();
	}

	private final MessageRepository messageRepository;
	private final CompleteUserRepository completeUserRepository;

	private final UserService userService;
	private final AdminUserLoginService adminUserLoginService;
	private final RelationshipService relationshipService;
	private final AnswerService answerService;
	private final UserDataService userDataService;
	private final LocatableService locatableService;
	private final QuestionnaireService questionnaireService;
	private final MessageService messageService;
	private final UserProfileReportService userProfileReportService;
	private final SubscriptionService subscriptionService;
	private final InvoiceService invoiceService;
	private final ActivateDeactivateService activateDeactivateService;
	private final ClientInformationService clientInformationService;
	private final MatchingUtilService matchingUtilService;
	private final MailSendService mailSendService;
	private final UserMailTemplateService userMailTemplateService;
	private final GenerateSuggestionService generateSuggestionService;
	private final UndeliverableMailService undeliverableMailService;
	private final ExportService exportService;
	private final MediaService mediaService;
	private final MediaGalleryHandler mediaGalleryHandler;
	private final ReminderService reminderService;
    private final PaymentService paymentService;
    private final FilterSpecificationBuilder filterSpecificationBuilder;
	private final FilterControlService filterControlService;
	private final ProductRepository productRepository;

	private final ApplicationContext ctx;

	private final DefaultInvoiceHandler defaultInvoiceHandler;
	private final DefaultSubscriptionHandler defaultSubscriptionHandler;
	private final AdminMessageHandler adminMessageHandler;
	private final PopupOpener popupOpener;

	private final Admin currentAdmin;
	private final List<ValueChangedListener> valueChangedListeners = new ArrayList<>();
	private UserManagePopup userManagePopup = null;

	private final AppUrlBuilder appUrlBuilder;

	private UserManagePresenter userManagePresenter;
	private User currentUser;

	public UserControlHandler(ApplicationContext ctx, PopupOpener popupOpener)
	{
		this.popupOpener = Objects.requireNonNull(popupOpener);

		this.ctx = ctx;
		currentUser = ctx.getBean(CompleteUserRepository.class).findById(ctx.getBean(AuthenticationService.class).getAuthenticatedUserId());
		adminUserLoginService = ctx.getBean(AdminUserLoginService.class);
		relationshipService = ctx.getBean(RelationshipService.class);
		messageRepository = ctx.getBean(MessageRepository.class);
		messageService = ctx.getBean(MessageService.class);
		userService = ctx.getBean(UserService.class);
		answerService = ctx.getBean(AnswerService.class);
		userDataService = ctx.getBean(UserDataService.class);
		locatableService = ctx.getBean(LocatableService.class);
		questionnaireService = ctx.getBean(QuestionnaireService.class);
		completeUserRepository = ctx.getBean(CompleteUserRepository.class);
		mailSendService = ctx.getBean(MailSendService.class);
		userMailTemplateService = ctx.getBean(UserMailTemplateService.class);
		reminderService = ctx.getBean(ReminderService.class);
		paymentService = ctx.getBean(PaymentService.class);
        productRepository = ctx.getBean(ProductRepository.class);
		userProfileReportService = ctx.getBean(UserProfileReportService.class);

		subscriptionService = ctx.getBean(SubscriptionService.class);
		invoiceService = ctx.getBean(InvoiceService.class);
        activateDeactivateService = ctx.getBean(ActivateDeactivateService.class);
		clientInformationService = ctx.getBean(ClientInformationService.class);

		matchingUtilService = ctx.getBean(MatchingUtilService.class);
		generateSuggestionService = ctx.getBean(GenerateSuggestionService.class);

		undeliverableMailService = ctx.getBean(UndeliverableMailService.class);

		exportService = ctx.getBean(ExportService.class);

		filterSpecificationBuilder = ctx.getBean(FilterSpecificationBuilder.class);
		filterControlService = ctx.getBean(FilterControlService.class);

		mediaService = ctx.getBean(MediaService.class);
		mediaGalleryHandler = new MediaGalleryHandler(ctx, popupOpener);

		currentAdmin = ctx.getBean(AdminService.class).getCurrentUser();

		adminMessageHandler = new AdminMessageHandler(ctx, popupOpener);
		defaultInvoiceHandler = new DefaultInvoiceHandler(ctx, this::refreshInvoiceTable, popupOpener);
		defaultSubscriptionHandler = new DefaultSubscriptionHandler(ctx, this::refreshSubscriptionTable, popupOpener);
		appUrlBuilder = ctx.getBean(AppUrlBuilder.class);
	}

	public void addValueChangedListener(ValueChangedListener valueChangedListener)
	{
		if (valueChangedListener == null) return;
		this.valueChangedListeners.add(valueChangedListener);
	}

	public void openUser(long userId){

		final User user = completeUserRepository.findById(userId);
		final UserManagePopup userManagePopup = new UserManagePopup(createUserControlData(user), this, currentAdmin);
		if(user.getBlockedStatus().equals(BlockedStatus.BLOCKED)){
			userManagePopup.setCaption(user.getAlias() + ", " + user.getEmail() + " (BLOCKED)");
		}
		else if(user.getBlockedStatus().equals(BlockedStatus.ADMIN_BLOCKED)){
			userManagePopup.setCaption(user.getAlias() + ", " + user.getEmail() + " (BLOCKED BY ADMIN)");
		}
		else {
			userManagePopup.setCaption(user.getAlias() + ", " + user.getEmail());
		}
		if (popupOpener.tryOpenPopup(userManagePopup))
		{
			this.userManagePopup = userManagePopup;
		}
	}

	public void closeUserPopup(){

		userManagePopup.close();
	}

	private UserControlData createUserControlData(User user)
	{
		final DefaultFilterControlHandler relationshipFilterControlHandler;
		relationshipFilterControlHandler = new DefaultFilterControlHandler(filterControlService);
		relationshipFilterControlHandler.setTemplateContext(SUGGESTION);
		relationshipFilterControlHandler.setUserFilterTypes();
		relationshipFilterControlHandler.setPinnedUserFilterTypes(ALIAS_FILTER, MAIL_FILTER);
		relationshipFilterControlHandler.removeFilterControlFeatures(PREVIEW);

		return new UserControlDataBuilder()
				.setUser(user)
				.setSex(answerService.getAnswerValue(user, NaturalKey.SEX))
				.setAge(user.getAge())
				.setAwareThrough(answerService.getAnswerValue(user, NaturalKey.AWARE_THROUGH))
				.setFreeText(answerService.getAnswerValue(user, NaturalKey.FREE_TEXT_GK))
				.setCurrentSubscriptionState(subscriptionService.getCurrentSubscriptionWithState(user, LocalDateTime.now()).getState())
				.setNoOfMatches(UserDataService.createEnumMap(user, relationshipService::getRelationshipCountForUser))
				.setNoOfReceivedMessages(UserDataService.createEnumMap(user, messageRepository::countIncomingsByUser))
				.setNoOfSendMessages(UserDataService.createEnumMap(user, messageRepository::countOutgoingsByUser))
				.setMissingRequiredAnswersRatio(UserDataService.createEnumMap(user, matchingUtilService::getMissingAnswerRatio))
				.setMatchCount(UserDataService.createEnumMap(user, matchingUtilService::getMatchCount))
				.setAllocatableMatchCount(UserDataService.createEnumMap(user, matchingUtilService::getAllocatableMatchCount))
				.setSuggestionInitial(UserDataService.createEnumMap(user.getId(), generateSuggestionService::isInitial))
				.setRelationshipCountSinceCheckPeriod(UserDataService.createEnumMap(user.getId(), generateSuggestionService::getRelationshipsSinceCheckPeriod))
				.setMessageHandlerMap(createMessageHandler(user))
				.setAdminMessageHandlerMap(createAdminMessageHandler(user))
				.setReportsSourceList(createReportsSourceList(user))
				.setRelationshipHandler(relationshipService.createAdminRelationshipHandler(user))
				.setRelationshipFilterControlHandler(relationshipFilterControlHandler)
				.setFilterSpecificationBuilder(filterSpecificationBuilder)
				.setAdditionalStatistics(relationshipService.getStatistics(user,true))
				.setAvailableCategories(subscriptionService.getCurrentSubscriptionOfferCategories(user))
				.createUserControlData();
	}

	private SortedMap<AdminWorkItemTable.Directory, LazyBeanFilteredItemsHandler<AdminWorkItem>> createAdminMessageHandler(User user)
	{
		final SortedMap<AdminWorkItemTable.Directory, LazyBeanFilteredItemsHandler<AdminWorkItem>> messageHandler = new TreeMap<>();
		messageHandler.put(AdminWorkItemTable.Directory.INCOMING, messageService.createAdminWorkItemMessagesHandler(MessageDirectory.INCOMING, user));
		messageHandler.put(AdminWorkItemTable.Directory.OUTGOING, messageService.createAdminWorkItemMessagesHandler(MessageDirectory.OUTGOING, user));
		messageHandler.put(AdminWorkItemTable.Directory.DRAFT, messageService.createAdminWorkItemMessagesHandler(MessageDirectory.DRAFT, user));

		return messageHandler;
	}

	private SortedMap<MessageTable.Directory, LazyBeanFilteredItemsHandler<Message>> createMessageHandler(User user)
	{
		final SortedMap<MessageTable.Directory, LazyBeanFilteredItemsHandler<Message>> messageHandler = new TreeMap<>();
		messageHandler.put(MessageTable.Directory.INCOMING, messageService.createIncomingMessagesHandler(user));
		messageHandler.put(MessageTable.Directory.OUTGOING, messageService.createOutgoingMessagesHandler(user));
		messageHandler.put(MessageTable.Directory.DRAFT, messageService.createDraftMessagesHandler(user));
		messageHandler.put(MessageTable.Directory.DELETED, messageService.createHiddenMessagesHandler(user));

		return messageHandler;
	}

	private Map<String, StreamResource> createReportsSourceList(User user)
	{
		final Map<String, StreamResource> sourceList = new LinkedHashMap<>();

		sourceList.put(I18N.USERMANAGEPRESENTER_PERSONAL_PROFILE.msg(), new StreamResource(new OnDemandStreamSource()
		{
			@Override
			public String getFileName()
			{
				return I18N.USERMANAGEPRESENTER_PERSONAL_PROFILE_FILENAME.msg(".pdf");
			}

			@Override
			public String getMimeType()
			{
				return "application/pdf";
			}

			@Override
			public InputStream getStream()
			{
				return new ByteArrayInputStream(userProfileReportService.getPersoenlichkeitProfileForUser(user).getOutputStream().toByteArray());
			}
		}, I18N.USERMANAGEPRESENTER_PERSONAL_PROFILE_FILENAME.msg(".pdf")));
		sourceList.put(I18N.USERMANAGEPRESENTER_SOCIAL_PROFILE.msg(), new StreamResource(new OnDemandStreamSource()
		{
			@Override
			public String getFileName()
			{
				return I18N.USERMANAGEPRESENTER_SOCIAL_PROFILE_FILENAME.msg(".pdf");
			}

			@Override
			public String getMimeType()
			{
				return "application/pdf";
			}

			@Override
			public InputStream getStream()
			{
				return new ByteArrayInputStream(userProfileReportService.getGesellschaftProfileForUser(user).getOutputStream().toByteArray());
			}
		}, I18N.USERMANAGEPRESENTER_SOCIAL_PROFILE_FILENAME.msg(".pdf")));

		// get the current subscription offers
		Set<RecommendationCategory> rc = user.getOrderedCategories();
		rc.forEach(recommendationCategory -> {
			switch (recommendationCategory)
			{
				case PARTNERSHIP:
					sourceList.put(I18N.USERMANAGEPRESENTER_PARTNER_PROFILE.msg(), new StreamResource(new OnDemandStreamSource()
					{
						@Override
						public String getFileName()
						{
							return I18N.USERMANAGEPRESENTER_PARTNER_PROFILE_FILENAME.msg(".pdf");
						}

						@Override
						public String getMimeType()
						{
							return "application/pdf";
						}

						@Override
						public InputStream getStream()
						{
							return new ByteArrayInputStream(userProfileReportService.getPartnerschaftProfileForUser(user).getOutputStream().toByteArray());
						}
					}, I18N.USERMANAGEPRESENTER_PARTNER_PROFILE_FILENAME.msg(".pdf")));
					break;

				case FRIENDSHIP:
					sourceList.put(I18N.USERMANAGEPRESENTER_FRIENDSHIP_PROFILE.msg(), new StreamResource(new OnDemandStreamSource()
					{
						@Override
						public String getFileName()
						{
							return I18N.USERMANAGEPRESENTER_FRIENDSHIP_PROFILE_FILENAME.msg(".pdf");
						}

						@Override
						public String getMimeType()
						{
							return "application/pdf";
						}

						@Override
						public InputStream getStream()
						{
							return new ByteArrayInputStream(userProfileReportService.getFreundschaftProfileForUser(user).getOutputStream().toByteArray());
						}
					}, I18N.USERMANAGEPRESENTER_FRIENDSHIP_PROFILE_FILENAME.msg(".pdf")));
			}
		});

		return sourceList;
	}

	@Override
	public void openMessage(Message message, User user)
	{
		final ShowMessagePopup popup = new ShowMessagePopup(message, user, messageService.getMessageAttachments(message));
		popupOpener.tryOpenPopup(popup);
	}

	@Override
	public void openAdminWorkItem(AdminWorkItem item, Directory directory)
	{
		adminMessageHandler.openAdminWorkItem(item, directory, null);
	}

	@Override
	public List<QuestionnairePresenter> getQuestionnairePresenter(RecommendationCategory category, User user)
	{
		final List<QuestionnairePresenter> questionnairePresenters = new ArrayList<>();
		final List<Questionnaire> questionnaires = questionnaireService.getQuestionnaires(Collections.singleton(category), true);

		for (Questionnaire questionnaire : questionnaires)
		{
			final QuestionnaireView questionnaireView = new QuestionnaireViewImpl(questionnaire, new QuestionGroupVerticalLayout(new IgnoreEmptyValuesValidationStrategy(), true));

			final QuestionnaireActivation activation = questionnaireService.getActivation(user);
			final QuestionnairePresenter questionnairePresenter = new QuestionnairePresenter(ctx, this, activation, questionnaireView, true, ActivatorLevel.QUESTION_GROUP);

			questionnairePresenters.add(questionnairePresenter);
		}

		return questionnairePresenters;
	}

	@Override
	public void openInfo(UserInfo userInfo, List<Answer> answers)
	{
		final InfoPopup infoPopup = new InfoPopup(userInfo.toString(), answers);
		popupOpener.tryOpenPopup(infoPopup);
	}

	@Override
	public void showMediaGalleries(UserProfile sender, User user, RecommendationCategory category)
	{
		final Map<MediaGallery, MediaUploadFile> mediaGalleryWithPreview = mediaService.getMediaGalleryWithPreview(user, category);

		popupOpener.tryOpenPopup(new MediaGalleryPopup(
				mediaGalleryHandler,
				MEDIAGALLERYPOPUP_CAPTION_GALLERY.msg(user.getAlias()),
				mediaGalleryWithPreview));
	}

	@Override
	public String getEmailContent(UserMailTemplate template, User user, String... params)
	{
		if (Objects.nonNull(template))
		{
			return userMailTemplateService.createMailTemplateInstance(template, user, params).getContent();
		}

		return null;
	}

	@Override
	public void sendEmail(UserMailTemplate template, User user, String... params)
	{
		mailSendService.sendEmail(user, userMailTemplateService.createMailTemplateInstance(template, user, params));
	}

	@Override
	public void save(UserControlData userControlData) throws ValidationException
	{
		userService.save(userControlData.getUser(), userControlData.getPassword());
	}

	@Override
	public List<Continent> getContinents()
	{
		return locatableService.getContinents();
	}

	@Override
	public List<Country> getCountries(@NotNull Continent continent)
	{
		return locatableService.getCountries(continent);
	}

	@Override
	public List<Zip> getZips(@NotNull Country country)
	{
		return locatableService.getZips(country);
	}

	@Override
	public List<Region> getRegions(@NotNull Country country)
	{
		return locatableService.getRegions(country);
	}

	@Override
	public List<Continent> getContinentsWithZips()
	{
		return locatableService.getContinentsWithZips();
	}

	@Override
	public List<Country> getCountriesWithZips(@NotNull Continent continent)
	{
		return locatableService.getCountriesWithZips(continent);
	}

	@Override
	public List<Zip> getZips(@NotNull Region region)
	{
		return locatableService.getZips(region);
	}

	@Override
	public void activateQuestionnaire(Questionnaire questionnaire, boolean enabled)
	{
		//TODO: MW implement activator
	}

	@Override
	@Deprecated
	public void synchronizePendingExternalPayments()
	{
		defaultInvoiceHandler.synchronizePendingExternalPayments();
	}

	@Override
	public void editSubscription(Subscription subscription)
	{
		defaultSubscriptionHandler.editSubscription(subscription);
	}

	@Override
	public void edit(Invoice invoice)
	{
		defaultInvoiceHandler.edit(invoice);
	}

	@Override
	public void cancelSubscription(Subscription subscription)
	{
		defaultSubscriptionHandler.cancelSubscription(subscription);
	}

	@Override
	public void refreshSubscriptionTable()
	{
		if (userManagePopup != null)
			userManagePopup.setSubscriptionTableHandler(subscriptionService.createSubscriptionHandler(userManagePopup.getUserControlData().getUser()));
	}

	@Override
	public void refreshInvoiceTable()
	{
		if (userManagePopup != null)
			userManagePopup.setInvoiceTableHandler(invoiceService.createInvoiceHandler(userManagePopup.getUserControlData().getUser()));
	}

	@Override
	public void refreshActivateDeactivateTable() {

		if (userManagePopup != null)
			userManagePopup.setActiveDeactiveTableHandler(activateDeactivateService.activeDeactiveHandler(userManagePopup.getUserControlData().getUser(), getCurrentSubscription(userManagePopup.getUserControlData().getUser())));

	}

	@Override
	public void refreshClientInformationTable() {

		if (userManagePopup != null)
			userManagePopup.setCleintInformationTableHandler(clientInformationService.createSubscriptionHandler(userManagePopup.getUserControlData().getUser()));

	}

	@Override
	public void newSubscription(User user)
	{
		defaultSubscriptionHandler.newSubscription(user);
	}

	@Override
	public UserProfileData getUserProfileData(User user, RecommendationCategory category)
	{
		return userDataService.createUserProfileData(user, category);
	}

	@Override
	public void newRelationship(UserManagePopup sender, User user)
	{
		final DefaultFilterControlHandler createRelationshipFilterControlHandler = new DefaultFilterControlHandler(filterControlService);
		createRelationshipFilterControlHandler.removeFilterControlFeatures(PREVIEW);
		createRelationshipFilterControlHandler.setTemplateContext(SUGGESTION);
		createRelationshipFilterControlHandler.setUserFilterTypes();
		createRelationshipFilterControlHandler.setPinnedUserFilterTypes(ALIAS_FILTER, MAIL_FILTER, LAST_NAME_FILTER, FIRST_NAME_FILTER);

		final RelationshipPopup popup = new RelationshipPopup(createRelationshipFilterControlHandler, filterSpecificationBuilder, user, generateSuggestionService::createRelationship);
		popup.addCloseListener(event -> sender.refreshRelationshipTable());
		popupOpener.tryOpenPopup(popup);
	}

	@Override
	public void deleteRelationship(UserManagePopup sender, Relationship relationship)
	{
		relationshipService.deleteRelationship(relationship);
		sender.refreshRelationshipTable();
	}

	@Override
	public void undeleteRelationship(UserManagePopup sender, Relationship relationship)
	{
		relationshipService.restoreRelationship(relationship);
		sender.refreshRelationshipTable();
	}

	@Override
	public void openUser(User user) {
		if (Objects.isNull(user)) {
			MessageBox.show("This reminder is not for user");
			return;
		}

		openUser(user.getId());
	}

	@Override
	public  void closePopup(){
		closeUserPopup();
	}

	@Override
	public void adminBlocked(User user) {
		if (!user.getBlockedStatus().equals(BlockedStatus.ADMIN_BLOCKED)) {

			if (user.getBlockedStatus().equals(BlockedStatus.BLOCKED)) {
				MessageBox.show("Are you sure want to permanently block the  User " + (user.getEmail()), MessageBoxButtons.YES_NO, dialogResult ->
				{
					if (DialogResult.YES.equals(dialogResult)) {
						userService.adminBlocked(user);
					}

				});
			} else {
				MessageBox.show("Only Blocked members can be confirmed Block", MessageBox.MessageBoxButtons.OK, MessageBox.MessageBoxStyle.ATTENTION, null);
			}
		}
		else {
			MessageBox.show("User already blocked by Admin", MessageBox.MessageBoxButtons.OK, MessageBox.MessageBoxStyle.ATTENTION, null);

		}
	}

	@Override
	public void reminderLogin(User user) {

		AdminReminderPopup adminReminderPopup = new AdminReminderPopup(reminderService, AdminReminderPopup.Operation.ADD, null, currentAdmin,null);
		if (user != null) {
			adminReminderPopup.userNameOrAlias.setValue(user.getAlias());
		} else {
			adminReminderPopup.userNameOrAlias.setValue(currentUser.getAlias());
		}
		adminReminderPopup.show();
	}

	@Override
	    public void openRevocationPopup(User user) {
        Subscription subscription =  subscriptionService.findCurrentSubscription(user).orElse(null);
        Product product = productRepository.findProduct(user.getId());
        if(!paymentService.findCurrentPayment(user).isPresent()){
        	Notification.show("No Current Payment Exist for this user", Type.WARNING_MESSAGE);
        	return;
		}
		if((subscription!=null && subscription.isAutomaticRenewal())){
			Notification.show("Achtung: Product is prolongation", Notification.Type.WARNING_MESSAGE);
		}
		else if(subscription!=null && product!=null && product.getProductType().equals(Product.ProductType.INITIAL_SUBSCRIPTION_OFFER)){
			Notification.show("Achtung: Abonnement ist ein erstes Angebot!", Notification.Type.WARNING_MESSAGE);
		}
		else if((subscription!=null && subscription.isAutomaticRenewal()) && (product!=null && product.getProductType().equals(Product.ProductType.INITIAL_SUBSCRIPTION_OFFER))){
			Notification.show("Achtung: Abonnement ist eine Verlangerung Oder Erstangebot!", Notification.Type.WARNING_MESSAGE);
		}

		if(subscription!=null && !subscription.getBegin().plusDays(35).isAfter(LocalDateTime.now())){
            MessageBox.show("Subscription started more than 5 weeks ago",MessageBox.MessageBoxButtons.OK, MessageBox.MessageBoxStyle.ATTENTION, null);
		}
		final PaybackPopup popup = new PaybackPopup(paymentService,user,subscriptionService, this,relationshipService, defaultInvoiceHandler,productRepository);
		popup.setBoxSize(GenericPopup.BoxSize.MEDIUM);
		popup.setHeight(60, Sizeable.Unit.PERCENTAGE);
		popup.show();
	}

	@Override
	public void deactivateAutoRenewal(User user, Subscription currentSubscription)
	{
		try
		{
			defaultSubscriptionHandler.deactivateAutoRenewal(currentSubscription);
		}
		catch (ObjectOptimisticLockingFailureException ignored)
		{
			Notification.show(I18N.OPTIMISTIC_LOCKING_FAILURE.msg(), Type.ERROR_MESSAGE);
		}
		valueChangedListeners.forEach(ValueChangedListener::valueChanged);
	}

	@Override
	public void deleteUserData(User user)
	{
		MessageBox.show(I18N.USERMANAGEPRESENTER_MESSAGEBOX_TITLE.msg(), I18N.USERMANAGEPRESENTER_MESSAGEBOX_DESCRIPTION.msg(user.getAlias()), MessageBoxButtons.YES_NO, MessageBox.MessageBoxStyle.QUESTION, dialogResult ->
		{
			if (dialogResult == DialogResult.YES)
			{
				userDataService.deleteUserData(user);
				valueChangedListeners.forEach(ValueChangedListener::valueChanged);
			}
		});
	}

	@Override
	public void cancelUser(User user, Subscription currentSubscription)
	{
		defaultSubscriptionHandler.cancelSubscription(currentSubscription, () -> valueChangedListeners.forEach(ValueChangedListener::valueChanged));
	}


	@Override
	public void generateLogin(User user)
	{
		try
		{
			final String password = adminUserLoginService.createPassword(currentAdmin, user);
			final String url = appUrlBuilder.toAutoLoginReact(user.getEmail(), password).toString();
			final Popup loginPopup = new GenerateLoginPopup(url);
			popupOpener.tryOpenPopup(loginPopup);
		}
		catch (UniqueValidationException e)
		{
			Notification.show(e.getLocalizedMessage(), Type.WARNING_MESSAGE);
		}
	}

	@Override
	public String generatePassword()
	{
		return RandomStringUtils.randomAlphanumeric(10);
	}

	@Override
	public void savePassword(User user, String password) throws UniqueValidationException
	{
		userService.save(user, password);
	}

	@Override
	public Subscription getCurrentSubscription(User user)
	{
		this.currentUser = user;
		return subscriptionService.findCurrentSubscription(user).orElse(null);
	}

	@Override
	public boolean isMailBlacklisted(User user)
	{
		return undeliverableMailService.isBlocked(user.getEmail());
	}

	@Override
	public void removeFromBlacklist(User user)
	{
		if (Objects.isNull(user.getEmail()))
		{
			MessageBox.show(I18N.USERMANAGEPRESENTER_VALIDATION_NOTPOSSIBLE.msg());
			return;
		}

		undeliverableMailService.remove(user);
	}

	@Override
	public void addToBlacklist(User user)
	{
		if (Objects.isNull(user.getEmail()))
		{
			MessageBox.show(I18N.USERMANAGEPRESENTER_VALIDATION_NOTPOSSIBLE.msg());
			return;
		}

		try
		{
			undeliverableMailService.addUserToBlacklist(user);
		}
		catch (ValidationException e)
		{
			MessageBox.show(I18N.USERMANAGEPRESENTER_VALIDATION_ALREADYONBLACKLIST.msg());
		}
	}

	@Override
	public void activateCategory(User user, RecommendationCategory category)
	{
		if(!subscriptionService.getCurrentSubscriptionOfferCategories(user).contains(category))
		{
			MessageBox.show("Diese Kategorie ist nicht im aktuellen Abo des Nutzers enthalten");
			return;
		}

		user.getCategories().add(category);
		user.setCategories(new HashSet<>(user.getCategories()));
		saveUser(user);
	}

	@Override
	public void deactivateCategory(User user, RecommendationCategory category)
	{
//		if(user.getCategories().size() < 2)
//		{
//			MessageBox.show("Es muss mindestens eine Kategorie aktiviert bleiben");
//			return;
//		}

		user.getCategories().remove(category);
		user.setCategories(new HashSet<>(user.getCategories()));
		saveUser(user);
	}

	@Override
	public void activateRecommendationBreak(UserManagePopup sender, User user, RecommendationCategory category)
	{
		final RecommendationBreakPopup popup = new RecommendationBreakPopup(category, recommendationBreak ->
		{
			final Map<RecommendationCategory, LocalDate> recommendationBreaks = userService.getRecommendationBreaks(user);
			recommendationBreaks.putAll(recommendationBreak);
			userService.saveRecommendationBreaks(user, recommendationBreaks);
			sender.refreshView();
		});
		popupOpener.tryOpenPopup(popup);
	}

	@Override
	public void deactivateRecommendationBreak(UserManagePopup sender, User user, RecommendationCategory category)
	{
		final Map<RecommendationCategory, LocalDate> recommendationBreaks = userService.getRecommendationBreaks(user);
		recommendationBreaks.remove(category);
		userService.saveRecommendationBreaks(user, recommendationBreaks);
		sender.refreshView();
	}

	@Override
	public Map<RecommendationCategory, LocalDate> getRecommendationBreaks(User user)
	{
		return userService.getRecommendationBreaks(user);
	}

	@Override
	public void saveUser(User user)
	{
		try
		{
			userService.save(user);
		}
		catch (UniqueValidationException e)
		{
			MessageBox.show(e.getLocalizedMessage());
		}
	}

	@Override
	public void paymentReceived(Invoice invoice, boolean askForComment)
	{
		defaultInvoiceHandler.paymentReceived(invoice, askForComment);
	}

	@Override
	public InputStream exportUserData(User user)
	{
		return exportService.exportUserInformation(user);
	}

	@Override
	public InputStream exportQuestionsAndAnswers(User user)
	{
		return exportService.exportQuestionsAndAnswers(user);
	}

	@Override
	public InputStream exportImagesAndAvatars(User user)
	{
		return exportService.exportImagesAndAvatars(user);
	}

	@Override
	public void resetUserLogin() {
		adminUserLoginService.reset(currentAdmin);
	}

	@Override
	public void unblockUser() {

//		if(currentUser.isBlocked()){
//
//		}

//		try {
//			userService.addUserToUnblockList(currentUser);
//
//		} catch (ValidationException e) {
//			e.printStackTrace();
//		}

		if(!currentUser.isBlocked()){
			MessageBox.show("User is not in block list", MessageBox.MessageBoxButtons.OK, MessageBox.MessageBoxStyle.ATTENTION, null);
			return;
		}
		MessageBox.show("Are you sure want to unblock User "+(currentUser.getEmail()), MessageBoxButtons.YES_NO, dialogResult ->
		{
			if(DialogResult.YES.equals(dialogResult))
			{
				try
				{
					userService.addUserToUnblockList(currentUser);
				}
				catch (ValidationException e)
				{
					MessageBox.show(I18N.USERMANAGEPRESENTER_VALIDATION_ALREADYONBLACKLIST.msg());
				}
			}
		});

	}


	@Override
	public void abuserLogin(String messageBody) {
		User user = null;
		messageBody = StringEscapeUtils.unescapeHtml(messageBody);
		if(messageBody.contains("Abuse:") && messageBody.contains("(")) {
			user = userService.findByAlias(StringUtils.substringBetween(messageBody, "Abuse:", "(").trim());
		}
		else if(messageBody.contains("Missbrauch:") && messageBody.contains("(")){
            user = userService.findByAlias(StringUtils.substringBetween(messageBody, "Missbrauch:", "(").trim());
		}
		else if(messageBody.contains(":")){
			user = userService.findByAlias(messageBody.split(":")[1].trim());
		}
		if(user!=null) {
			generateLogin(user);
		}else{
			MessageBox.show("Unable to login");
		}

	}

	@Override
	public void writeMessage(User targetUser) {
		final NewMessagePopup popup = new NewMessagePopup(messageService.createNewMessageByAdmin(currentUser , targetUser, currentAdmin));
		popup.setNewUploadCallback(this::newUpload);
		popup.setSendCallback(this::sendMessage);
		popup.setSaveCallback(this::saveMessage);
		popup.setReceiver(targetUser);

		popup.setBoxSize(GenericPopup.BoxSize.WIDE);
		popup.setHeight(80, Sizeable.Unit.PERCENTAGE);
		popupOpener.tryOpenPopup(popup);
	}

	@Override
	public void openReminder(User user) {
		reminderLogin(user);
	}

	private void sendMessage(Message message, List<MessageUploadFile> messageUploadFiles) throws ValidationException
	{
		messageService.sendMessageFromAdmin(message, messageUploadFiles, currentAdmin);
	}

	private void saveMessage(Message message, List<MessageUploadFile> messageUploadFiles) throws ValidationException
	{
		messageService.saveAdminMessage(message, messageUploadFiles, currentAdmin);
	}

	private MessageUploadFile newUpload(Message message, Collection<MessageUploadFile> messageUploadFiles) throws ValidationException
	{
		return messageService.createMessageAttachment(message, messageUploadFiles.size());
	}
}
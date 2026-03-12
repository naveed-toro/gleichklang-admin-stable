package de.binaerebauten.gleichklang.memberweb.presenter;

import com.google.common.collect.Sets;
import com.vaadin.server.Sizeable;
import com.vaadin.server.StreamResource;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Component;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import de.binaerebauten.gleichklang.core.model.audio.UserAudio;
import de.binaerebauten.gleichklang.core.model.matching.Relationship;
import de.binaerebauten.gleichklang.core.model.media.Avatar;
import de.binaerebauten.gleichklang.core.model.media.FileEntity;
import de.binaerebauten.gleichklang.core.model.media.MediaGallery;
import de.binaerebauten.gleichklang.core.model.news.UserNews;
import de.binaerebauten.gleichklang.core.model.payment.*;
import de.binaerebauten.gleichklang.core.model.questionnaire.Answer;
import de.binaerebauten.gleichklang.core.model.questionnaire.Question.Requirement;
import de.binaerebauten.gleichklang.core.model.questionnaire.Questionnaire;
import de.binaerebauten.gleichklang.core.model.questionnaire.QuestionnaireActivation;
import de.binaerebauten.gleichklang.core.model.user.Address;
import de.binaerebauten.gleichklang.core.model.user.ClientInformation;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.navigation.DefaultNavigator;
import de.binaerebauten.gleichklang.core.presenter.NavigatePresenter;
import de.binaerebauten.gleichklang.core.repository.AvatarRepository;
import de.binaerebauten.gleichklang.core.repository.MediaGalleryRepository;
import de.binaerebauten.gleichklang.core.repository.RelationshipRepository;
import de.binaerebauten.gleichklang.core.repository.UserNewsRepository;
import de.binaerebauten.gleichklang.core.repository.message.MessageRepository;
import de.binaerebauten.gleichklang.core.repository.user.CompleteUserRepository;
import de.binaerebauten.gleichklang.core.security.AuthenticationService;
import de.binaerebauten.gleichklang.core.service.*;
import de.binaerebauten.gleichklang.core.service.content.AdvertisementContentTemplate;
import de.binaerebauten.gleichklang.core.service.content.StaticContentService;
import de.binaerebauten.gleichklang.core.service.file.*;
import de.binaerebauten.gleichklang.core.service.mail.UndeliverableMailService;
import de.binaerebauten.gleichklang.core.service.payment.PaymentService;
import de.binaerebauten.gleichklang.core.service.report.UserProfileReportService;
import de.binaerebauten.gleichklang.core.service.validator.UniqueValidationException;
import de.binaerebauten.gleichklang.core.view.commit_strategy.DefaultValidationStrategy;
import de.binaerebauten.gleichklang.core.view.commit_strategy.ValidationStrategy;
import de.binaerebauten.gleichklang.core.view.component.AddressPanel;
import de.binaerebauten.gleichklang.core.view.component.LazyBeanItemContainer;
import de.binaerebauten.gleichklang.core.view.component.MessageBox;
import de.binaerebauten.gleichklang.core.view.component.OnDemandStreamSource;
import de.binaerebauten.gleichklang.core.view.component.validator.ValidationResult;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;
import de.binaerebauten.gleichklang.core.view.popup.AnswerPopup;
import de.binaerebauten.gleichklang.core.view.popup.GenericPopup;
import de.binaerebauten.gleichklang.memberweb.navigation.DefaultNavigatorFactory.MemberMenuItem;
import de.binaerebauten.gleichklang.memberweb.presenter.handler.RelationshipHandler;
import de.binaerebauten.gleichklang.memberweb.service.NewsService;
import de.binaerebauten.gleichklang.memberweb.view.HomeView;
import de.binaerebauten.gleichklang.memberweb.view.HomeView.HomeViewListener;
import de.binaerebauten.gleichklang.memberweb.view.SubscriptionView.SubscriptionTab;
import de.binaerebauten.gleichklang.memberweb.view.popup.*;
import de.binaerebauten.gleichklang.memberweb.view.popup.AvatarPopup.AvatarCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.*;
import java.util.stream.Collectors;

public class HomePresenter extends NavigatePresenter implements HomeViewListener, AvatarCallback
{
	private static final Logger LOG = LoggerFactory.getLogger(HomePresenter.class);
	
	private final User currentUser;
	private final HomeView view;
	private final DefaultNavigator navigator;
	private final RelationshipHandler relationshipHandler;
	
	private final NewsService newsService;
	private final QuestionnaireService questionnaireService;
	private final LocatableService locatableService;
	private final UserService userService;
	private final UserProfileReportService userProfileReportService;
	private final AvatarService avatarService;
	private final AudioService audioService;
	private final AnswerService answerService;
	private final RelationshipRepository relationshipRepository;
	private final UserNewsRepository userNewsRepository;
	private final MessageRepository messageRepository;
	private final SubscriptionService subscriptionService;
	private final PaymentService paymentService;
	private final UndeliverableMailService invalidEmailService;
	private final StaticContentService staticContentService;
	private final MediaGalleryRepository mediaGalleryRepository;
	private final MediaService mediaService;
	private final FileService fileService;
	private final AvatarRepository avatarRepository;
	
	private final QuestionnaireActivation activation;
	
	private final ValidationStrategy commitStrategy = new DefaultValidationStrategy();
	
	public HomePresenter(ApplicationContext ctx, HomeView view, DefaultNavigator navigator, QuestionnaireActivation activation)
	{
		super(view);
		
		this.view = view;
		this.navigator = navigator;
		this.activation = activation;
		relationshipHandler = new RelationshipHandler(ctx, this);
		
		newsService = ctx.getBean(NewsService.class);
		questionnaireService = ctx.getBean(QuestionnaireService.class);
		locatableService = ctx.getBean(LocatableService.class);
		userService = ctx.getBean(UserService.class);
		userProfileReportService = ctx.getBean(UserProfileReportService.class);
		avatarService = ctx.getBean(AvatarService.class);
		audioService = ctx.getBean(AudioService.class);
		paymentService = ctx.getBean(PaymentService.class);
		subscriptionService = ctx.getBean(SubscriptionService.class);
		mediaGalleryRepository = ctx.getBean(MediaGalleryRepository.class);
		mediaService = ctx.getBean(MediaService.class);
		fileService = ctx.getBean(FileService.class);
		avatarRepository = ctx.getBean(AvatarRepository.class);

		relationshipRepository = ctx.getBean(RelationshipRepository.class);
		userNewsRepository = ctx.getBean(UserNewsRepository.class);
		messageRepository = ctx.getBean(MessageRepository.class);
		answerService = ctx.getBean(AnswerService.class);
		invalidEmailService = ctx.getBean(UndeliverableMailService.class);
		staticContentService = ctx.getBean(StaticContentService.class);
		
		currentUser = ctx.getBean(CompleteUserRepository.class).findById(ctx.getBean(AuthenticationService.class).getAuthenticatedUserId());
		view.setListener(this);
	}
	
	@Override
	public void enter(String parameters)
	{
		refreshView();
		LOG.debug("Layout resized");
	}
	
	private void refreshView()
	{
		final Subscription currentSubscription = subscriptionService.findCurrentSubscription(currentUser).orElse(null);
		
		view.setUserReccats(currentUser.getOrderedCategories());
		view.setNewsList(newsService.getNewsForUser(currentUser));
		view.setVisibleCategories(subscriptionService.getCurrentSubscriptionOfferCategories(currentUser));
		view.setNewSuggestionsUser(getNewSuggestions());
		view.setNewUserMessagesUser(getNewMessages());
		view.setNewFootprints(relationshipRepository.findAllRelationshipsWithNewFootprint(currentUser));
		view.setMissingAvatars(avatarService.getMissingAvatars(currentUser));
		view.setMissingAudio(audioService.getMissingAudio(currentUser));
		view.setIncompleteQuestionnaires(questionnaireService.getIncompleteQuestionnairesWithRequired(currentUser));
		view.setUserReportDownloader(getReportStreamResource());
		view.setNewUserAdminMessages(getNewAdminMessages());
		view.setInvalidEmail(invalidEmailService.isBlocked(currentUser.getEmail()), currentUser.getEmail(), navigator,userService,currentUser);
		view.setCurrentUser(currentUser);
		view.setDisabledCategories(getDeactivatedCategories(currentSubscription), getRecommendationBreaks());
		view.setSubscriptionInfo(getSubscriptionInfo(), getSubscriptionInfoButton(currentSubscription));
		view.checkPanelContent();
		view.setStaticContent(getStaticContent());
		view.initView(navigator.getDevice());
	}
	
	private Collection<RecommendationCategory> getRecommendationBreaks()
	{
		return Sets.intersection(userService.getRecommendationBreaks(currentUser).keySet(), currentUser.getCategories());
	}
	
	private Collection<RecommendationCategory> getDeactivatedCategories(Subscription subscription)
	{
		final EnumSet<RecommendationCategory> categories = EnumSet.noneOf(RecommendationCategory.class);
		
		if (subscription != null)
		{
			subscription.getOffer().getCategories().stream()
					.map(SubscriptionOfferCategory::getCategory)
					.filter(c -> !currentUser.getCategories().contains(c))
					.forEach(categories::add);
		}
		
		return categories;
	}

	@Override
	public void onGotoRecommendationBreakClicked()
	{
		navigateTo(MemberMenuItem.SUBSCRIPTION, SubscriptionTab.RECOMMENDATIONBREAK);
	}

	@Override
	public void onGotoActivateCategoryClicked()
	{
		navigateTo(MemberMenuItem.SUBSCRIPTION, SubscriptionTab.SUBSCRIPTION);
		navigateTo(MemberMenuItem.SUBSCRIPTION, SubscriptionTab.ACTIVECATEGORIES);
	}
	
	private String getSubscriptionInfo()
	{
		String infoText = null;
		final Optional<AbstractPayment> payment = paymentService.findCurrentPayment(currentUser);
		if (payment.isPresent())
		{
			final List<Product> products = payment.get().getInvoice().getItems().stream().map(InvoiceItem::getProduct).collect(Collectors.toList());
			
			if (PaymentState.PENDING.equals(payment.get().getState()))
			{
				if (products.stream().anyMatch(p -> p instanceof InitialSubscriptionOffer))
				{
					switch (payment.get().getMethod())
					{
						case PREPAYMENT:
							infoText = I18N.HOMEPRESENTER_CAPTION_PAYMENTREQUEST.msg();
							break;
						default:
							infoText = I18N.HOMEPRESENTER_CAPTION_PAYMENTPROCESSING.msg();
					}
				}
				else if (products.stream().anyMatch(p -> p instanceof UpgradeOffer || p instanceof ServiceOffer))
				{
					switch (payment.get().getMethod())
					{
						case PREPAYMENT:
							infoText = I18N.HOMEPRESENTER_CAPTION_PAYMENTAWAITING.msg();
							break;
						default:
							infoText = I18N.HOMEPRESENTER_CAPTION_PAYMENTPROCESSING.msg();
					}
				}
				else if (products.stream().anyMatch(p -> p instanceof RenewalOffer))
				{
					switch (payment.get().getMethod())
					{
						case PREPAYMENT:
							infoText = I18N.HOMEPRESENTER_CAPTION_RENEWALPAYMENTREQUEST.msg();
							break;
					}
				}
			}
		}
		return infoText;
	}
	
	private String getSubscriptionInfoButton(Subscription subscription)
	{
		final String buttonText;
		
		if (subscription != null)
		{
			final LocalDateTime now = LocalDateTime.now();
			final Duration duration = Duration.between(now, subscription.getEnd());
			final Period period = Period.between(now.toLocalDate(), subscription.getEnd().toLocalDate());
			
			if (duration.getSeconds() > 0 && duration.toDays() < 14)
			{
				buttonText = I18N.HOMEPRESENTER_CAPTION_SUBSCRIPTIONINFOEXPIRATION.msg(period.getDays() + 1);
			}
			else if (duration.getSeconds() <= 0)
			{
				buttonText = I18N.HOMEPRESENTER_CAPTION_SUBSCRIPTIONINFOEXPIRED.msg();
			}
			else if (period.toTotalMonths() > 0)
			{
				//buttonText = I18N.HOMEPRESENTER_CAPTION_SUBSCRIPTIONINFORUNNINGMONTHS.msg(period.toTotalMonths());
				buttonText = null;
			}
			else
			{
				buttonText = I18N.HOMEPRESENTER_CAPTION_SUBSCRIPTIONINFORUNNINGDAYS.msg(duration.toDays() + 1);
			}
		}
		else
		{
			final Optional<AbstractPayment> payment = paymentService.findCurrentPayment(currentUser);
			if (payment.isPresent() && payment.get() instanceof Prepayment && PaymentState.PENDING.equals(payment.get().getState()))
			{
				buttonText = I18N.HOMEPRESENTER_CAPTION_SUBSCRIPTIONINFOPENDING.msg();
			}
			else
			{
				buttonText = null;
			}
		}
		
		return buttonText;
	}
	
	private HashMap<String, Long> getNewSuggestions()
	{
		HashMap<String, Long> newSuggestions = new HashMap<>();
		Set<RecommendationCategory> categories = currentUser.getOrderedCategories();
		
		newSuggestions.put("countSuggestionsBySourceUser", relationshipRepository.countUnviewedBySourceUser(currentUser));

		return newSuggestions;
	}
	
	private HashMap<String, Long> getNewMessages()
	{
		HashMap<String, Long> newMessages = new HashMap<>();
		Set<RecommendationCategory> categories = currentUser.getOrderedCategories();
		
		newMessages.put("countNewIncomingMessagesByUser", messageRepository.countNewIncomingMessagesByUser(currentUser));

		return newMessages;
	}
	
	private Long getNewAdminMessages()
	{
		return messageRepository.countIncomingAdminMessagesByUser(currentUser);
	}
	
	/**
	 * Creates data source for the User Report Downloader based on the currnt
	 * subscription offer of the current user.
	 *
	 * @return data source for File Link downloader
	 */
	private Map<String, StreamResource> getReportStreamResource()
	{
		// LinkedHashMap is used to keep the order of insertion
		Map<String, StreamResource> sourceList = new LinkedHashMap<>();
		sourceList.put(I18N.HOMEPRESENTER_PERSONAL_PROFILE.msg(), new StreamResource(new OnDemandStreamSource()
		{
			@Override
			public String getFileName()
			{
				return I18N.HOMEPRESENTER_PERSONAL_PROFILE_FILENAME.msg(".pdf");
			}
			
			@Override
			public String getMimeType()
			{
				return "application/pdf";
			}
			
			@Override
			public InputStream getStream()
			{
				return new ByteArrayInputStream(userProfileReportService.getPersoenlichkeitProfileForUser(currentUser).getOutputStream().toByteArray());
			}
		}, I18N.HOMEPRESENTER_PERSONAL_PROFILE_FILENAME.msg(".pdf")));
		sourceList.put(I18N.HOMEPRESENTER_SOCIAL_PROFILE.msg(), new StreamResource(new OnDemandStreamSource()
		{
			@Override
			public String getFileName()
			{
				return I18N.HOMEPRESENTER_SOCIAL_PROFILE_FILENAME.msg(".pdf");
			}
			
			@Override
			public String getMimeType()
			{
				return "application/pdf";
			}
			
			@Override
			public InputStream getStream()
			{
				return new ByteArrayInputStream(userProfileReportService.getGesellschaftProfileForUser(currentUser).getOutputStream().toByteArray());
			}
		}, I18N.HOMEPRESENTER_SOCIAL_PROFILE_FILENAME.msg(".pdf")));
		
		// get the current subscription offers
		Set<RecommendationCategory> rc = currentUser.getOrderedCategories();
		rc.forEach(recommendationCategory ->
		{
			switch (recommendationCategory)
			{
				case PARTNERSHIP:
					sourceList.put(I18N.HOMEPRESENTER_PARTNER_PROFILE.msg(), new StreamResource(new OnDemandStreamSource()
					{
						@Override
						public String getFileName()
						{
							return I18N.HOMEPRESENTER_PARTNER_PROFILE_FILENAME.msg(".pdf");
						}
						
						@Override
						public String getMimeType()
						{
							return "application/pdf";
						}
						
						@Override
						public InputStream getStream()
						{
							return new ByteArrayInputStream(userProfileReportService.getPartnerschaftProfileForUser(currentUser).getOutputStream().toByteArray());
						}
					}, I18N.HOMEPRESENTER_PARTNER_PROFILE_FILENAME.msg(".pdf")));
					break;
				
				case FRIENDSHIP:
					sourceList.put(I18N.HOMEPRESENTER_FRIENDSHIP_PROFILE.msg(), new StreamResource(new OnDemandStreamSource()
					{
						@Override
						public String getFileName()
						{
							return I18N.HOMEPRESENTER_FRIENDSHIP_PROFILE_FILENAME.msg(".pdf");
						}
						
						@Override
						public String getMimeType()
						{
							return "application/pdf";
						}
						
						@Override
						public InputStream getStream()
						{
							return new ByteArrayInputStream(userProfileReportService.getFreundschaftProfileForUser(currentUser).getOutputStream().toByteArray());
						}
					}, I18N.HOMEPRESENTER_FRIENDSHIP_PROFILE_FILENAME.msg(".pdf")));
			}
		});
		
		return sourceList;
	}
	
	private Map<AdvertisementContentTemplate, Component> getStaticContent()
	{
		final Map<AdvertisementContentTemplate, Component> staticContent = new HashMap<>();
		
		staticContent.put(AdvertisementContentTemplate.AKTUELLES, staticContentService.getStaticComponent(AdvertisementContentTemplate.AKTUELLES));
		staticContent.put(AdvertisementContentTemplate.COMMUNITY, staticContentService.getStaticComponent(AdvertisementContentTemplate.COMMUNITY));
		staticContent.put(AdvertisementContentTemplate.MOEGLICHKEITEN, staticContentService.getStaticComponent(AdvertisementContentTemplate.MOEGLICHKEITEN));
		
		return staticContent;
	}
	
	@Override
	public void showAvatar(RecommendationCategory category)
	{
		final AvatarPopup avatarPopup = new AvatarPopup(avatarService.createAvatars(currentUser), this,
				this::saveAvatarFromMediaGallery, currentUser.getOrderedCategories(), this::saveAvatarsInAvatarGallery);
		
		avatarPopup.setDraggable(false);
		avatarPopup.selectCategory(category);
		avatarPopup.addStyleName(CssStyle.GENERIC_POPUP.getStyleName());
		
		tryOpenPopup(avatarPopup);
	}

	private void saveAvatarsInAvatarGallery(AvatarUploadFile avatar)
	{
		MediaGallery avatarGallery = mediaGalleryRepository.findAvatarGalleryByAuthor(userService.getCurrentUser());
		
		if (avatarGallery == null)
		{
			avatarGallery = new MediaGallery();
			avatarGallery.setAsAvatarGallery();
			avatarGallery.setAuthor(userService.getCurrentUser());
			avatarGallery.setName("Avatar Galerie");
			avatarGallery.setSecret(true);
			mediaGalleryRepository.save(avatarGallery);
		}
		
		try
		{
			BufferedImage inputImage = ImageIO.read(new File(avatar.getPath().toString()));
			
			File file = new File(avatar.getPath().toString());
			FileEntity persistedFile = fileService.saveAvatar(file, inputImage);
			
			MediaUploadFile media = new MediaUploadFile(avatarGallery, fileService, persistedFile);
			mediaService.saveWithoutValidation(media);
			
			avatar.getAvatar().setMediafile(persistedFile);
			
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private void saveAvatarFromMediaGallery(MediaUploadFile mediaUploadFile, RecommendationCategory recommendationCategory)
	{
		BufferedImage inputImage = null;
		try
		{
			inputImage = ImageIO.read(new File(mediaUploadFile.getPath().toString()));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		File file = new File(mediaUploadFile.getPath().toString());
		FileEntity persistedFile = fileService.saveAvatar(file, inputImage);
		FileEntity persistedThumbnail = fileService.saveAvatarThumbnail(file, inputImage);
		
		Avatar oldAvatar = avatarRepository.findByUserAndCategory(currentUser, recommendationCategory);
		if (oldAvatar != null)
		{
			oldAvatar.setFile(persistedFile);
			oldAvatar.setThumbnail(persistedThumbnail);
			oldAvatar.setMediafile(mediaUploadFile.getFileEntity());
			avatarRepository.save(oldAvatar);
		}
		else
		{
			Avatar avatar = new Avatar();
			avatar.setCategory(recommendationCategory);
			avatar.setUser(currentUser);
			
			avatar.setFile(persistedFile);
			avatar.setThumbnail(persistedThumbnail);
			avatar.setMediafile(mediaUploadFile.getFileEntity());
			avatarRepository.save(avatar);
		}
		
		refreshView();
	}
	
	@Override
	public void saveAvatars(Collection<AvatarUploadFile> avatarUploadFiles)
	{
		avatarUploadFiles.forEach(a ->
		{
			if (a.getPath() != null)
			{
				a.save();
			}
		});
		refreshView();
	}
	
	@Override
	public void showFullNews(UserNews userNews)
	{
		final NewsPopup popup = new NewsPopup(userNews);
		popup.setCaption(I18N.HOMEPRESENTER_NEWS_POPUP.msg());
		popup.addHideButton(event ->
		{
			userNews.setHide(true);
			saveNews(userNews);
			popup.close();
			view.setNewsList(newsService.getNewsForUser(currentUser));
		});
		
		popup.setBoxSize(GenericPopup.BoxSize.LARGE);
		
		if (tryOpenPopup(popup)) saveNews(userNews);
	}
	
	@Override
	public void saveNews(UserNews news)
	{
		userNewsRepository.save(news);
	}
	
	@Override
	public void showIncompleteQuestions(Questionnaire questionnaire, Collection<Requirement> requirements)
	{
		final List<Answer> incompleteAnswers = answerService.getIncompleteAndMissingAnswers(currentUser, questionnaire, activation, requirements);
		final AnswerPopup answerPopup = new AnswerPopup(incompleteAnswers, questionnaire.getName(), locatableService, activation, this::saveAnswers);
		
		answerPopup.addStyleName(CssStyle.POPUP_TYPE_GREEN.getStyleName());
		answerPopup.setIcon(new ThemeResource("img/question-icon.svg"));
		answerPopup.setBoxSize(GenericPopup.BoxSize.WIDE);
		answerPopup.setHeight(80, Sizeable.Unit.PERCENTAGE);
		
		tryOpenPopup(answerPopup);
	}
	
	@Override
	public List<MediaUploadFile> getMedias(MediaGallery mediaGallery)
	{
		return mediaService.getMedias(mediaGallery);
	}
	
	@Override
	public Map<MediaGallery, MediaUploadFile> getMediaGalleries()
	{
		return mediaService.getMediaGalleryWithPreview(currentUser);
	}
	
	private void saveAnswers(List<Answer> answers)
	{
		try
		{
			answerService.save(answers);
		}
		catch (UniqueValidationException e)
		{
			MessageBox.show(I18N.HOMEPRESENTER_SAVE_ERROR.msg(), MessageBox.MessageBoxButtons.OK, MessageBox.MessageBoxStyle.ATTENTION, null);
		}
		
		refreshView();
	}
	
	@Override
	public void showMessages(RecommendationCategory category)
	{
		navigator.navigateTo("MESSAGES");
	}
	
	@Override
	public void showMatches(RecommendationCategory category)
	{
		navigator.navigateTo("RELATIONSHIP");
	}
	
	@Override
	public void showRelationship(Relationship relationship, ClientInformation.Device device)
	{
		final Relationship originalRelationship = relationshipRepository.findRelationshipBySourceUserAndTargetUser(relationship.getTargetUser(), relationship.getSourceUser());
		if (originalRelationship == null)
		{
			LOG.error("No original relationship for {} - {}", relationship.getTargetUser(), relationship.getSourceUser());
			return;
		}
		
		final RelationshipPopup popup = relationshipHandler.createRelationshipPopup(originalRelationship, null, e -> refreshView(), this.navigator.getDevice());
		tryOpenPopup(popup);
	}
	
	@Override
	public void showIncompleteAddress(Address address)
	{
		final AddressPanel addressPanel = new AddressPanel(address, locatableService, 1);
		final IncompleteAddressPopup incompleteAddressPopup = new IncompleteAddressPopup(addressPanel);
		incompleteAddressPopup.setSaveListener(event ->
		{
			final ValidationResult validationResult = new ValidationResult(commitStrategy);
			incompleteAddressPopup.commit(validationResult);
			
			if (validationResult.isSuccess())
			{
				try
				{
					userService.save(currentUser);
					incompleteAddressPopup.close();
					refreshView();
				}
				catch (UniqueValidationException e)
				{
					LOG.error("Couldn't update Address, {} ", e.getMessage(), e);
					
					Notification.show(I18N.DATA_NOT_SAVED.msg(), Type.WARNING_MESSAGE);
				}
				
			}
			incompleteAddressPopup.getValidationComponent().showValidationResult(validationResult);
		});
		
		incompleteAddressPopup.setStyleName(CssStyle.GENERIC_POPUP.getStyleName());
		
		tryOpenPopup(incompleteAddressPopup);
	}
	
	@Override
	public void showAdminMessages()
	{
		navigator.navigateTo("MESSAGE_TO_GLEICHKLANG");
	}
	
	@Override
	public void showSubscription()
	{
		navigator.navigateTo("SUBSCRIPTION");
	}
}

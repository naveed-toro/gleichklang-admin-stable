package de.binaerebauten.gleichklang.core.service;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServletService;
import com.vaadin.server.WebBrowser;
import de.binaerebauten.gleichklang.core.model.locatable.Continent;
import de.binaerebauten.gleichklang.core.model.locatable.Country;
import de.binaerebauten.gleichklang.core.model.locatable.Zip;
import de.binaerebauten.gleichklang.core.model.mail.Newsletter;
import de.binaerebauten.gleichklang.core.model.mail.UserMailTemplate;
import de.binaerebauten.gleichklang.core.model.message.Message;
import de.binaerebauten.gleichklang.core.model.payment.InitialSubscriptionOffer;
import de.binaerebauten.gleichklang.core.model.payment.Subscription;
import de.binaerebauten.gleichklang.core.model.payment.Subscription.SubscriptionState;
import de.binaerebauten.gleichklang.core.model.payment.SubscriptionOffer;
import de.binaerebauten.gleichklang.core.model.payment.SubscriptionOfferCategory;
import de.binaerebauten.gleichklang.core.model.questionnaire.*;
import de.binaerebauten.gleichklang.core.model.systemconfig.EmailDomainMapping;
import de.binaerebauten.gleichklang.core.model.user.*;
import de.binaerebauten.gleichklang.core.model.user.UserActivityLog.UserActivity;
import de.binaerebauten.gleichklang.core.presenter.SubNavigatePresenter;
import de.binaerebauten.gleichklang.core.repository.*;
import de.binaerebauten.gleichklang.core.repository.message.MessageRepository;
import de.binaerebauten.gleichklang.core.repository.systemconfig.EmailDomainMappingRepository;
import de.binaerebauten.gleichklang.core.repository.user.UserRegistrationStateRepository;
import de.binaerebauten.gleichklang.core.repository.user.UserRepository;
import de.binaerebauten.gleichklang.core.security.AuthenticationService;
import de.binaerebauten.gleichklang.core.service.mail.MailQueueService;
import de.binaerebauten.gleichklang.core.service.mail.MailSendService;
import de.binaerebauten.gleichklang.core.service.mail.UndeliverableMailService;
import de.binaerebauten.gleichklang.core.service.mail.UserMailTemplateService;
import de.binaerebauten.gleichklang.core.service.payment.ExternalPaymentService;
import de.binaerebauten.gleichklang.core.service.payment.PaymentException;
import de.binaerebauten.gleichklang.core.service.validator.UniqueValidationException;
import de.binaerebauten.gleichklang.core.service.validator.ValidationException;
import de.binaerebauten.gleichklang.core.utils.CheckedTransactional;
import de.binaerebauten.gleichklang.core.view.SubNavigateView;
import de.binaerebauten.gleichklang.core.view.component.LazyBeanItemContainer;
import org.jfree.util.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import de.binaerebauten.gleichklang.core.view.component.LazyBeanItemContainer.LazyBeanFilteredItemsHandler;

/**
 * Provides common operations for {@link User} entities.
 */
@Service
public class UserService extends AbstractUserService<User>
{
	private static final Logger LOG = LoggerFactory.getLogger(UserService.class);
	
	private final AuthenticationService authenticationService;
	private final UserRepository userRepository;
	private final UserPaymentSettingsRepository userPaymentSettingsRepository;
	private final PaymentRepository paymentRepository;
	private final ExternalPaymentService externalPaymentService;
	private final QuestionnaireService questionnaireService;
	private final AnswerService answerService;
	private final UserRegistrationStateRepository userRegistrationStateRepository;
	private final ChatService chatService;
	private final RecommendationBreakRepository recommendationBreakRepository;
	private final UserActivityService userActivityService;
	private final LocatableRepository locatableRepository;
	private final EmailDomainMappingRepository emailDomainMappingRepository;
	private final MailQueueService mailQueueService;
	private final MessageRepository messageRepository;
	private final I18NRepository i18NRepository;
	private final UndeliverableMailService undeliverableMailService;
	
	private final UserMailTemplateService userMailTemplateService;
	// TODO : Move to property file and read at startup using @Value("${sibling_email_check_enabled}")
	private boolean siblingEmailCheckEnabled =true;
	@Autowired
	@Lazy
	private MailSendService mailSendService;
	
	@Autowired
	private SubscriptionService subscriptionService;
	
	@Value("${subscription_revocation_period_in_days}")
	private int subscriptionRevocationPeriodInDays;
	
	/**
	 * This class uses constructor dependency injection to ease testing.
	 *
	 * @param authenticationService
	 * @param mailQueueService
	 * @param userRepository
	 * @param userPaymentSettingsRepository
	 * @param answerService
	 * @param userRegistrationStateRepository
	 * @param chatService
	 * @param recommendationBreakRepository
	 * @param userActivityService
	 * @param locatableRepository
	 * @param passwordEncoder
	 */
	@Autowired
	public UserService(
			AuthenticationService authenticationService,
			MailQueueService mailQueueService,
			UserMailTemplateService userMailTemplateService,
			UserRepository userRepository,
			UserPaymentSettingsRepository userPaymentSettingsRepository,
			PaymentRepository paymentRepository, ExternalPaymentService externalPaymentService,
			AnswerService answerService,
			QuestionnaireService questionnaireService,
			UserRegistrationStateRepository userRegistrationStateRepository,
			ChatService chatService,
			RecommendationBreakRepository recommendationBreakRepository,
			UserActivityService userActivityService,
			LocatableRepository locatableRepository,
			PasswordEncoder passwordEncoder,
			@Value("${security.salt}") String salt,
			EmailDomainMappingRepository emailDomainMappingRepository,
			MessageRepository messageRepository,
			I18NRepository i18NRepository,UndeliverableMailService undeliverableMailService)
	{
		super(passwordEncoder, salt, userRepository);
		this.authenticationService = authenticationService;
		this.mailQueueService = mailQueueService;
		this.userMailTemplateService = userMailTemplateService;
		this.userRepository = userRepository;
		this.userPaymentSettingsRepository = userPaymentSettingsRepository;
		this.paymentRepository = paymentRepository;
		this.externalPaymentService = externalPaymentService;
		this.answerService = answerService;
		this.questionnaireService = questionnaireService;
		this.userRegistrationStateRepository = userRegistrationStateRepository;
		this.chatService = chatService;
		this.recommendationBreakRepository = recommendationBreakRepository;
		this.userActivityService = userActivityService;
		this.locatableRepository = locatableRepository;
		this.emailDomainMappingRepository = emailDomainMappingRepository;
		this.messageRepository = messageRepository;
		this.i18NRepository = i18NRepository;
		this.undeliverableMailService = undeliverableMailService;
	}
	
	/**
	 * Register and persist a new user with the given payment settings and
	 * answers.
	 *
	 * @param user                  the non-null user
	 * @param sendConfirmationEmail
	 * @param userPaymentSettings   the non-null user payment settings
	 * @param sexAnswer             the non-null answer   @return the persisted
	 *                              user
	 */
	@CheckedTransactional
	public User preregister(User user, boolean sendConfirmationEmail,
			UserPaymentSettings userPaymentSettings,
			Answer sexAnswer) throws UniqueValidationException
	{
		Objects.requireNonNull(user, "user == null");
		Objects.requireNonNull(userPaymentSettings, "userPaymentSettings == null");

		//TODO changes to allow for sibling mail ids - START
		if(siblingEmailCheckEnabled)
		{ 		if(findBySiblingEmail(user.getEmail(),false) != null) // check for active / inactive both mappings to prevent data inconsistency
			{		throw new UniqueValidationException("user with sibling email address found");		}		}
		//TODO changes to allow for sibling mail ids - END

		user.setMemberStatus(MemberStatus.REGISTRATION);
		user = preregister(user);
		
		final UserRegistrationState userRegistrationState = new UserRegistrationState();
		userRegistrationState.setRegistrationState(RegistrationState.INITIATED);
		userRegistrationState.setUser(user);
		userRegistrationStateRepository.save(userRegistrationState);
		
		userPaymentSettingsRepository.save(userPaymentSettings);
		if (sexAnswer != null) answerService.save(sexAnswer);
		
		if (sendConfirmationEmail)
		{
			mailQueueService.enqueue(user, UserMailTemplate.REGISTRATION_USER);
		}
		mailQueueService.enqueue(user, UserMailTemplate.MISSING_PAYMENT_REMINDER);
		
		return user;
	}
	
	@VisibleForTesting
	User preregister(User user) throws UniqueValidationException
	{
		if (user.getPassword() != null)
		{
			user.setPassword(encodePassword(user.getPassword()));
		}
		
		try
		{
			user.setRegisterIp(getIpAddress(null));
		}
		catch (NullPointerException catchForTest)
		{
			LOG.error("Could not get IP address {}", catchForTest.getMessage());
		}
		
		user = save(user);
		
		return user;
	}
	
	private String getIpAddress(HttpServletRequest request)
	{
		final VaadinRequest currentRequest = VaadinServletService.getCurrentRequest();
		final Page currentPage = Page.getCurrent();
		
		String ipAddress = null;
		
		if (request != null)
		{
			ipAddress = request.getHeader("X-Forwarded-For");
		}
		
		if (ipAddress == null && request != null)
		{
			ipAddress = request.getRemoteAddr();
		}
		
		if (ipAddress == null && currentRequest != null)
		{
			ipAddress = currentRequest.getHeader("X-Forwarded-For");
		}
		
		if (ipAddress == null && currentPage != null)
		{
			final WebBrowser webBrowser = currentPage.getWebBrowser();
			ipAddress = webBrowser.getAddress();
		}
		
		/* important for testing */
		if (ipAddress == null)
		{
			ipAddress = "could not extract from session";
		}
		
		return ipAddress;
	}
	
	/**
	 * This method register the given user and creates answers for all active
	 * questions.
	 * <p>
	 * It's only available for testing purposes and shouldn't be used
	 * otherwise.
	 *
	 * @param user the non-null user
	 * @return the registered user
	 * @throws UniqueValidationException
	 */
	@Transactional
	public User registerForTesting(User user) throws UniqueValidationException
	{
		Objects.requireNonNull(user, "user == null");
		
		user.setFirstName("Marvin");
		user.setLastName("Dummy");
		user.setMemberStatus(MemberStatus.REGISTRATION);
		
		if (user.getAddresses().isEmpty())
		{
			Address defaultAddress = createDefaultAddress(user);
			user.addAddress(defaultAddress);
		}
		
		user = save(user);
		final List<Questionnaire> questionnaires = questionnaireService.getQuestionnairesForUser(user);
		final QuestionnaireActivation activation = questionnaireService.getActivation(user);
		
		for (Questionnaire questionnaire : questionnaires)
		{
			final QuestionnaireAnswers questionnaireAnswers = answerService.getQuestionnaireWithAnswers(questionnaire, activation, false);
			final List<Answer> answers = questionnaireAnswers.getAnswers();
			final List<Answer> requiredAnswers = answers.stream()
					.filter(a -> a.getQuestion().isRequired())
					.collect(Collectors.toList());
			
			for (Answer answer : requiredAnswers)
			{
				if (answer instanceof TextAnswer)
				{
					final TextAnswer textAnswer = (TextAnswer) answer;
					textAnswer.setTextValue("DUMMY");
				}
				else if (answer instanceof NumberAnswer)
				{
					final NumberAnswer numberAnswer = (NumberAnswer) answer;
					numberAnswer.setNumberValue(42);
				}
				else if (answer instanceof ChoiceAnswer)
				{
					ChoiceQuestion choiceQuestion = (ChoiceQuestion) answer.getQuestion();
					
					Set<Choice> choices = new HashSet<>();
					switch (choiceQuestion.getSelectionType())
					{
						case SINGLE:
							Choice choice = choiceQuestion.getChoiceGroup().getChoices().get(0);
							choices.add(choice);
							break;
						case MULTIPLE:
							choices.addAll(choiceQuestion.getChoiceGroup().getChoices());
							break;
					}
					final ChoiceAnswer choiceAnswer = (ChoiceAnswer) answer;
					choiceAnswer.setChoices(choices);
				}
			}
			answerService.save(answers);
		}
		return user;
	}
	
	private Address createDefaultAddress(User user)
	{
		Address address = new Address();
		address.setPayment(true);
		address.setChecked(true);
		address.setUser(user);
		
		final Country country = locatableRepository.findByCountryCode("DE");
		if (country == null) return address;
		
		final Continent continent = country.getParent();
		
		address.setContinent(continent);
		address.setCountry(country);
		
		List<Zip> zips = locatableRepository.findOrderedZips(country);
		if (!zips.isEmpty())
		{
			final Zip zip = zips.get(0);
			address.setZip(zip);
			address.setRegion(zip.getRegion());
		}
		
		return address;
	}
	
	@CheckedTransactional
	public User save(User user, String newPassword) throws UniqueValidationException
	{
		if (!Strings.isNullOrEmpty(newPassword))
		{
			mailSendService.sendEmail(user, userMailTemplateService.createNewPassword(user, newPassword));
			user.setPassword(encodePassword(newPassword));
			user.setResetPassword(false);
		}
		
		return save(user);
	}

	@CheckedTransactional
	public User save(User user) throws UniqueValidationException
	{
		try
		{
			return userRepository.saveAndFlush(user);
		}
		catch (final DataIntegrityViolationException e)
		{
			throw convert(e, user);
		}
	}

	@Transactional
	public void updateUser(User user){
		userRepository.updateUser(MemberStatus.CANCELED, user);
	}
	
	@Override
	public User getCurrentUser()
	{
		final Long currentUserId = authenticationService.getAuthenticatedUserId();
		return currentUserId != null ? userRepository.findOne(currentUserId) : null;
	}
	
	@Transactional
	public void resetPassword(User user, String password)
	{
		user.setPassword(encodePassword(password));
		user.setResetPassword(true);
		userRepository.save(user);
	}
	
	@Override
	public void sendResetPasswordMail(SignableUser user)
	{
		if (user instanceof User)
		{
			mailQueueService.enqueue((User) user, UserMailTemplate.PASSWORD_RESET);
		}
	}
	
	@Override
	public User findByEmail(String email)
	{
		return userRepository.findByEmail(email);
	}

	public User findById(Long id){
		return userRepository.findById(id);
	}

	public User findByAlias(String alias)
	{
		return userRepository.findByAlias(alias);
	}
	
	public boolean isOnline(User user)
	{
		return chatService.isOnline(user);
	}
	
	public Map<RecommendationCategory, LocalDate> getRecommendationBreaks(User user)
	{
		final Map<RecommendationCategory, LocalDate> result = new HashMap<>();
		recommendationBreakRepository.findByUser(user).forEach(value -> result.put(value.getCategory(), value.getEndDate()));
		return result;
	}
	
	@Transactional
	public void saveRecommendationBreaks(User user, Map<RecommendationCategory, LocalDate> recommendationBreaks)
	{
		recommendationBreakRepository.deleteByUser(user);
		recommendationBreakRepository.flush();
		for (Map.Entry<RecommendationCategory, LocalDate> entry : recommendationBreaks.entrySet())
		{
			final RecommendationBreak recommendationBreak = new RecommendationBreak();
			recommendationBreak.setCategory(entry.getKey());
			recommendationBreak.setEndDate(entry.getValue());
			recommendationBreak.setUser(user);
			
			recommendationBreakRepository.save(recommendationBreak);
		}
	}
	
	/**
	 * Returns true iff. the given user can cancel his current subscription via
	 * the UI at the given date.
	 *
	 * @param userId the non-null user
	 * @param date   the non-null current date (in production code {@link
	 *               LocalDateTime#now()}, but exposed as parameter to ease
	 *               testing)
	 * @return true iff. the given user can cancel the subscription at the given
	 * date
	 */
	@Transactional
	public boolean canCancelSubscription(Long userId, LocalDateTime date)
	{
		Objects.requireNonNull(userId, "user == null");
		Objects.requireNonNull(date, "date == null");
		
		final Optional<Subscription> currentSubscription = subscriptionService.findCurrentSubscription(userId);
		
		final boolean subscriptionActive = currentSubscription.isPresent() &&
				SubscriptionState.ACTIVE == currentSubscription.get().getState();
		if (subscriptionActive)
		{
			final LocalDateTime endOfRevocationPeriod =
					currentSubscription.get().getBegin().plusDays(subscriptionRevocationPeriodInDays);
			return date.compareTo(endOfRevocationPeriod) >= 0 || !(currentSubscription.get().getOffer() instanceof InitialSubscriptionOffer);
		}
		
		return false; // this can happen when subscription state == PENDING
	}
	
	/**
	 * Cancels the current subscription of the given user and saves the user to
	 * persist the {@link User#cancelReasons}.
	 *
	 * @param user the non-null user
	 * @throws PaymentException
	 * @throws UniqueValidationException
	 */
	@CheckedTransactional
	public void cancelSubscription(User user)
	{
		Objects.requireNonNull(user, "user == null");
		
		final Optional<Subscription> currentSubscription = subscriptionService.findCurrentSubscription(user);
		if (currentSubscription.isPresent())
		{
			// must be send before cancelling, because cancelled user doesn't receive any mails


			String successOfMediation = i18NRepository.successOfMediation(user.getId());
			List<Object> satisfactionWithHarmony = i18NRepository.satisfactionWithHarmony(user.getId());
			Set<CancelReason> cancelReason = user.getCancelReasons();
			if((satisfactionWithHarmony.size()>0?(satisfactionWithHarmony.contains("Zufrieden")|| satisfactionWithHarmony.contains("Satisfied")):false ||
					cancelReason.contains(CancelReason.SUCCESS_THROW_GK) ||
					successOfMediation!=null ?successOfMediation.equalsIgnoreCase("Ich habe partnerschaft gefunden.")||
					successOfMediation.equalsIgnoreCase("ich habe freundschaft gefunden.")||
					successOfMediation.equalsIgnoreCase("ich habe partnerschaft und freundschaft gefunden."):false)  &&  !cancelReason.contains(CancelReason.UNHAPPY_WITH_SERVICE)){
				mailSendService.sendEmail(user, userMailTemplateService.createMailTemplateInstance(UserMailTemplate.SUBSCRIPTION_CANCELLED_WITH_SATISFACTION, user));
			}
            else {
				mailSendService.sendEmail(user, userMailTemplateService.createMailTemplateInstance(UserMailTemplate.SUBSCRIPTION_CANCELLED, user));
			}
			subscriptionService.cancelSubscription(currentSubscription.get());
		}
		else
		{
			throw new NullPointerException("currentSubscription == null");
		}
	}
	
	public boolean needsEmailConfirmation(User user)
	{
		return !user.isEmailConfirmed() && paymentRepository.existsByUser(user);
	}
	
	@Transactional
	public void sendChangeMail(User user, String newMail) throws UniqueValidationException
	{

		//	System.out.println("in send change mail" );
		if(siblingEmailCheckEnabled)
		{

			final boolean emailChanged = !Objects.equals(user.getEmail(), newMail);
			final boolean otherExistingMail = emailChanged && findByEmail(newMail) != null;

			if (!emailChanged && user.isEmailConfirmed() || otherExistingMail) {
				//return;
				throw  new UniqueValidationException(I18N.EMAIL_CAN_NOT_BE_USED.msg());
			}

			if (findByEmail(newMail) != null  && findByEmail(newMail).isEmailConfirmed())
			{ throw  new UniqueValidationException("This email id already exists");
			}
			else{ // also check for sibling email id
				User foundUser = findBySiblingEmail(newMail,true);

				if (foundUser != null &&  foundUser.getId() != null && user.getId() != null
				 && foundUser.getId().longValue() != user.getId().longValue())
				// anothe user is trying to update his id to some other users sibling ID, so dont allow him
				{
					//	System.out.println("Another user trying to update" + foundUser.getEmail());

					throw  new UniqueValidationException("another user with same email id exists" );
				}
				else
				{
					user.setEmail(newMail);
					userRepository.save(user);
					mailQueueService.enqueue(user, UserMailTemplate.MAIL_CHANGE_VERIFICATION);

				}
			}

		}
		else {
			final boolean emailChanged = !Objects.equals(user.getEmail(), newMail);
			final boolean otherExistingMail = emailChanged && findByEmail(newMail) != null;

			if (!emailChanged && user.isEmailConfirmed() || otherExistingMail)
				return;

			user.setEmail(newMail);
			userRepository.save(user);
			mailQueueService.enqueue(user, UserMailTemplate.MAIL_CHANGE_VERIFICATION);
		}
	}

	@Transactional
	public void sendChangeMailNew(User user) throws UniqueValidationException
	{

		    undeliverableMailService.remove(user);
		    if(!user.isEmailConfirmed()) {
				mailQueueService.enqueue(user, UserMailTemplate.MAIL_CHANGE_VERIFICATION);
			}
	}
	
	@CheckedTransactional
	public boolean changeMail(HttpServletRequest request, User user, String newMail)
	{

		user.setEmail(newMail);
		user.setEmailConfirmed(true);
		user.setConfirmationIp(getIpAddress(request));
		user.setConfirmationDate(LocalDateTime.now());
		
		try
		{
			save(user);
			return true;
		}
		catch (UniqueValidationException e)
		{
			LOG.error("UniqueValidation Exception by changing email for user {} to {} ", user.getAlias(), newMail, e);
			return false;
		}
	}
	
	@CheckedTransactional
	public void unsubscribe(User user, UserMailTemplate mailTemplate)
	{
		final UserSettings userSettings = user.getUserSettings();
		
		switch (mailTemplate)
		{
			case NEWS_FROM_GLEICHKLANG:
				userSettings.setDisableNewsNotifications(true);
				break;
			case NEW_BOXNUMBER_CONTACT:
				userSettings.setDisableCipherMessageNotifications(true);
				break;
			case NEW_MATCH:
				userSettings.setDisableRecommendationNotifications(true);
				break;
			case NEW_MATCH_POSITIVE:
				userSettings.setDisablePositiveRankingNotifications(true);
				break;
			case NEW_FOOTPRINT:
				userSettings.setDisableFootprintNotifications(true);
				break;
			case MISSING_PAYMENT_REMINDER:
			case MISSING_QUESTIONAIRE_REMINDER:
				userSettings.setEnableMarketingNotifications(false);
				break;
			default:
				LOG.error("{} is not unsubscribable", mailTemplate);
				return;
		}
		
		userRepository.save(user);
	}
	
	@Transactional
	public void reuseUser(User user)
	{
		if (user.getMemberStatus().equals(MemberStatus.CANCELED)|| user.getMemberStatus().equals(MemberStatus.ADMIN_CANCELED))
		{
			try
			{
				if (externalPaymentService.usesExternalPayment(user))
				{
					externalPaymentService.deregister(user);
				}
			}
			catch (PaymentException e)
			{
				LOG.error("Deregister failed for user {}", user.getAlias(), e);
			}
			
			user.setMemberStatus(MemberStatus.REGISTRATION);
			userRepository.save(user);
			
			UserRegistrationState userRegistrationState = userRegistrationStateRepository.findByUser(user);
			if (Objects.isNull(userRegistrationState))
			{
				userRegistrationState = new UserRegistrationState();
				userRegistrationState.setUser(user);
			}
			userRegistrationState.setRegistrationState(RegistrationState.PAYMENT);
			userRegistrationState.setUser(user);
			userRegistrationStateRepository.save(userRegistrationState);
		}
	}
	
	@Transactional
	public User activateUser(User user)
	{
		LOG.info("Inside activat user=====================================");
		Objects.requireNonNull(user);
		
		userRegistrationStateRepository.deleteByUser(user);
		
		if (MemberStatus.REGISTERED.equals(user.getMemberStatus())) return user;
		
		user.setMemberStatus(MemberStatus.REGISTERED);
		
		mailQueueService.dequeue(user, UserMailTemplate.MISSING_QUESTIONAIRE_REMINDER);
		mailQueueService.enqueue(user, UserMailTemplate.MISSING_QUESTIONAIRE_REMINDER);
		
		userActivityService.createActivity(user, UserActivity.REGISTERED);

		userRepository.updateUser(MemberStatus.REGISTERED,user);
		return user;


	}

	
	public void updateCategories(User user, SubscriptionOffer offer, boolean activateAll)
	{
		final Set<RecommendationCategory> recommendationCategories = offer.getCategories().stream()
				.map(SubscriptionOfferCategory::getCategory)
				.collect(Collectors.toSet());
		
		updateCategories(user, recommendationCategories, activateAll);
	}
	
	public void updateCategories(User user, Collection<RecommendationCategory> categories, boolean activateAll)
	{
		user.getCategories().removeIf(c -> !categories.contains(c));
		if (activateAll)
		{
			user.setCategories(new HashSet<>(categories));
		}
		userRepository.save(user);
	}
	
	@Transactional
	public void setPreConfigNotifications(User user, boolean systemNotificationsEnabled, boolean marketingNotificationsEnabled) throws ValidationException
	{
		Objects.requireNonNull(user);
		Objects.requireNonNull(user.getUserSettings());
		
		final UserSettings userSettings = user.getUserSettings();
		userSettings.setDisableFootprintNotifications(!systemNotificationsEnabled);
		userSettings.setDisableCipherMessageNotifications(!systemNotificationsEnabled);
		userSettings.setDisablePositiveRankingNotifications(!systemNotificationsEnabled);
		userSettings.setDisableRecommendationNotifications(!systemNotificationsEnabled);
		userSettings.setDisableNewsNotifications(!marketingNotificationsEnabled);
		userSettings.setEnableMarketingNotifications(marketingNotificationsEnabled);
		
		save(user);
	}
	
	@Transactional
	public long disableUserNewsNotifications(Collection<String> mails)
	{
		if (mails.isEmpty()) return 0;
		
		final AtomicInteger disabledNews = new AtomicInteger(0);
		
		Lists.partition(new ArrayList<>(mails), 1000).forEach(mailsPartition ->
		{
			final List<User> users = userRepository.findUsersWithUserNewsActivated(mailsPartition, MemberStatus.CANCELED);
			users.forEach(u -> u.getUserSettings().setDisableNewsNotifications(true));
			disabledNews.addAndGet(users.size());
		});
		
		return disabledNews.get();
	}

	//method to check if a sibling email address exists for userentered email address
	// as per requirement; user should be able to log in with parallel email addresses, eg axz@google.com == xyz@gmail.com
	//TODO optimise code to remove hard coding
	public User findBySiblingEmail(String emailAddress, boolean checkForOnlyActiveMappings )
	{
		String domain, id, idToCheck;
		String siblingDomains[];
		if(emailAddress != null && !emailAddress.isEmpty())
		{
			try
			{
				domain = emailAddress.substring(emailAddress.indexOf("@")+1);
				id = emailAddress.substring(0, emailAddress.indexOf("@"));
				List<EmailDomainMapping> siblingMappings;
				if(checkForOnlyActiveMappings)
					siblingMappings =  emailDomainMappingRepository.findByActiveTrueAndMappingValueContaining(domain);
				else
					siblingMappings =  emailDomainMappingRepository.findByMappingValueContaining(domain);
				if(siblingMappings != null && siblingMappings.size() >0)
				{
					for(EmailDomainMapping mapping: siblingMappings)
					{

						siblingDomains = mapping.getMappingValue().split(",");
						for(String s: siblingDomains)
						{
							idToCheck = id.trim()+"@"+s.trim();
							User u = userRepository.findByEmail(idToCheck);

							if(u != null)
							{
								return u;

							}

						}
					}

				}
			}
			catch (Exception e)
			{
				LOG.error("unable to provide sibling mapping, moving forward as is");
			}
		}
		return  null;
	}

	// Changes for GR3-56, allow sibling emails to be searched when filtering
	public List<String> findSiblingIds(String emailAddress)
	{
		String id, domain;
		String siblingDomains[];
		List<String> ids = new ArrayList<>();
		domain = emailAddress.substring(emailAddress.indexOf("@")+1);
		id = emailAddress.substring(0, emailAddress.indexOf("@"));
		List<EmailDomainMapping> siblingMappings;
		siblingMappings =  emailDomainMappingRepository.findByMappingValueContaining(domain.trim());
		if(siblingMappings != null && siblingMappings.size() >0) {
			for (EmailDomainMapping mapping : siblingMappings) {
				siblingDomains = mapping.getMappingValue().split(",");
				for (String s : siblingDomains) {
					//   System.out.println("sibling found"+s);
					ids.add(id.trim() + "@" + s.trim());
				}
			}
		}

		return ids;

	}


	@CheckedTransactional
	public void addUserToUnblockList(User user) throws ValidationException
	{

		if(user.isBlocked()){
			//
			    if(user.getMemberStatus()==MemberStatus.CANCELED){
					userRepository.unblockUserAdmin(MemberStatus.CANCELED, user, BlockedStatus.NOT_BLOCKED, null);
				}
				else if(user.getMemberStatus()==MemberStatus.ADMIN_CANCELED){
					userRepository.unblockUserAdmin(MemberStatus.ADMIN_CANCELED, user, BlockedStatus.NOT_BLOCKED, null);
				}
				else {
					userRepository.unblockUserAdmin(MemberStatus.REGISTERED, user, BlockedStatus.NOT_BLOCKED, null);
				}
			//}
			//else{
				//userRepository.unblockUserAdmin(MemberStatus.REGISTERED, user);
			//}
			//messageRepository.deleteScamAdminMessageOnUnblock(user);

			List<Message> messageList = messageRepository.findByUserScam(user);

			for(Message m : messageList)
			{

				if(m.getSenderEnvelope() != null && m.getSenderEnvelope().getUser() != null) {
					m.getSenderEnvelope().getUser().setBlocked(false);
					m.getSenderEnvelope().getUser().setBlockedDate(null);
					m.getSenderEnvelope().getUser().setAdminBlockedDate(null);
					messageRepository.save(m);
				}

				messageRepository.deleteScamAdminMessageOnUnblock(m.getId());
			}
		}
	}

	public void addUserToBlockList(User user) throws ValidationException
	{
		if(!user.isBlocked()){
			userRepository.updateUserBlocked(user.getId(), BlockedStatus.BLOCKED, LocalDateTime.now());

			List<Message> messageList = messageRepository.findByUserScam(user);
			for(Message m : messageList)
			{
				if(m.getSenderEnvelope() != null && m.getSenderEnvelope().getUser() != null) {
					m.getSenderEnvelope().getUser().setBlocked(true);
					m.getSenderEnvelope().getUser().setBlockedDate(LocalDateTime.now());
					messageRepository.save(m);
				}
				messageRepository.deleteScamAdminMessageOnUnblock(m.getId());
			}
		}
	}
	// create the method for deleting the suspended recommendation category from reuse purpose of user, So that user can see the recommendation suggestion
	@Transactional
	public void DeleteRecommendationBreak(User user){
		recommendationBreakRepository.deleteByUser(user);
	}

	@Transactional
	public void adminBlocked(User user) {
		userRepository.blockedByAdmin(BlockedStatus.ADMIN_BLOCKED, user);
	}

	public LazyBeanFilteredItemsHandler<User> createBlockedUserHandler(User user)
	{
		return createBlockedUsersHandler(user);
	}

	public LazyBeanFilteredItemsHandler<User> createBlockedUsersHandler(User user)
	{
		final Specification<User> directorySpec=new BlockedUserSpecification(user);

		final Specifications<User> specs = Specifications.where(directorySpec);
		return (specification, pageable) -> userRepository.findAll(specs.and(specification), pageable);
	}

	public static class BlockedUserSpecification implements Specification<User>
	{
		private final User user;

		public BlockedUserSpecification()
		{
			this(null);
		}

		public BlockedUserSpecification(User user)
		{
			this.user = user;
		}

		@Override
		public Predicate toPredicate(Root<User> root, CriteriaQuery<?> query, CriteriaBuilder cb)
		{

			final Predicate blokedUserPredicate = cb.equal(root.get(User_.isBlocked), true);

			return blokedUserPredicate;
		}
	}

	public LazyBeanFilteredItemsHandler<User> createUsersDataExportHandler()
	{
		return (specification, pageable) -> userRepository.findAll(specification, pageable);
	}
}



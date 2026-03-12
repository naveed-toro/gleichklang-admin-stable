package de.binaerebauten.gleichklang.memberweb.presenter;

import com.vaadin.ui.*;
import com.vaadin.ui.Notification.Type;
import de.binaerebauten.gleichklang.core.model.NaturalKeyEntity.NaturalKey;
import de.binaerebauten.gleichklang.core.model.locatable.Continent;
import de.binaerebauten.gleichklang.core.model.payment.*;
import de.binaerebauten.gleichklang.core.model.questionnaire.Questionnaire;
import de.binaerebauten.gleichklang.core.model.questionnaire.QuestionnaireActivation;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.model.user.RegistrationState;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.model.user.UserPaymentSettings;
import de.binaerebauten.gleichklang.core.presenter.NavigatePresenter;
import de.binaerebauten.gleichklang.core.presenter.question.QuestionnairePresenter;
import de.binaerebauten.gleichklang.core.presenter.question.QuestionnairePresenter.ActivatorLevel;
import de.binaerebauten.gleichklang.core.repository.*;
import de.binaerebauten.gleichklang.core.repository.user.CompleteUserRepository;
import de.binaerebauten.gleichklang.core.security.AuthenticationService;
import de.binaerebauten.gleichklang.core.security.AuthenticationService.RedirectGoal;
import de.binaerebauten.gleichklang.core.service.QuestionnaireService;
import de.binaerebauten.gleichklang.core.service.UserService;
import de.binaerebauten.gleichklang.core.service.payment.InvoiceService;
import de.binaerebauten.gleichklang.core.service.payment.PaymentException;
import de.binaerebauten.gleichklang.core.service.payment.PaymentService;
import de.binaerebauten.gleichklang.core.service.validator.ValidationException;
import de.binaerebauten.gleichklang.core.view.QuestionnaireView;
import de.binaerebauten.gleichklang.core.view.QuestionnaireView.QuestionnaireActivationListener;
import de.binaerebauten.gleichklang.core.view.commit_strategy.HideUnsavedNotificationValidationStrategy;
import de.binaerebauten.gleichklang.core.view.component.LocatableSelection;
import de.binaerebauten.gleichklang.core.view.component.ProductSelection;
import de.binaerebauten.gleichklang.core.view.component.question.QuestionGroupVerticalLayout;
import de.binaerebauten.gleichklang.memberweb.presenter.handler.DefaultPersonalDataHandler;
import de.binaerebauten.gleichklang.memberweb.service.RegistrationService;
import de.binaerebauten.gleichklang.memberweb.service.payment.ExternalPaymentFormService;
import de.binaerebauten.gleichklang.memberweb.view.*;
import de.binaerebauten.gleichklang.memberweb.view.PreConfigNotificationsView.PreConfigNotificationsViewListener;
import de.binaerebauten.gleichklang.memberweb.view.RegistrationReportingView.RegistrationReportingListener;
import de.binaerebauten.gleichklang.memberweb.view.component.RegistrationWizard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.vaadin.teemu.wizards.WizardStep;
import org.vaadin.teemu.wizards.event.*;

import java.net.URI;
import java.util.*;

import static de.binaerebauten.gleichklang.memberweb.view.component.RegistrationWizard.REGISTRATION_PAYMENT_RESULT_STEP;
import static de.binaerebauten.gleichklang.memberweb.view.component.RegistrationWizard.REGISTRATION_PAYMENT_STEP;

public class RegistrationPresenter extends NavigatePresenter implements RegistrationView.RegistrationViewListener,
		QuestionnaireActivationListener,
		SubscriptionOfferPaymentView.SubscriptionOfferPaymentViewListener,
		WizardProgressListener, PreConfigNotificationsViewListener
{
	private static final Logger LOG = LoggerFactory.getLogger(RegistrationPresenter.class);
	
	private final QuestionnaireActivation activation;
	
	private final RegistrationView registrationView;
	private final SubscriptionOfferPaymentView subscriptionOfferPaymentView;
	private final ExternalPaymentFormView externalPaymentFormView;
	private final PersonalDataView personalDataView;
	private final PreConfigNotificationsView preConfigNotificationsView;
	
	private final QuestionnaireRepository questionnaireRepository;
	private final AnswerRepository answerRepository;
	private final ExternalPaymentRegistrationRepository externalPaymentRegistrationRepository;
	
	private final QuestionnaireService questionnaireService;
	private final RegistrationService registrationService;
	private final UserService userService;
	private final ExternalPaymentFormService externalPaymentFormService;
	
	private final InvoiceService invoiceService;
	private final ApplicationContext ctx;
	
	private final AuthenticationService authenticationService;
	private final RegistrationWizard registrationWizard;
	
	private final PaymentService paymentService;
	private final PaymentRepository paymentRepository;
	
	private User currentUser;
	private LinkedHashMap<Questionnaire, QuestionnairePresenter> registrationStepViewMap;
	
	private SubscriptionOfferPaymentPresenter subscriptionOfferPresenter;
	
	private Product product;
	private PaymentMethod paymentMethod;
	private Map<RecommendationCategory, RegistrationReportingListener> reportingStepViewMap;
	//private final Map<String, StreamResource> reportStreamResourceMap = new LinkedHashMap<>();
	
	private Questionnaire firstQuestionnaireAfterPayment = null;
	
	/**
	 * This is an array for exceptional ordering of wizard steps. If NaturalKey
	 * is in this array it will be ignored by creation of initial ordering and
	 * added at the end.
	 */
	private List<NaturalKey> lastWizardSteps = Arrays.asList(NaturalKey.REGISTRATION, NaturalKey.FREE_TEXT_PARTNER, NaturalKey.FREE_TEXT_FRIENDSHIP);
	
	public RegistrationPresenter(ApplicationContext ctx, RegistrationView registrationView,
			SubscriptionOfferPaymentView subscriptionOfferPaymentView, ExternalPaymentFormView externalPaymentFormView, PersonalDataView personalDataView, PreConfigNotificationsView preConfigNotificationsView)
	{
		super(registrationView);
		
		this.ctx = ctx;
		this.userService = ctx.getBean(UserService.class);
		
		this.questionnaireRepository = ctx.getBean(QuestionnaireRepository.class);
		this.answerRepository = ctx.getBean(AnswerRepository.class);
		this.externalPaymentRegistrationRepository = ctx.getBean(ExternalPaymentRegistrationRepository.class);
		
		this.authenticationService = ctx.getBean(AuthenticationService.class);
		this.questionnaireService = ctx.getBean(QuestionnaireService.class);
		this.registrationService = ctx.getBean(RegistrationService.class);
		this.invoiceService = ctx.getBean(InvoiceService.class);
		this.paymentRepository = ctx.getBean(PaymentRepository.class);
		this.externalPaymentFormService = ctx.getBean(ExternalPaymentFormService.class);
		this.paymentService = ctx.getBean(PaymentService.class);
		
		CompleteUserRepository userWithAddressRepository = ctx.getBean(CompleteUserRepository.class);
		
		this.currentUser = userWithAddressRepository.findById(authenticationService.getAuthenticatedUserId());
		this.activation = questionnaireService.getActivation(currentUser);
		
		this.registrationView = registrationView;
		this.registrationWizard = registrationView.getRegistrationWizard();
		this.registrationView.setListener(this);
		this.registrationWizard.addListener(this);
		
		this.subscriptionOfferPaymentView = subscriptionOfferPaymentView;
		this.externalPaymentFormView = externalPaymentFormView;
		this.personalDataView = personalDataView;
		subscriptionOfferPaymentView.setListener(this);
		personalDataView.setPersonalDataComponent(new DefaultPersonalDataHandler(ctx, currentUser));
		
		this.preConfigNotificationsView = preConfigNotificationsView;
		this.preConfigNotificationsView.setListener(this);
	}
	
	@Override
	public void enter(String parameters)
	{
		if (registrationStepViewMap == null)
		{
			initRegistrationSteps();
		}
		
		registrationView.setPersonalDataStep(personalDataView);
		registrationView.setSubscriptionOfferView(subscriptionOfferPaymentView);
		registrationView.setExternalPaymentFormView(externalPaymentFormView);
		
		PaymentResultView paymentResultView = new PaymentResultViewImpl();
		registrationView.setPaymentResultView(paymentResultView);
		subscriptionOfferPresenter = new SubscriptionOfferPaymentPresenter(ctx, subscriptionOfferPaymentView,
				paymentResultView);
		
		registrationView.setPreConfigNotificationsView(preConfigNotificationsView);
		
		final Map<Questionnaire, QuestionnaireView> lastQuestionnairesMap = getLastQuestionnairesMap();
		firstQuestionnaireAfterPayment = lastQuestionnairesMap.keySet().iterator().next();
		
		registrationView.setLastQuestionSteps(lastQuestionnairesMap);
		registrationView.setFinalStep();
		
		String stepId = registrationService.getCurrentStepId(currentUser);
		nextStep(stepId);
	}
	
	/**
	 * For general case we activate the given stepId.
	 * <p>
	 * In case when the payment method and product were selected previously, we
	 * skip this page and go directly to the next screen.
	 */
	private void nextStep(String stepId)
	{
		if (REGISTRATION_PAYMENT_STEP.equals(stepId))
		{
			Optional<AbstractPayment> currentPayment = paymentRepository.findCurrentPayment(currentUser);
			if (currentPayment.isPresent())
			{
				ProductSelection productSelection = subscriptionOfferPaymentView.getProductSelection();
				
				UserPaymentSettings userPaymentSettings = paymentService.createOrGetUserPaymentSettings(currentUser);
				subscriptionOfferPaymentView.setUserPaymentSettings(userPaymentSettings);
				
				AbstractPayment payment = currentPayment.get();
				Optional<Product> product = payment.getInvoice().getItems().stream()
						.findFirst()
						.map(InvoiceItem::getProduct);
				if (payment.getState() == PaymentState.PENDING && product.isPresent())
				{
					productSelection.setProduct(product.get());
					
					// If this product is still available
					boolean isRegistered = Objects.nonNull(externalPaymentRegistrationRepository.findByUser(currentUser));
					
					// Skip step
					if (PaymentMethod.PREPAYMENT.equals(payment.getMethod()) || isRegistered)
					{
						stepId = REGISTRATION_PAYMENT_RESULT_STEP;
					}
				}
			}
		}
		
		registrationWizard.activateStep(stepId);
		registrationWizard.updateHeader();
	}
	
	private Map<Questionnaire, QuestionnaireView> getLastQuestionnairesMap()
	{
		Map<Questionnaire, QuestionnaireView> lastQuestionsMap = new LinkedHashMap<>();
		for (NaturalKey naturalKey : lastWizardSteps)
		{
			Questionnaire questionnaire = questionnaireRepository.findByNaturalKey(naturalKey);
			
			if (questionnaire.getRecommendationCategory() == null || currentUser.getOrderedCategories().contains(questionnaire.getRecommendationCategory()))
			{
				QuestionnaireView registrationQuestionnaireView = new RegistrationQuestionnaireViewImpl(questionnaire, new QuestionGroupVerticalLayout(new HideUnsavedNotificationValidationStrategy(), false));
				QuestionnairePresenter registrationQuestionnairePresenter = new QuestionnairePresenter(ctx, this, this.activation, registrationQuestionnaireView, false, ActivatorLevel.QUESTION_GROUP);
				lastQuestionsMap.put(questionnaire, registrationQuestionnaireView);
				registrationStepViewMap.put(questionnaire, registrationQuestionnairePresenter);
			}
		}
		return lastQuestionsMap;
	}
	
	private void initRegistrationSteps()
	{
		registrationStepViewMap = new LinkedHashMap<>();
		// for each RecommendationCategory in user create view and presenter
		for (RecommendationCategory category : currentUser.getOrderedCategories())
		{
			this.createQuestionnaireViewAndPresenter(category);
		}
		
		// create questionnaire view and presenter for questionnaires without category
		createQuestionnaireViewAndPresenter(null);
		
		reportingStepViewMap = getReportingStepMap();
		registrationView.setRegistrationStepViews(registrationStepViewMap, reportingStepViewMap);
	}
	
	private Map<RecommendationCategory, RegistrationReportingListener> getReportingStepMap()
	{
		Map<RecommendationCategory, RegistrationReportingListener> reportingListenerMap = new HashMap<>();
		RegistrationReportingViewImpl registrationReportingView = new RegistrationReportingViewImpl(null);
		RegistrationReportingListener registrationReportingListener = new RegistrationReportingPresenter(ctx, registrationReportingView);
		reportingListenerMap.put(null, registrationReportingListener);
		for (final RecommendationCategory recommendationCategory : currentUser.getCategories())
		{
			registrationReportingView = new RegistrationReportingViewImpl(recommendationCategory);
			registrationReportingListener = new RegistrationReportingPresenter(ctx, registrationReportingView);
			
			reportingListenerMap.put(recommendationCategory, registrationReportingListener);
		}
		return reportingListenerMap;
	}
	
	/**
	 * Creates view and presenter for each questionnaire for given {@link
	 * RecommendationCategory}
	 *
	 * @param recommendationCategory
	 */
	private void createQuestionnaireViewAndPresenter(RecommendationCategory recommendationCategory)
	{
		List<Questionnaire> questionnaires = questionnaireService.getQuestionnaires(Collections.singleton(recommendationCategory), false);
		for (Questionnaire questionnaire : questionnaires)
		{
			if (!lastWizardSteps.contains(questionnaire.getNaturalKey(Questionnaire.class)))
			{
				RegistrationQuestionnaireView registrationQuestionnaireView = new RegistrationQuestionnaireViewImpl(questionnaire, new QuestionGroupVerticalLayout(new HideUnsavedNotificationValidationStrategy(), false));
				QuestionnairePresenter registrationQuestionnairePresenter = new QuestionnairePresenter(ctx, this,
						this.activation, registrationQuestionnaireView, false, ActivatorLevel.QUESTION_GROUP);
				registrationStepViewMap.put(questionnaire, registrationQuestionnairePresenter);
			}
		}
	}
	
	@Override
	public void activateQuestionnaire(Questionnaire questionnaire, boolean enabled)
	{
		boolean isCompletedQuestionnaire = this.questionnaireService.isCompletedQuestionnaire(currentUser, questionnaire, activation);
		this.registrationView.enableNextQuestionnaire(isCompletedQuestionnaire);
	}
	
	@Override
	public void activeStepChanged(WizardStepActivationEvent event)
	{
		Component component = event.getActivatedStep().getContent();
		LOG.info("Activating {} ", component);
		activeStepChanged(component);
		
		registrationWizard.activateStep(event.getActivatedStep().getCaption());
		updateRegistrationState(event.getActivatedStep());
		
		// override save scroll position behaviour of Wizard Add-on
		UI.getCurrent().setScrollTop(0);
	}
	
	private void activeStepChanged(Component component)
	{
		if (component instanceof RegistrationQuestionnaireView)
		{
			RegistrationQuestionnaireView registrationQuestionnaireView = (RegistrationQuestionnaireView) component;
			Questionnaire questionnaire = registrationQuestionnaireView.getQuestionnaire();
			
			LOG.info("Activating questionnaire {} ", questionnaire.getName());
			
			QuestionnairePresenter questionnairePresenter = registrationStepViewMap.get(questionnaire);
			questionnairePresenter.enter();
		}
		else if (component instanceof RegistrationReportingView)
		{
			RegistrationReportingView registrationReportingView = (RegistrationReportingView) component;
			final RecommendationCategory category = registrationReportingView.getRecommendationCategory();
			String categoryName = category != null ? category.getName() : "personal";
			LOG.info("Activating reporting for category {} ", categoryName);
			
			reportingStepViewMap.get(category).enter();
		}
		else if (component instanceof PersonalDataView)
		{
			LOG.info("Activating PersonalDataView");
		}
		else if (component instanceof SubscriptionOfferPaymentView)
		{
			LOG.info("Activating SubscriptionOfferPaymentView");
			
			// If we go back, the already created payment should be cancelled
			Optional<AbstractPayment> currentPayment = paymentRepository.findCurrentPayment(currentUser).filter(p -> PaymentState.PENDING.equals(p.getState()));
			if (currentPayment.isPresent())
			{
				LOG.info("Found existing payment. It will be cancelled and replaced.");
				
				AbstractPayment payment = currentPayment.get();
				payment.setState(PaymentState.CANCELED);
				paymentRepository.save(payment);
			}
			
			subscriptionOfferPresenter.enter();
		}
		else if (component instanceof ExternalPaymentFormView)
		{
			LOG.info("Activating ExternalPaymentFormView");
			AbstractPayment payment = getPayment();
			processPayment(payment);
		}
		else if (component instanceof PaymentResultView)
		{
			LOG.info("Activating PaymentResultView");
			AbstractPayment payment = getPayment();
			subscriptionOfferPresenter.showPaymentResult(payment);
		}
		else if (component instanceof PreConfigNotificationsView)
		{
			LOG.info("Activating PreConfigNotificationsView");
		}
		
		registrationWizard.updateHeader();
	}
	
	private AbstractPayment getPayment()
	{
		Optional<AbstractPayment> currentPayment = paymentRepository.findCurrentPayment(currentUser);
		if (currentPayment.isPresent() && currentPayment.get().getState() == PaymentState.PENDING)
		{
			return currentPayment.get();
		}
		else
		{
			// Create a new payment
			Invoice invoice = invoiceService.createAndSaveInvoice(currentUser, product, paymentMethod);
			return invoice.getPayments().stream().findFirst().get();
		}
	}
	
	/**
	 * Shows the payment page: 1) if it is a prepaiment, then skips this page
	 * and goes to the result step 2) if it is an external payment, it shows an
	 * iFrame
	 *
	 * @param payment
	 */
	private void processPayment(AbstractPayment payment)
	{
		if (payment.getState() != PaymentState.PENDING || payment instanceof Prepayment)
		{
			// Jump to the next step
			registrationWizard.activateStep(REGISTRATION_PAYMENT_RESULT_STEP);
		}
		else
		{
			ExternalPaymentRegistration externalPaymentRegistration = externalPaymentRegistrationRepository.findByUser(payment.getUser());
			if (Objects.nonNull(externalPaymentRegistration) &&
					payment.getState() == PaymentState.PAID)
			{

				// User is already registered by the external system, skip this step
				registrationWizard.activateStep(REGISTRATION_PAYMENT_RESULT_STEP);
			}
			else
			{
				if(externalPaymentRegistration!=null)
				externalPaymentRegistrationRepository.delete(externalPaymentRegistration.getId());
				ExternalPayment externalPayment = (ExternalPayment) payment;

				try
				{
					URI paymentFormUrl = externalPaymentFormService.getInitialRegistrationFormUrl(externalPayment);
					externalPaymentFormView.setPaymentFormUrl(paymentFormUrl);
				}
				catch (PaymentException e)
				{
					LOG.error("Error while contacting external payment system", e);
					Notification.show(I18N.SUBSCRIPTIONPRESENTER_NOTIFICATION_EXTERNALPAYMENTERRORTITLE.msg(),
							I18N.SUBSCRIPTIONPRESENTER_NOTIFICATION_EXTERNALPAYMENTERROR.msg(),
							Type.ERROR_MESSAGE);
				}
			}
		}
	}
	
	private void updateRegistrationState(WizardStep wizardStep)
	{
		Component component = wizardStep.getContent();
		if (component instanceof RegistrationQuestionnaireView)
		{
			Questionnaire questionnaire = ((RegistrationQuestionnaireView) component).getQuestionnaire();
			registrationService.updateQuestionnaireRegistrationState(currentUser, questionnaire);
		}
		else if (component instanceof PersonalDataView)
		{
			registrationService.updateRegistrationState(currentUser, RegistrationState.MEMBERADDRESS);
		}
		else if (component instanceof SubscriptionOfferPaymentView)
		{
			registrationService.updateRegistrationState(currentUser, RegistrationState.PAYMENT);
		}
		else if (component instanceof PaymentResultView)
		{
			registrationService.updateRegistrationState(currentUser, RegistrationState.CONFIGNOTIFICATION);
		}
		else if (component instanceof PreConfigNotificationsView)
		{
			registrationService.updateRegistrationState(currentUser, RegistrationState.CONFIGNOTIFICATION);
		}
	}
	
	@Override
	public void stepSetChanged(WizardStepSetChangedEvent event)
	{
	}
	
	@Override
	public void wizardCompleted(WizardCompletedEvent event)
	{
	}
	
	@Override
	public void quickRegisterUser()
	{
		try
		{
			currentUser = userService.registerForTesting(currentUser);
			
			registrationWizard.getSteps().forEach(this::updateRegistrationState);
			int answers = this.answerRepository.countByUser(currentUser);
			String message = String.format("%d answers were persisted for user %s", answers,
					currentUser.getAlias());
			registrationWizard.activateStep(REGISTRATION_PAYMENT_STEP);
			Notification.show(message, Notification.Type.HUMANIZED_MESSAGE);
		}
		catch (Exception ex)
		{
			String message = String.format("Quick register failed %s ", ex.getMessage());
			Notification.show(message, Notification.Type.ERROR_MESSAGE);
		}
	}
	
	@Override
	public void quickFill()
	{
		WizardStep currentStep = registrationWizard.getCurrentStep();
		Component component = currentStep.getContent();
		if (component instanceof RegistrationQuestionnaireView)
		{
			RegistrationQuestionnaireView registrationQuestionnaireView = (RegistrationQuestionnaireView) component;
			registrationQuestionnaireView.quickFill();
		}
		else if (component instanceof PersonalDataView)
		{
			final PersonalDataView personalDataView = (PersonalDataView) component;
			setRandomValuesForFields(personalDataView.getFields());
		}
		else
		{
			Notification.show(I18N.HOMEPRESENTER_QUICKFILL_WARNING.msg(), Type.WARNING_MESSAGE);
		}
	}
	
	private void setRandomValuesForFields(Collection<Field<?>> fields)
	{
		for (Field field : fields)
		{
			if (field instanceof LocatableSelection)
			{
				LocatableSelection locatableSelection = (LocatableSelection) field;
				if (locatableSelection.getType().equals(Continent.class))
				{
					Continent continentValue = ctx.getBean(LocatableRepository.class).findContinentWithZips().get(0);
					locatableSelection.getItemIds().stream().filter(o -> o.equals(continentValue)).findAny().ifPresent(locatableSelection::setValue);
				}
				else
				{
					locatableSelection.setValue(locatableSelection.getContainerDataSource().getItemIds().iterator().next());
				}
			}
			else if (field instanceof CheckBox)
			{
				CheckBox checkBox = (CheckBox) field;
				checkBox.setValue(true);
			}
			else if (field instanceof AbstractSelect)
			{
				AbstractSelect abstractSelect = (AbstractSelect) field;
				abstractSelect.setValue(abstractSelect.getContainerPropertyIds().iterator().next());
			}
			
			else if (field instanceof TextField)
			{
				TextField textField = (TextField) field;
				textField.setValue("test");
			}
		}
	}
	
	@Override
	public void wizardCancelled(WizardCancelledEvent event)
	{
		this.authenticationService.logout(RedirectGoal.LANDING);
	}
	
	@Override
	public void select(Product product, PaymentMethod paymentMethod)
	{
		this.product = product;
		this.paymentMethod = paymentMethod;
	}
	
	@Override
	public void actionCodeChanged(String actionCode)
	{
		// nothing to do here, handled by SubscriptionOfferPaymentPresenter
	}
	
	@Override
	public void paymentMethodChanged(PaymentMethod paymentMethod)
	{
		// nothing to do here, handled by SubscriptionOfferPaymentPresenter
	}
	
	/**
	 * Executes after click on finish button Deletes registration states and
	 *
	 * @param listener
	 */
	public void addOnFinishClickListener(Button.ClickListener listener)
	{
		this.registrationWizard.addOnFinishClickListener(event ->
		{
			currentUser = userService.activateUser(userService.getCurrentUser());
			registrationWizard.destroyWizard();
			listener.buttonClick(event);
		});
	}
	
	@Override
	public void setNotifications(boolean systemNotificationsEnabled, boolean marketingNotificationsEnabled) throws ValidationException
	{
		userService.setPreConfigNotifications(userService.getCurrentUser(), systemNotificationsEnabled, marketingNotificationsEnabled);
	}
}

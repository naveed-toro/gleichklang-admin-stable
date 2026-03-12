package de.binaerebauten.gleichklang.core.utils;

import com.google.common.collect.Sets;
import de.binaerebauten.gleichklang.core.model.I18NEntity;
import de.binaerebauten.gleichklang.core.model.I18NEntity.BaseName;
import de.binaerebauten.gleichklang.core.model.I18NEntity.Language;
import de.binaerebauten.gleichklang.core.model.NaturalKeyEntity.NaturalKey;
import de.binaerebauten.gleichklang.core.model.locatable.Continent;
import de.binaerebauten.gleichklang.core.model.locatable.Country;
import de.binaerebauten.gleichklang.core.model.locatable.Region;
import de.binaerebauten.gleichklang.core.model.locatable.Zip;
import de.binaerebauten.gleichklang.core.model.mail.*;
import de.binaerebauten.gleichklang.core.model.matching.*;
import de.binaerebauten.gleichklang.core.model.matching.AreaMatchStatisticEntry.MatchArea;
import de.binaerebauten.gleichklang.core.model.matching.MatchStatistic.MatchingScope;
import de.binaerebauten.gleichklang.core.model.matching.MatchStatisticEntry.LogArea;
import de.binaerebauten.gleichklang.core.model.matching.MatrixValue.Strictness;
import de.binaerebauten.gleichklang.core.model.matching.Relationship.Affiliation;
import de.binaerebauten.gleichklang.core.model.media.Avatar;
import de.binaerebauten.gleichklang.core.model.media.FileEntity;
import de.binaerebauten.gleichklang.core.model.media.FileEntity.FileType;
import de.binaerebauten.gleichklang.core.model.media.Media;
import de.binaerebauten.gleichklang.core.model.media.MediaGallery;
import de.binaerebauten.gleichklang.core.model.message.*;
import de.binaerebauten.gleichklang.core.model.message.Message.MessageType;
import de.binaerebauten.gleichklang.core.model.news.News;
import de.binaerebauten.gleichklang.core.model.news.UserNews;
import de.binaerebauten.gleichklang.core.model.payment.InitialSubscriptionOffer;
import de.binaerebauten.gleichklang.core.model.questionnaire.*;
import de.binaerebauten.gleichklang.core.model.questionnaire.Question.Requirement;
import de.binaerebauten.gleichklang.core.model.user.*;
import de.binaerebauten.gleichklang.core.model.user.ClientInformation.Browser;
import de.binaerebauten.gleichklang.core.model.user.ClientInformation.Device;
import de.binaerebauten.gleichklang.core.model.user.ClientInformation.OS;
import de.binaerebauten.gleichklang.core.model.user.EmailLink.EmailLinkContext;
import de.binaerebauten.gleichklang.core.model.user.EmailLinkParameter.ParameterType;
import de.binaerebauten.gleichklang.core.model.user.UserActivityLog.UserActivity;
import de.binaerebauten.gleichklang.core.repository.*;
import de.binaerebauten.gleichklang.core.repository.mail.MailQueueEntryRepository;
import de.binaerebauten.gleichklang.core.repository.mail.NewsletterRepository;
import de.binaerebauten.gleichklang.core.repository.mail.UndeliverableMailRepository;
import de.binaerebauten.gleichklang.core.repository.mail.UserMailQueueEntryRepository;
import de.binaerebauten.gleichklang.core.repository.matching.MatchRepository;
import de.binaerebauten.gleichklang.core.repository.matching.MatchStatisticRepository;
import de.binaerebauten.gleichklang.core.repository.message.AdminWorkItemRepository;
import de.binaerebauten.gleichklang.core.repository.message.MessageAttachmentRepository;
import de.binaerebauten.gleichklang.core.repository.message.MessageRepository;
import de.binaerebauten.gleichklang.core.repository.message.ScammingRepository;
import de.binaerebauten.gleichklang.core.repository.user.*;
import de.binaerebauten.gleichklang.core.service.AnswerFactory;
import de.binaerebauten.gleichklang.core.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import javax.validation.ConstraintViolationException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class DefaultEntityFactory
{
	public enum MessageState
	{
		SENT, //senderUser send to receiverUser
		DRAFT, //sendUser has draft
		HIDDEN_DRAFT, //sendUser hide draft
		HIDDEN_INCOMING, //sendUser send to receiverUser (receiverUser hide message)
		HIDDEN_OUTGOING, //sendUser send to receiverUser (sendUser hide message)
		ADMIN_SENT, //admin send to receiverUser
		ADMIN_RECEIVED, //senderUser send to admin
		DELETED_DRAFT, //sendUser delete draft
		DELETED_HIDDEN_DRAFT, //sendUser hide and then delete draft
		DELETED_INCOMING, //sendUser send to receiverUser (receiverUser deleted message)
		DELETED_OUTGOING, //sendUser send to receiverUser (sendUser deleted message)
		SENT_CANCEL_MESSAGE
	}
	
	private static final Logger LOG = LoggerFactory.getLogger(DefaultEntityFactory.class);

	@Autowired
	private AvatarRepository avatarRepository;
	/**
	 *
	 */
	@Autowired
	private AdminRepository adminRepository;
	@Autowired
	private AffiliatePaymentStateRepository affiliatePaymentStateRepository;
	@Autowired
	private FileRepository fileRepository;
	@Autowired
	private MediaRepository mediaRepository;
	@Autowired
	private MediaGalleryRepository mediaGalleryRepository;
	@Autowired
	private QuestionRepository questionRepository;
	@Autowired
	private QuestionnaireRepository questionnaireRepository;
	@Autowired
	private QuestionGroupRepository questionGroupRepository;
	@Autowired
	private ActivatorRepository activatorRepository;
	@Autowired
	private MessageRepository messageRepository;
	@Autowired
	private MessageAttachmentRepository messageAttachmentRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private UserService userService;
	@Autowired
	private AddressRepository addressRepository;
	@Autowired
	private MatrixRepository matrixRepository;
	@Autowired
	private QuestionMappingRepository questionMappingRepository;
	@Autowired
	private MatchRepository matchRepository;
	@Autowired
	private ProductRepository<InitialSubscriptionOffer> initialSubscriptionOfferProductRepository;
	@Autowired
	private SubscriptionRepository subscriptionRepository;
	@Autowired
	private BankAccountRepository bankAccountRepository;
	@Autowired
	private InvoiceRepository invoiceRepository;
	@Autowired
	private PrepaymentRepository prepaymentRepository;
	@Autowired
	private ExternalPaymentRepository externalPaymentRepository;
	@Autowired
	private AnswerRepository answerRepository;
	@Autowired
	private LocatableRepository locatableRepository;
	@Autowired
	private RelationshipRepository relationshipRepository;
	@Autowired
	private ChoiceGroupRepository choiceGroupRepository;
	@Autowired
	private I18NRepository i18NRepository;
	@Autowired
	private HeidelpayTransactionRepository heidelpayTransactionRepository;
	@Autowired
	private ExternalPaymentRegistrationRepository externalPaymentRegistrationRepository;
	@Autowired
	private UserPaymentSettingsRepository userPaymentSettingsRepository;
	@Autowired
	private UserRegistrationStateRepository userRegistrationStateRepository;
	@Autowired
	private NewsRepository newsRepository;
	@Autowired
	private UserNewsRepository userNewsRepository;
	@Autowired
	private MailQueueEntryRepository mailQueueEntryRepository;
	@Autowired
	private UserMailQueueEntryRepository userMailQueueEntryRepository;
	@Autowired
	private UserActivityLogRepository userActivityLogRepository;
	@Autowired
	private RecommendationBreakRepository recommendationBreakRepository;
	@Autowired
	private AdminWorkItemRepository adminWorkItemRepository;
	@Autowired
	private ClientInformationRepository clientInformationRepository;
	@Autowired
	private EmailLinkRepository emailLinkRepository;
	@Autowired
	private UndeliverableMailRepository undeliverableMailRepository;
	@Autowired
	private MatchStatisticRepository matchStatisticRepository;
	@Autowired
	private ScammingRepository scammingRepository;
	@Autowired
	private NewsletterRepository newsletterRepository;

	public DefaultEntityFactory()
	{
	}

	private Questionnaire createDefaultQuestionnaire()
	{
		final Questionnaire questionnaire = new Questionnaire();
		questionnaire.setSortOrder(questionnaireRepository.findMaxSortOrder(null) + 1);
		questionnaire.setI18nKey(DefaultStaticEntityFactory.createUniqueString());
		return questionnaire;
	}

	@Transactional
	public Questionnaire persistDefaultQuestionnaire()
	{
		final Questionnaire questionnaire = createDefaultQuestionnaire();

		return questionnaireRepository.save(questionnaire);
	}

	@Transactional
	public Questionnaire persistDefaultQuestionnaire(RecommendationCategory category)
	{
		final Questionnaire questionnaire = createDefaultQuestionnaire();
		questionnaire.setRecommendationCategory(category);

		return questionnaireRepository.save(questionnaire);
	}

	@Transactional
	public Questionnaire persistDefaultQuestionnaireWithQuestionGroup(RecommendationCategory category, I18NEntity questionnaireI18N)
	{
		Questionnaire questionnaire = createDefaultQuestionnaire();
		questionnaire.setRecommendationCategory(category);
		questionnaire.setI18nKey(questionnaireI18N.getKey());
		questionnaire = questionnaireRepository.save(questionnaire);
		QuestionGroup questionGroup = persistDefaultQuestionGroup(questionnaire);
		persistDefaultRequiredQuestions(questionGroup);
		return questionnaire;
	}

	@Transactional
	public NumberQuestion persistDefaultNumberQuestion(QuestionGroup questionGroup, I18NEntity numberI18N)
	{
		final NumberQuestion numberQuestion = new NumberQuestion();
		numberQuestion.setI18nKey(numberI18N.getKey());
		numberQuestion.setSortOrder(questionRepository.findMaxSortOrder(null) + 1);
		numberQuestion.setRequirement(Requirement.OPTIONAL);
		numberQuestion.setMinVal(18);
		numberQuestion.setMaxVal(99);
		questionGroup.addQuestion(numberQuestion);

		return questionRepository.save(numberQuestion);
	}

	@Transactional
	public ChoiceQuestion persistDefaultChoiceQuestion(QuestionGroup questionGroup, I18NEntity choiceI18N)
	{
		final ChoiceQuestion choiceQuestion = new ChoiceQuestion();
		choiceQuestion.setI18nKey(choiceI18N.getKey());
		choiceQuestion.setSortOrder(questionRepository.findMaxSortOrder(null) + 1);
		questionGroup.addQuestion(choiceQuestion);
		choiceQuestion.setRequirement(Requirement.OPTIONAL);
		choiceQuestion.setSelectionType(ChoiceQuestion.SelectionType.MULTIPLE);
		choiceQuestion.setChoiceGroup(persistDefaultChoiceGroup());
		return questionRepository.save(choiceQuestion);
	}

	@Transactional
	public ChoiceGroup persistDefaultChoiceGroup()
	{
		final ChoiceGroup choiceGroup = new ChoiceGroup();
		choiceGroup.setI18nKey(persistDefaultI18NEntry().getKey());
		final List<Choice> choices = createDefaultChoices(choiceGroup);
		choiceGroup.setChoices(choices);

		return choiceGroupRepository.save(choiceGroup);
	}

	private List<Choice> createDefaultChoices(ChoiceGroup choiceGroup)
	{
		final List<Choice> choices = new ArrayList<>();
		for (int i = 0; i < 5; i++)
		{
			final Choice choice = new Choice();
			choice.setChoiceGroup(choiceGroup);
			choice.setSortOrder(i + 1);
			choices.add(choice);
		}

		return choices;
	}

	@Transactional
	public TextQuestion persistDefaultTextQuestion()
	{
		final I18NEntity i18NEntity = persistDefaultI18NEntry();
		final Questionnaire questionnaire = persistDefaultQuestionnaire();
		final QuestionGroup questionGroup = persistDefaultQuestionGroup(questionnaire);
		return persistDefaultTextQuestion(questionGroup, i18NEntity);
	}

	@Transactional
	public TextQuestion persistDefaultTextQuestion(QuestionGroup questionGroup,
			I18NEntity i18NEntity)
	{
		final TextQuestion textQuestion = new TextQuestion();
		textQuestion.setI18nKey(i18NEntity.getKey());
		textQuestion.setSortOrder(questionRepository.findMaxSortOrder(null) + 1);
		textQuestion.setRequirement(Requirement.REQUIRED);
		textQuestion.setMaxLength(10);
		textQuestion.setNumberOfLines(2);

		questionGroup.addQuestion(textQuestion);
		return questionRepository.save(textQuestion);
	}

	@Transactional
	public List<Question> persistDefaultRequiredQuestions(QuestionGroup questionGroup)
	{
		final List<Question> questions = new ArrayList<>();

		questions.add(persistDefaultTextQuestion(questionGroup, persistDefaultI18NEntry()));
		questions.add(persistDefaultChoiceQuestion(questionGroup, persistDefaultI18NEntry()));
		questions.add(persistDefaultNumberQuestion(questionGroup, persistDefaultI18NEntry()));
		questions.add(persistDefaultTextQuestion(questionGroup, persistDefaultI18NEntry()));
		questions.add(persistDefaultTextQuestion(questionGroup, persistDefaultI18NEntry()));
		
		for(Question question : questions)
		{
			question.setRequirement(Requirement.REQUIRED);
			questionRepository.save(question);
		}

		return questions;
	}

	@Transactional
	public QuestionActivator persistDefaultQuestionActivator()
	{
		final Questionnaire questionnaire = persistDefaultQuestionnaire();
		final I18NEntity choiceI18N = persistDefaultI18NEntry();
		final QuestionGroup questionGroup = persistDefaultQuestionGroup(questionnaire);
		final ChoiceQuestion choiceQuestion = persistDefaultChoiceQuestion(questionGroup, choiceI18N);

		final QuestionActivator questionActivator = new QuestionActivator();
		questionActivator.setEnablesQuestion(persistDefaultTextQuestion());
		questionActivator.setActivatingQuestion(choiceQuestion);
		questionActivator.setActivatingChoices(choiceQuestion.getChoiceGroup().getChoices().stream().limit(2).collect(Collectors.toSet()));
		questionActivator.setNaturalKey(UUID.randomUUID().toString());

		return activatorRepository.save(questionActivator);
	}

	private QuestionGroup persistDefaultQuestionGroup(Questionnaire questionnaire, I18NEntity i18NKey)
	{
		final QuestionGroup questionGroup = DefaultStaticEntityFactory.createDefaultQuestionGroup(questionnaire, i18NKey);
		return questionGroupRepository.save(questionGroup);
	}

	@Transactional
	public QuestionGroup persistDefaultQuestionGroup(
			Questionnaire questionnaire)
	{

		final I18NEntity i18NKey = persistDefaultI18NEntry();
		final QuestionGroup questionGroup = DefaultStaticEntityFactory.createDefaultQuestionGroup(questionnaire, i18NKey);
		return questionGroupRepository.save(questionGroup);
	}
	
	@Transactional
	public QuestionnaireActivator persistDefaultQuestionnaireActivator()
	{
		final Questionnaire questionnaire = persistDefaultQuestionnaire();
		final I18NEntity choiceI18N = persistDefaultI18NEntry();

		final QuestionGroup questionGroup = persistDefaultQuestionGroup(questionnaire);
		final ChoiceQuestion choiceQuestion = persistDefaultChoiceQuestion(questionGroup, choiceI18N);

		final QuestionnaireActivator questionnaireActivator = new QuestionnaireActivator();
		questionnaireActivator.setEnablesQuestionnaire(persistDefaultQuestionnaire());
		questionnaireActivator.setActivatingQuestion(choiceQuestion);
		questionnaireActivator.setActivatingChoices(choiceQuestion.getChoiceGroup().getChoices().stream().limit(2).collect(Collectors.toSet()));
		questionnaireActivator.setNaturalKey(UUID.randomUUID().toString());

		return activatorRepository.save(questionnaireActivator);
	}

	@Transactional
	public QuestionGroupActivator persistDefaultQuestionGroupActivator()
	{
		final Questionnaire questionnaire = persistDefaultQuestionnaire();
		final I18NEntity choiceI18N = persistDefaultI18NEntry();
		final QuestionGroup questionGroup = persistDefaultQuestionGroup(questionnaire);
		final ChoiceQuestion choiceQuestion = persistDefaultChoiceQuestion(questionGroup, choiceI18N);

		final QuestionGroupActivator questionGroupActivator = new QuestionGroupActivator();
		questionGroupActivator.setEnablesQuestionGroup(persistDefaultQuestionGroup(persistDefaultQuestionnaire()));
		questionGroupActivator.setActivatingQuestion(choiceQuestion);
		questionGroupActivator.setActivatingChoices(choiceQuestion.getChoiceGroup().getChoices().stream().limit(2).collect(Collectors.toSet()));
		questionGroupActivator.setNaturalKey(UUID.randomUUID().toString());

		return activatorRepository.save(questionGroupActivator);
	}

	/**
	 * Creates a default Draft-Message and persist this message.
	 *
	 * @param userReceiver the receiver of the message
	 * @param userSender   the sender of the message
	 * @return the persisted message
	 */
	@Transactional
	public Message persistDefaultMessage(User userReceiver, User userSender, MessageState messageState)
	{
		// sleep to prevent timing problem for LocalDateTime.now() in creating of the messages
		try
		{
			Thread.sleep(10);
		}
		catch (InterruptedException ignore)
		{
		}
		
		final Message message = createDefaultMessage(userReceiver, userSender);
		if(messageState != null) updateMessage(message, messageState);

		return messageRepository.save(message);
	}
	
	private void updateMessage(Message message, MessageState messageState)
	{
		final String body = "body " + messageState.toString();
		final String subject = "subject " + messageState.toString();
		
		message.setBody(body);
		message.setSubject(subject);
		
		switch (messageState)
		{
			case SENT:
				message.setSendDate(LocalDateTime.now());
				message.setSent(true);
				break;
			case DRAFT:
				break;
			case HIDDEN_DRAFT:
				message.getSenderEnvelope().setHidden(true);
				break;
			case HIDDEN_INCOMING:
				message.setSendDate(LocalDateTime.now());
				message.setSent(true);
				message.getReceiverEnvelope().setHidden(true);
				break;
			case HIDDEN_OUTGOING:
				message.setSendDate(LocalDateTime.now());
				message.setSent(true);
				message.getSenderEnvelope().setHidden(true);
				break;
			case ADMIN_SENT:
				message.setSendDate(LocalDateTime.now());
				message.setSent(true);
				message.getSenderEnvelope().setUser(null);
				break;
			case ADMIN_RECEIVED:
				message.setSendDate(LocalDateTime.now());
				message.setSent(true);
				message.getReceiverEnvelope().setUser(null);
				break;
			case DELETED_DRAFT:
				message.getSenderEnvelope().setDeleted(true);
				break;
			case DELETED_HIDDEN_DRAFT:
				message.getSenderEnvelope().setDeleted(true);
				message.getSenderEnvelope().setHidden(true);
				break;
			case DELETED_INCOMING:
				message.setSendDate(LocalDateTime.now());
				message.setSent(true);
				message.getReceiverEnvelope().setDeleted(true);
				break;
			case DELETED_OUTGOING:
				message.setSendDate(LocalDateTime.now());
				message.setSent(true);
				message.getSenderEnvelope().setDeleted(true);
				break;
			case SENT_CANCEL_MESSAGE:
				message.setSendDate(LocalDateTime.now());
				message.setSent(true);
				message.setMessageType(MessageType.CANCEL_MESSAGE);
				break;
		}
	}

	public Message createDefaultMessage(User userReceiver, User userSender)
	{
		final Message message = new Message();
		message.setBody("body");
		message.setSubject("subject");

		final ReceiverEnvelope receiver = new ReceiverEnvelope();
		receiver.setUser(userReceiver);
		receiver.setMessage(message);

		final SenderEnvelope sender = new SenderEnvelope();
		sender.setUser(userSender);
		sender.setMessage(message);

		message.setSenderEnvelope(sender);
		message.setReceiverEnvelope(receiver);
		return message;
	}

	@Transactional
	public MessageAttachment persistDefaultMessageAttachment()
	{
		final Message message = persistDefaultMessage(persistDefaultUser(UUID.randomUUID().toString()), persistDefaultUser(UUID.randomUUID().toString()), null);
		final FileEntity fileEntity = persistDefaultFileEntity();

		final MessageAttachment messageAttachment = new MessageAttachment();
		messageAttachment.setMessage(message);
		messageAttachment.setFile(fileEntity);

		return messageAttachmentRepository.save(messageAttachment);
	}

	@Transactional
	public User persistDefaultUser(String alias, RecommendationCategory... recommendationCategories)
	{
		final String email = alias.replaceAll(" ", "") + "@example.com";
		return persistDefaultUser(email, alias, recommendationCategories);
	}

	@Transactional
	public User persistDefaultUser(String email, String alias, RecommendationCategory... recommendationCategories)
	{
		return persistDefaultUser(email, alias, MemberStatus.REGISTERED, recommendationCategories);
	}

	@Transactional
	public User persistDefaultUser(String email, String alias, MemberStatus memberStatus, RecommendationCategory... recommendationCategories)
	{
		final User defaultUser = DefaultStaticEntityFactory.createDefaultUser(email, alias, memberStatus, recommendationCategories);
		final Country defaultCountry = persistDefaultCountry();
		final Zip zip = persistDefaultZip(defaultCountry);

		Address address = DefaultStaticEntityFactory.createDefaultAddress(zip);
		address.setPayment(true);
		address.setUser(defaultUser);
		defaultUser.addAddress(address);

		return userRepository.save(defaultUser);
	}

	@Transactional
	public Admin persistDefaultAdmin(String email, AdminRole... roles)
	{
		Admin admin = new Admin();
		admin.setEmail(email);
		admin.setAlias(email);
		admin.setPassword("blub");
		admin.setRoles(new HashSet<>(Arrays.asList(roles)));

		return adminRepository.save(admin);
	}

	@Transactional
	public MatchingMatrix persistDefaultMatchingMatrix()
	{
		final Questionnaire questionnaire = persistDefaultQuestionnaire();
		final List<I18NEntity> choiceI18N = persistDefaultI18NEntries(2);
		final QuestionGroup questionGroup = persistDefaultQuestionGroup(questionnaire);
		return persistDefaultMatchingMatrix(persistDefaultChoiceQuestion(questionGroup, choiceI18N.get(0)), persistDefaultChoiceQuestion(questionGroup, choiceI18N.get(1)));
	}

	private MatchingMatrix persistDefaultMatchingMatrix(ChoiceQuestion sourceQuestion, ChoiceQuestion targetQuestion)
	{
		final MatchingMatrix matrix = new MatchingMatrix();
		matrix.setName(UUID.randomUUID().toString().substring(0, 31));
		matrix.setTargetChoiceGroup(targetQuestion.getChoiceGroup());
		matrix.setSourceChoiceGroup(sourceQuestion.getChoiceGroup());

		for (Choice targetChoice : matrix.getTargetChoiceGroup().getChoices())
		{
			for (Choice sourceChoice : matrix.getSourceChoiceGroup().getChoices())
			{
				final MatrixValue matrixValue = new MatrixValue();
				matrixValue.setSourceChoice(sourceChoice);
				matrixValue.setTargetChoice(targetChoice);
				matrixValue.setMatrix(matrix);
				matrixValue.setStrictness(Arrays.asList(Strictness.values()).stream().findAny().get());
				matrix.getMatrixValues().add(matrixValue);
			}
		}

		return matrixRepository.save(matrix);
	}

	@Transactional
	public NumberQuestionsMapping persistDefaultNumberQuestionsMapping(RecommendationCategory recommendationCategory)
	{

		final Questionnaire questionnaire = persistDefaultQuestionnaire(recommendationCategory);

		final List<I18NEntity> numberI18Ns = persistDefaultI18NEntries(3);
		final QuestionGroup questionGroup = persistDefaultQuestionGroup(questionnaire, numberI18Ns.get(0));

		final NumberQuestion factQuestion = persistDefaultNumberQuestion(questionGroup, numberI18Ns.get(0));
		final NumberQuestion maxQuestion = persistDefaultNumberQuestion(questionGroup, numberI18Ns.get(1));
		final NumberQuestion minQuestion = persistDefaultNumberQuestion(questionGroup, numberI18Ns.get(2));

		return persistDefaultNumberQuestionsMapping(factQuestion, minQuestion, maxQuestion);
	}

	@Transactional
	public NumberQuestionsMapping persistDefaultNumberQuestionsMapping(NumberQuestion factQuestion, NumberQuestion minQuestion, NumberQuestion maxQuestion)
	{
		final NumberQuestionsMapping questionsMapping = new NumberQuestionsMapping();

		questionsMapping.setFactQuestion(factQuestion);
		questionsMapping.setMaxQuestion(maxQuestion);
		questionsMapping.setMinQuestion(minQuestion);
		questionsMapping.setNaturalKey(UUID.randomUUID().toString());

		return questionMappingRepository.save(questionsMapping);
	}

	@Transactional
	public ChoiceQuestionsMapping persistDefaultChoiceQuestionsMapping(RecommendationCategory recommendationCategory)
	{
		final List<I18NEntity> choiceI18N = persistDefaultI18NEntries(2);
		final Questionnaire questionnaire = persistDefaultQuestionnaire(recommendationCategory);
		final QuestionGroup questionGroup = persistDefaultQuestionGroup(questionnaire);

		final ChoiceQuestion sourceQuestion = persistDefaultChoiceQuestion(questionGroup, choiceI18N.get(0));
		final ChoiceQuestion targetQuestion = persistDefaultChoiceQuestion(questionGroup, choiceI18N.get(1));

		return persistDefaultChoiceQuestionsMapping(sourceQuestion, targetQuestion);
	}

	@Transactional
	public ChoiceQuestionsMapping persistDefaultChoiceQuestionsMapping(ChoiceQuestion sourceQuestion, ChoiceQuestion targetQuestion)
	{
		final ChoiceQuestionsMapping questionsMapping = new ChoiceQuestionsMapping();

		questionsMapping.setSourceQuestion(sourceQuestion);
		questionsMapping.setTargetQuestion(targetQuestion);
		questionsMapping.setMatrix(persistDefaultMatchingMatrix(questionsMapping.getSourceQuestion(), questionsMapping.getTargetQuestion()));
		questionsMapping.setNaturalKey(UUID.randomUUID().toString());

		return questionMappingRepository.save(questionsMapping);
	}

	public AffinityMapping persistDefaultAffinityMapping()
	{
		return persistDefaultAffinityMapping((RecommendationCategory) null);
	}

	@Transactional
	public AffinityMapping persistDefaultAffinityMapping(RecommendationCategory recommendationCategory)
	{
		final int questionCount = 4;

		final List<I18NEntity> choiceI18N = persistDefaultI18NEntries(questionCount);

		final Questionnaire questionnaire = persistDefaultQuestionnaire(recommendationCategory);
		final QuestionGroup questionGroup = persistDefaultQuestionGroup(questionnaire);

		final ChoiceQuestion[] choiceQuestions = new ChoiceQuestion[questionCount];

		for (int i = 0; i < questionCount; i++)
		{
			choiceQuestions[i] = persistDefaultChoiceQuestion(questionGroup, choiceI18N.get(i));
		}

		return persistDefaultAffinityMapping(choiceQuestions);
	}

	@Transactional
	public AffinityMapping persistDefaultAffinityMapping(ChoiceQuestion... choiceQuestions)
	{
		final AffinityMapping questionMapping = new AffinityMapping();
		questionMapping.setMaxDistance(1);
		questionMapping.getQuestions().addAll(Arrays.asList(choiceQuestions));
		questionMapping.setNaturalKey(UUID.randomUUID().toString());

		return questionMappingRepository.save(questionMapping);
	}

	@Transactional
	public AgeQuestionMapping persistDefaultAgeQuestionMapping(RecommendationCategory recommendationCategory)
	{

		final Questionnaire questionnaire = persistDefaultQuestionnaire(recommendationCategory);

		final List<I18NEntity> numberI18Ns = persistDefaultI18NEntries(2);
		final QuestionGroup questionGroup = persistDefaultQuestionGroup(questionnaire, numberI18Ns.get(0));

		final NumberQuestion minQuestion = persistDefaultNumberQuestion(questionGroup, numberI18Ns.get(0));
		final NumberQuestion maxQuestion = persistDefaultNumberQuestion(questionGroup, numberI18Ns.get(1));

		return persistDefaultAgeQuestionMapping(minQuestion, maxQuestion);
	}

	@Transactional
	public AgeQuestionMapping persistDefaultAgeQuestionMapping(NumberQuestion minQuestion, NumberQuestion maxQuestion)
	{
		final AgeQuestionMapping questionsMapping = new AgeQuestionMapping();

		questionsMapping.setMaxAgeQuestion(maxQuestion);
		questionsMapping.setMinAgeQuestion(minQuestion);
		questionsMapping.setNaturalKey(UUID.randomUUID().toString());

		return questionMappingRepository.save(questionsMapping);
	}

	@Transactional
	public AvatarQuestionMapping persistDefaultAvatarQuestionMapping(RecommendationCategory category)
	{
		final AvatarQuestionMapping questionsMapping = new AvatarQuestionMapping();
		final I18NEntity choiceI18N = persistDefaultI18NEntry();

		final Questionnaire questionnaire = persistDefaultQuestionnaire(category);
		final QuestionGroup questionGroup = persistDefaultQuestionGroup(questionnaire);
		final ChoiceQuestion question = persistDefaultChoiceQuestion(questionGroup, choiceI18N);

		questionsMapping.setAvatarQuestion(question);
		questionsMapping.setTrueChoice(question.getChoiceGroup().getChoices().get(0));
		questionsMapping.setNaturalKey(UUID.randomUUID().toString());

		return questionMappingRepository.save(questionsMapping);
	}

	public I18NEntity persistDefaultI18NEntry(I18NEntity.BaseName baseName, I18NEntity.Language language, String key, String value)
	{
		I18NEntity defaultI18NEntry = createDefaultI18NEntry(baseName, language, key, value);

		return i18NRepository.save(defaultI18NEntry);
	}
	
	public I18NEntity createDefaultI18NEntry(NaturalKey naturalKey)
	{
		return persistDefaultI18NEntry(BaseName.NONE, Language.DE, naturalKey.naturalKey, naturalKey.naturalKey);
	}

	public I18NEntity createDefaultI18NEntry(I18NEntity.BaseName baseName, I18NEntity.Language language, String key, String value)
	{
		final I18NEntity i18NEntity = new I18NEntity();

		i18NEntity.setKey(key);
		i18NEntity.setBaseName(baseName);
		i18NEntity.setLanguage(language);
		i18NEntity.setValue(value);

		return i18NEntity;
	}

	@Transactional
	public List<I18NEntity> persistDefaultI18NEntries(int number)
	{
		return persistDefaultI18NEntries(DefaultStaticEntityFactory.createUniqueString(), number);
	}

	@Transactional
	public List<I18NEntity> persistDefaultI18NEntries(String prefix, int number)
	{
		final List<I18NEntity> i18NEntities = new ArrayList<>();
		for (int i = 0; i < number; i++)
		{
			i18NEntities.add(DefaultStaticEntityFactory.createDefaultI18NEntry(prefix + "_key " + i, prefix + "_val " + i));
		}
		return i18NRepository.save(i18NEntities);
	}

	@Transactional
	public I18NEntity persistDefaultI18NEntry()
	{
		return persistDefaultI18NEntry(DefaultStaticEntityFactory.createUniqueString());
	}

	@Transactional
	public I18NEntity persistDefaultI18NEntry(String prefix)
	{
		return i18NRepository.save(DefaultStaticEntityFactory.createDefaultI18NEntry(prefix + "_key", prefix + "_val"));
	}

	@Transactional
	public List<Answer> persistDefaultAnswers(Collection<Question> questions, User user)
	{
		final List<Answer> answers = new ArrayList<>();

		questions.forEach(question ->
		{

			Answer answer = AnswerFactory.get().createNewAnswer(question, user);
			if (answer instanceof ChoiceAnswer)
			{
				final Set<Choice> choices = Sets.newHashSet(((ChoiceQuestion) question).getChoiceGroup().getChoices());
				((ChoiceAnswer) answer).setChoices(choices);
			}
			else if (answer instanceof TextAnswer)
			{
				((TextAnswer) answer).setTextValue("text");
			}
			else if (answer instanceof NumberAnswer)
			{
				((NumberAnswer) answer).setNumberValue(42);
			}
			answer.setQuestion(question);
			answer.setUser(user);
			answers.add(answer);
		});
		return answerRepository.save(answers);
	}

	private Region persistDefaultRegion()
	{
		final I18NEntity regionKey = persistDefaultI18NEntry();
		final Country country = persistDefaultCountry();
		final Region region = DefaultStaticEntityFactory.createDefaultRegion(country);
		region.setParent(country);
		region.setI18nKey(regionKey.getKey());

		return locatableRepository.save(region);
	}

	@Transactional
	public Region persistDefaultRegion(Country country)
	{
		final Region region = DefaultStaticEntityFactory.createDefaultRegion(DefaultStaticEntityFactory.createDefaultCountry(DefaultStaticEntityFactory.createDefaultContinent()));
		region.setParent(country);
		region.setI18nKey(persistDefaultI18NEntry().getKey());
		return locatableRepository.save(region);
	}

	@Transactional
	public Zip persistDefaultZip(Country country)
	{
		final Region region = persistDefaultRegion(country);
		return persistDefaultZip(region);
	}

	@Transactional
	public Zip persistDefaultZip(Region region)
	{
		final Zip zip = new Zip();
		zip.setZip("zip");
		zip.setLatitude(42.0);
		zip.setLongitude(42.0);
		zip.setRegion(region);
		zip.setParent(region.getParent());
		return locatableRepository.save(zip);
	}

	private Question persistDefaultRegionQuestion()
	{
		final Questionnaire questionnaire = persistDefaultQuestionnaire(RecommendationCategory.PARTNERSHIP);
		final QuestionGroup questionGroup = persistDefaultQuestionGroup(questionnaire);
		return persistDefaultRegionQuestion(questionGroup);
	}

	@Transactional
	public RegionQuestion persistDefaultRegionQuestion(QuestionGroup questionGroup)
	{
		final I18NEntity i18NEntity = persistDefaultI18NEntry();
		return persistDefaultRegionQuestion(questionGroup, i18NEntity);
	}

	@Transactional
	public RegionQuestion persistDefaultRegionQuestion(QuestionGroup questionGroup, I18NEntity i18nEntity)
	{
		final RegionQuestion defaultRegionQuestion = DefaultStaticEntityFactory.createDefaultRegionQuestion(questionGroup, i18nEntity);
		defaultRegionQuestion.setRequirement(Requirement.REQUIRED);
		defaultRegionQuestion.setSortOrder(questionRepository.findMaxSortOrder(null) + 1);
		defaultRegionQuestion.setWithRelocation(true);

		return questionRepository.save(defaultRegionQuestion);
	}

	@Transactional
	public NumberAnswer persistDefaultNumberAnswer(Question question)
	{
		final NumberAnswer numberAnswer = AnswerFactory.get().createNewAnswer(question);
		return answerRepository.save(numberAnswer);
	}

	@Transactional
	public RegionAnswer persistDefaultRegionAnswer(User user)
	{
		final Question regionQuestion = persistDefaultRegionQuestion();
		final RegionAnswer regionAnswer = AnswerFactory.get().createNewAnswer(regionQuestion, user);
		
		return answerRepository.save(regionAnswer);
	}

	@Transactional
	public RegionAnswer persistDefaultRegionAnswerWithProximitySearchRequest(User user, Zip zip)
	{
		final RegionAnswer regionAnswer = persistDefaultRegionAnswer(user);
		regionAnswer.addProximitySearchRequest(DefaultStaticEntityFactory.createDefaultProximitySearchRequest(zip));
		
		return answerRepository.save(regionAnswer);
	}
	
	@Transactional
	public RegionAnswer persistDefaultRegionAnswerWithRegionSearchRequest(User user, Region region)
	{
		final RegionAnswer regionAnswer = persistDefaultRegionAnswer(user);
		regionAnswer.addRegionSearchRequest(DefaultStaticEntityFactory.createDefaultRegionSearchRequest(region));
		
		return answerRepository.save(regionAnswer);
	}

	@Transactional
	public void reset()
	{
		try
		{
			newsletterRepository.deleteAll();
			scammingRepository.deleteAll();
			emailLinkRepository.deleteAll();
			addressRepository.deleteAll();
			clientInformationRepository.deleteAll();
			affiliatePaymentStateRepository.deleteAll();
			recommendationBreakRepository.deleteAll();
			userActivityLogRepository.deleteAll();
			userNewsRepository.deleteAll();
			messageAttachmentRepository.deleteAll();
			mailQueueEntryRepository.deleteAll();
			userMailQueueEntryRepository.deleteAll();
			avatarRepository.deleteAll();
			mediaRepository.deleteAll();
			mediaGalleryRepository.deleteAll();
			userRegistrationStateRepository.deleteAll();
			//			userSettingsRepository.deleteAll();
			userPaymentSettingsRepository.deleteAll();
			externalPaymentRegistrationRepository.deleteAll();
			heidelpayTransactionRepository.deleteAll();
			prepaymentRepository.deleteAll();
			externalPaymentRepository.deleteAll();
			invoiceRepository.deleteAll();
			bankAccountRepository.deleteAll();
			matchRepository.deleteAll();
			relationshipRepository.deleteAll();
			questionMappingRepository.deleteAll();
			matrixRepository.deleteAll();
			activatorRepository.deleteAll();
			adminWorkItemRepository.deleteAll();
			messageRepository.deleteAll();
			subscriptionRepository.deleteAll();
			initialSubscriptionOfferProductRepository.deleteAll();
			answerRepository.deleteAll();
			i18NRepository.deleteAll();
			questionRepository.deleteAll();
			questionGroupRepository.deleteAll();
			questionnaireRepository.deleteAll();
			choiceGroupRepository.deleteAll();
			userRepository.deleteAll();
			fileRepository.deleteAll();
			newsRepository.deleteAll();
			adminRepository.deleteAll();
			locatableRepository.deleteAll();
			undeliverableMailRepository.deleteAll();
			matchStatisticRepository.deleteAll();
		}
		catch (ConstraintViolationException ex)
		{
			LOG.error("Cleaning failed", ex);
		}
	}

	@Transactional
	public Match persistDefaultMatch(User sourceUser, User targetUser, RecommendationCategory recommendationCategory)
	{
		final Match match = new Match(sourceUser, targetUser, recommendationCategory);
		match.setStrictness(Arrays.asList(Strictness.values()).stream().findAny().get());
		match.setNumber(3);

		return matchRepository.save(match);
	}

	@Transactional
	public Country persistDefaultCountry()
	{
		final Continent continent = persistDefaultContinent();
		return persistDefaultCountry(continent);
	}

	@Transactional
	public Country persistDefaultCountry(Continent continent)
	{
		Country country = new Country();
		country.setParent(continent);
		country.setI18nKey(persistDefaultI18NEntry().getKey());

		country = locatableRepository.save(country);

		return country;
	}

	@Transactional
	public Continent persistDefaultContinent()
	{
		Continent continent = new Continent();
		continent.setI18nKey(persistDefaultI18NEntry().getKey());
		continent = locatableRepository.save(continent);
		return continent;
	}

	@Transactional
	public Relationship persistDefaultRelationship(User sourceUser, User targetUser, RecommendationCategory recommendationCategory)
	{
		final Relationship relationship = new Relationship();
		relationship.setSourceUser(sourceUser);
		relationship.setTargetUser(targetUser);
		relationship.setSourceUserId(sourceUser.getId());
		relationship.setTargetUserId(targetUser.getId());
		relationship.getCategories().add(recommendationCategory);
		relationship.setViewed(false);
		relationship.setDeleted(false);
		relationship.setNotified(false);

		return relationshipRepository.save(relationship);
	}

	@Transactional
	public Question persistDefaultDeletedQuestion(QuestionGroup questionGroup)
	{
		TextQuestion deletedQuestion = new TextQuestion();
		deletedQuestion.setMaxLength(12);
		deletedQuestion.setNumberOfLines(12);
		deletedQuestion.setI18nKey(persistDefaultI18NEntry("question").getKey());
		deletedQuestion.setQuestionGroup(questionGroup);
		questionGroup.addQuestion(deletedQuestion);
		deletedQuestion.setOnlyAdminVisible(true);
		deletedQuestion.setDeleted(true);

		return questionRepository.save(deletedQuestion);
	}

	public FileEntity persistDefaultFileEntity()
	{
		final FileEntity fileEntity = new FileEntity();
		fileEntity.setName("test.jpg");
		fileEntity.setType(FileType.IMAGE);

		return fileRepository.save(fileEntity);
	}

	public MediaGallery persistDefaultMediaGallery()
	{
		final MediaGallery mediaGallery = new MediaGallery();
		mediaGallery.setAuthor(persistDefaultUser(UUID.randomUUID().toString()));
		mediaGallery.setName(UUID.randomUUID().toString());
		mediaGallery.setVisibleAffiliation(Affiliation.POSITIVE);
		mediaGallery.setVisibleCategory(RecommendationCategory.FRIENDSHIP);
		
		return mediaGalleryRepository.save(mediaGallery);
	}
	
	public Media persistDefaultMedia()
	{
		final Media media = new Media();
		media.setMediaGallery(persistDefaultMediaGallery());
		media.setFile(persistDefaultFileEntity());

		return mediaRepository.save(media);
	}

	public Avatar persistDefaultAvatar()
	{
		final Avatar avatar = new Avatar();
		avatar.setFile(persistDefaultFileEntity());
		avatar.setCategory(RecommendationCategory.FRIENDSHIP);
		avatar.setUser(persistDefaultUser(UUID.randomUUID().toString()));

		return avatarRepository.save(avatar);
	}

	@Transactional
	public News persistDefaultNews()
	{
		final News news = new News();

		news.setActive(false);
		news.setTitle("title");
		news.setTeaserText("teaser");
		news.setText("body");
		news.setValidFrom(LocalDateTime.now());
		news.setValidTo(LocalDateTime.now().plusDays(365));
		news.setLanguage("DE");

		return newsRepository.save(news);
	}

	@Transactional
	public UserNews persistDefaultUserNews(News news, User user)
	{
		final UserNews defaultUserNews = new UserNews();
		defaultUserNews.setNews(news);
		defaultUserNews.setUser(user);
		defaultUserNews.setHide(false);

		return userNewsRepository.save(defaultUserNews);
	}
	
	@Transactional
	public UserMailQueueEntry persistDefaultUserMailQueueEntry(User user)
	{
		final UserMailQueueEntry mailQueueEntry = new UserMailQueueEntry();
		mailQueueEntry.setAttempts(0);
		mailQueueEntry.setRecipient(user);
		mailQueueEntry.setMailTemplate(UserMailTemplate.REGISTRATION_USER);
		mailQueueEntry.setDeliveryStatus(MailDeliveryStatus.PENDING);
		mailQueueEntry.setUndeliverableMailReason(UndeliverableMailReason.NONE);
		
		return userMailQueueEntryRepository.save(mailQueueEntry);
	}

	@Transactional
	public UserActivityLog persistDefaultUserActivityLog(User user, UserActivity userActivity)
	{
		final UserActivityLog userActivityLog = new UserActivityLog();
		userActivityLog.setUser(user);
		userActivityLog.setUserActivity(userActivity);
		userActivityLog.setCreateDate(LocalDateTime.now());

		return userActivityLogRepository.save(userActivityLog);
	}

	public RecommendationBreak persistDefaultRecommendationBreak(User user)
	{
		final RecommendationBreak recommendationBreak = new RecommendationBreak();
		recommendationBreak.setUser(user);
		recommendationBreak.setCategory(RecommendationCategory.PARTNERSHIP);

		return recommendationBreakRepository.save(recommendationBreak);
	}

	@Transactional
	public AdminWorkItem persistDefaultAdminWorkItem()
	{
		final Message message = persistDefaultMessage(persistDefaultUser(UUID.randomUUID().toString()), persistDefaultUser(UUID.randomUUID().toString()), null);

		final AdminWorkItem adminWorkItem = new AdminWorkItem();
		adminWorkItem.setMessage(message);

		return adminWorkItemRepository.save(adminWorkItem);
	}
	
	@Transactional
	public EmailLink persistDefaultEmailLink()
	{
		final EmailLinkParameter emailLinkParameter = new EmailLinkParameter();
		emailLinkParameter.setParameterType(ParameterType.EMAIL);
		emailLinkParameter.setValue("test");
		
		final EmailLink emailLink = new EmailLink();
		emailLink.setUser(persistDefaultUser(UUID.randomUUID().toString()));
		emailLink.setUniqueToken(UUID.randomUUID().toString());
		emailLink.setContext(EmailLinkContext.UNSUBSCRIBE);
		emailLink.addEmailLinkParameter(emailLinkParameter);
		
		return emailLinkRepository.save(emailLink);
	}
	
	public ClientInformation persistDefaultClientInformation()
	{
		final ClientInformation clientInformation = new ClientInformation();
		
		clientInformation.setUser(persistDefaultUser(UUID.randomUUID().toString()));
		clientInformation.setBrowser(Browser.CHROME);
		clientInformation.setOs(OS.LINUX);
		clientInformation.setBrowserHeight(768);
		clientInformation.setBrowserWidth(1024);
		clientInformation.setBrowserMajorVersion(42);
		clientInformation.setBrowserMinorVersion(42);
		clientInformation.setBrowserOutdated(false);
		clientInformation.setCountry("DE");
		clientInformation.setLanguage("DE");
		clientInformation.setLayout(Device.DESKTOP);
		clientInformation.setScreenWidth(1680);
		clientInformation.setScreenHeight(1050);
		clientInformation.setTouchDevice(false);
		clientInformation.setTimezoneOffset(0);
		
		return clientInformationRepository.save(clientInformation);
	}
	
	public MatchStatistic persistDefaultMatchStatistic()
	{
		final MatchStatistic matchStatistic = new MatchStatistic(MatchingScope.MATCHING, LocalDateTime.now());
		matchStatistic.setEndDate(LocalDateTime.now().plus(1, ChronoUnit.HOURS));
		
		matchStatistic.addDuration(LogArea.DELETE_MATCH, Duration.ofSeconds(4));
		matchStatistic.addDuration(LogArea.DELETE_MATCH, Duration.ofSeconds(4));
		
		matchStatistic.addDuration(MatchArea.AFFINITY, Duration.ofSeconds(5));
		matchStatistic.addDuration(MatchArea.AFFINITY, Duration.ofSeconds(5));
		
		return matchStatisticRepository.save(matchStatistic);
	}
	
	public Scamming persistDefaultScamming()
	{
		final Scamming scamming = new Scamming();
		scamming.setUser(persistDefaultUser(UUID.randomUUID().toString()));
		
		return scammingRepository.save(scamming);
	}
	
	public Newsletter persistDefaultNewsletter()
	{
		final Newsletter newsletter = new Newsletter();
		newsletter.setEmail("example@example.com");
		return newsletterRepository.save(newsletter);
	}
}

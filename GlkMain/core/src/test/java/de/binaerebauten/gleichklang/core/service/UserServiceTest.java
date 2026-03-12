package de.binaerebauten.gleichklang.core.service;

import de.binaerebauten.gleichklang.core.model.mail.UserMailTemplate;
import de.binaerebauten.gleichklang.core.model.payment.PaymentMethod;
import de.binaerebauten.gleichklang.core.model.questionnaire.*;
import de.binaerebauten.gleichklang.core.model.user.*;
import de.binaerebauten.gleichklang.core.repository.*;
import de.binaerebauten.gleichklang.core.repository.message.MessageRepository;
import de.binaerebauten.gleichklang.core.repository.user.UserRegistrationStateRepository;
import de.binaerebauten.gleichklang.core.repository.user.UserRepository;
import de.binaerebauten.gleichklang.core.security.AuthenticationService;
import de.binaerebauten.gleichklang.core.service.mail.MailQueueService;
import de.binaerebauten.gleichklang.core.service.mail.UndeliverableMailService;
import de.binaerebauten.gleichklang.core.service.validator.UniqueValidationException;
import de.binaerebauten.gleichklang.core.utils.DefaultEntityFactory;
import de.binaerebauten.gleichklang.core.utils.DefaultStaticEntityFactory;
import de.binaerebauten.gleichklang.core.utils.PaymentEntityFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.validation.ConstraintViolationException;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.*;

public class UserServiceTest extends BasePersistenceTest
{
	private static final Logger LOG = LoggerFactory.getLogger(UserServiceTest.class);
	
	@Rule
	public final ExpectedException thrown = ExpectedException.none();
	private final String email = DefaultStaticEntityFactory.DEFAULT_EMAIL;
	private final String alias = DefaultStaticEntityFactory.DEFAULT_ALIAS;

	private UserService userService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private DefaultEntityFactory defaultEntityFactory;
	
	@Autowired
	private PaymentEntityFactory paymentEntityFactory;

	@Autowired
	private AnswerService answerService;

	@Autowired
	private QuestionnaireService questionnaireService;

	@Autowired
	private LocatableRepository locatableRepository;

	@Autowired
	private UserRegistrationStateRepository userRegistrationStateRepository;

	@Autowired
	private UserPaymentSettingsRepository userPaymentSettingsRepository;

	@Autowired
	private MessageRepository messageRepository;

	@Autowired
	private I18NRepository i18NRepository;

	@Autowired
	private AnswerRepository answerRepository;

	private String password = "password";

	private User persistentUser;
	
	@Mock
	private AuthenticationService authenticationService;

	@Mock
	private BCryptPasswordEncoder passwordEncoder;
	
	@Mock
	private PaymentRepository paymentRepository;

	@Mock
	private UndeliverableMailService undeliverableMailService;
	
	@Mock
	private MailQueueService mailQueueService;

	@Before
	public void setUp() throws Exception
	{
		MockitoAnnotations.initMocks(this);
		userService = new UserService(authenticationService, mailQueueService,  null,
				userRepository, userPaymentSettingsRepository, paymentRepository, null,
				answerService, questionnaireService, userRegistrationStateRepository,
				null, null, null, locatableRepository, passwordEncoder, "salt", null,messageRepository,i18NRepository, undeliverableMailService);

		when(passwordEncoder.encode(password)).thenReturn(password);
		when(passwordEncoder.matches(password, password)).thenReturn(true);

		preregisterTestUser();
	}

	@After
	public void tearDown() throws Exception
	{
		defaultEntityFactory.reset();
	}

	@Test
	public void testRegisterForTesting() throws Exception
	{
		final String email = "dummy@example.com";
		Questionnaire questionnaire = defaultEntityFactory.persistDefaultQuestionnaire();
		QuestionGroup questionGroup = defaultEntityFactory.persistDefaultQuestionGroup(questionnaire);

		TextQuestion textQuestion = defaultEntityFactory.persistDefaultTextQuestion(questionGroup,
				defaultEntityFactory.persistDefaultI18NEntry("text"));
		NumberQuestion numberQuestion = defaultEntityFactory.persistDefaultNumberQuestion(questionGroup,
				defaultEntityFactory.persistDefaultI18NEntry("number"));
		ChoiceQuestion choiceQuestion = defaultEntityFactory.persistDefaultChoiceQuestion(questionGroup,
				defaultEntityFactory.persistDefaultI18NEntry("choice"));
		List<Question> questions = Arrays.asList(textQuestion, numberQuestion, choiceQuestion);

		User sampleUser = defaultEntityFactory.persistDefaultUser(email, email);
		User user = userService.registerForTesting(sampleUser);
		assertThat(userRepository.findByEmail(email), equalTo(user));

		List<Answer> answers = answerRepository.findAnswersForUser(user, questions);
		assertThat(answers.size(), equalTo(questions.size()));
	}

	@Test
	public void testPreregisterUserWithExistingEmail() throws Exception
	{
		User user = DefaultStaticEntityFactory.createDefaultUser(email, alias);
		thrown.expect(UniqueValidationException.class);
		userService.preregister(user);
	}

	@Test
	public void testPreregister() throws Exception
	{
		defaultEntityFactory.reset();
		User user = DefaultStaticEntityFactory.createDefaultUser(email, alias, RecommendationCategory.FRIENDSHIP);
		UserPaymentSettings userPaymentSettings = new UserPaymentSettings();
		userPaymentSettings.setUser(user);
		userPaymentSettings.setPaymentMethod(PaymentMethod.CREDIT_CARD);

		user = userService.preregister(user, false, userPaymentSettings, null);

		UserRegistrationState userRegistrationState = userRegistrationStateRepository.findByUser(user);
		assertThat(user.getMemberStatus(), equalTo(MemberStatus.REGISTRATION));
		assertThat(userRegistrationState.getRegistrationState(), equalTo(RegistrationState.INITIATED));
		//assertThat(user.getNewEmail(), equalTo(user.getEmail()));
	}

	@Test
	public void testPreregisterUserNonUniqueAlias() throws UniqueValidationException
	{
		User user = DefaultStaticEntityFactory.createDefaultUser(email + "2", alias);

		thrown.expect(UniqueValidationException.class);
		thrown.expectMessage(user.getAlias());

		userService.preregister(user);
	}

	@Test
	public void testPreregisterNonUniqueEmail() throws UniqueValidationException
	{
		User user = DefaultStaticEntityFactory.createDefaultUser(email, alias + "2");
		thrown.expect(UniqueValidationException.class);
		thrown.expectMessage(user.getEmail());

		userService.preregister(user);
	}

	@Test
	public void testSaveUserNullPassword() throws UniqueValidationException
	{
		User user = DefaultStaticEntityFactory.createDefaultUser(email + "2", alias + "2");
		user.setPassword(null);
		thrown.expect(ConstraintViolationException.class);
		userService.preregister(user);
	}

	@Test
	public void testSaveUser_NonUniqueEmail() throws UniqueValidationException
	{
		User user = DefaultStaticEntityFactory.createDefaultUser(email, alias + "2");
		thrown.expect(UniqueValidationException.class);
		thrown.expectMessage(user.getEmail());

		userService.save(user);
	}

	@Test
	public void testSaveUpdatedUser_NonUniqueEmail() throws UniqueValidationException
	{
		User user = defaultEntityFactory.persistDefaultUser(email + "2", alias + "2");

		user.setEmail(email);
		thrown.expect(UniqueValidationException.class);
		thrown.expectMessage(user.getEmail());

		userService.save(user);
	}

	@Test
	public void testSaveUpdatedUser_NonUniqueAlias() throws UniqueValidationException
	{
		User user = defaultEntityFactory.persistDefaultUser(email + "2", alias + "2");

		user.setAlias(alias);
		thrown.expect(UniqueValidationException.class);
		thrown.expectMessage(user.getAlias());

		userService.save(user);
	}

	private void preregisterTestUser() throws UniqueValidationException
	{

		persistentUser = DefaultStaticEntityFactory.createDefaultUser(email, alias);
		persistentUser.setPassword(password);

		userService.preregister(persistentUser);
		assertThat(passwordEncoder.matches(password, persistentUser.getPassword()), is(true));
	}

	@Test
	public void testUnsubscribe() throws UniqueValidationException
	{
		final User user = defaultEntityFactory.persistDefaultUser(email + "2", alias + "2");
		user.getUserSettings().setDisableNewsNotifications(false);
		userRepository.save(user);

		final boolean valueBefore = userRepository.findById(user.getId()).getUserSettings().isDisableNewsNotifications();
		userService.unsubscribe(user, UserMailTemplate.NEWS_FROM_GLEICHKLANG);
		final boolean valueAfter = userRepository.findById(user.getId()).getUserSettings().isDisableNewsNotifications();

		assertThat(valueBefore, equalTo(false));
		assertThat(valueAfter, equalTo(true));
	}

	@Test
	public void testChangeMail()
	{
		final String newMail = "newMail@example.com";

		final User beforeUser = userRepository.findById(persistentUser.getId());

		assertThat(beforeUser.getEmail(), not(newMail));
		assertThat(beforeUser.getConfirmationIp(), nullValue());
		assertThat(beforeUser.isEmailConfirmed(), is(false));

		userService.changeMail(null, persistentUser, newMail);

		final User afterUser = userRepository.findById(persistentUser.getId());

		assertThat(afterUser.getEmail(), is(newMail));
		assertThat(afterUser.getConfirmationIp(), notNullValue());
		assertThat(afterUser.isEmailConfirmed(), is(true));
	}

	@Test
	public void testSendChangeMail()
	{
		final String oldMail = persistentUser.getEmail();
		final String newMail = "newmail@example.com";

		setUser( true);
		try {
			userService.sendChangeMail(persistentUser, oldMail);
			checkSendChangeMail(oldMail, false);

			setUser(false);
			userService.sendChangeMail(persistentUser, oldMail);
			checkSendChangeMail(oldMail, true);

			setUser(true);
			userService.sendChangeMail(persistentUser, newMail);
			checkSendChangeMail(newMail, true);

			setUser(false);
			userService.sendChangeMail(persistentUser, newMail);
			checkSendChangeMail(newMail, true);
		}
		catch(Exception ex){}
	}
	
	private void setUser(boolean emailConfirmed)
	{
		persistentUser.setNewEmail(null);
		persistentUser.setEmailConfirmed(emailConfirmed);
		userRepository.save(persistentUser);
	}
	
	private void checkSendChangeMail(String email, boolean mailSent)
	{
		final String actualMail = userRepository.findById(persistentUser.getId()).getNewEmail();
		
		if(mailSent)
		{
			assertThat(actualMail, equalTo(email));
			verify(mailQueueService).enqueue(persistentUser, UserMailTemplate.MAIL_CHANGE_VERIFICATION);
		}
		else
		{
			assertThat(actualMail, nullValue());
			verifyZeroInteractions(mailQueueService);
		}
		
		reset(mailQueueService);
	}
}

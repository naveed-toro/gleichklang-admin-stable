package de.binaerebauten.gleichklang.core.service.mail;

import com.google.common.collect.ImmutableMap;
import de.binaerebauten.gleichklang.core.config.VelocityServiceTestConfig;
import de.binaerebauten.gleichklang.core.model.I18NEntity;
import de.binaerebauten.gleichklang.core.model.I18NEntity.Language;
import de.binaerebauten.gleichklang.core.model.mail.MailDeliveryStatus;
import de.binaerebauten.gleichklang.core.model.mail.MailQueueEntry;
import de.binaerebauten.gleichklang.core.model.mail.UserMailQueueEntry;
import de.binaerebauten.gleichklang.core.model.mail.UserMailTemplate;
import de.binaerebauten.gleichklang.core.model.payment.InitialSubscriptionOffer;
import de.binaerebauten.gleichklang.core.model.user.Admin;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.model.user.UserSettings;
import de.binaerebauten.gleichklang.core.repository.*;
import de.binaerebauten.gleichklang.core.repository.mail.MailQueueEntryRepository;
import de.binaerebauten.gleichklang.core.repository.mail.UserMailQueueEntryRepository;
import de.binaerebauten.gleichklang.core.repository.user.UserRepository;
import de.binaerebauten.gleichklang.core.service.UserService;
import de.binaerebauten.gleichklang.core.service.template.TemplateEngineService;
import de.binaerebauten.gleichklang.core.utils.DefaultEntityFactory;
import de.binaerebauten.gleichklang.core.utils.MailReminder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link MailSendService}.
 */
@ContextConfiguration(classes = VelocityServiceTestConfig.class)
public class MailSendServiceTest extends BasePersistenceTest
{
	@Mock
	private UserRepository userRepository;
	
	@Mock
	private MailQueueService mailQueueService;
	
	private JavaMailSender javaMailSender;
	
	@Autowired
	@Spy
	private UserService userService;
	
	@Autowired
	private MailQueueEntryRepository mailQueueEntryRepository;
	
	@Autowired
	private UserMailQueueEntryRepository userMailQueueEntryRepository;
	
	@Autowired
	private UserMailTemplateService userMailTemplateService;
	
	@Autowired
	private RelationshipRepository relationshipRepository;

	@Autowired
	private UserNewsRepository userNewsRepository;
	
	@Autowired
	private TransactionTemplate transactionTemplate;
	
	@Autowired
	private TemplateEngineService templateEngineService;
	
	@Autowired
	private DefaultEntityFactory defaultEntityFactory;
	
	@InjectMocks
	private MailSendService mailSendService;
	
	private User user;
	
	
	@Before
	public void setup()
	{
		final Environment environment = Mockito.mock(Environment.class);
		final UndeliverableMailService undeliverableMailService = Mockito.mock(UndeliverableMailService.class);
		final MailReminder<MailQueueEntry> mailReminder = Mockito.mock(MailReminder.class);
		javaMailSender = Mockito.mock(JavaMailSender.class);
		
		when(environment.getProperty("email.sender")).thenReturn("support@binaere-bauten.de");
		when(environment.getProperty("email.max_attempts", int.class)).thenReturn(5);
		when(environment.getProperty("email.white_list.enabled", boolean.class)).thenReturn(false);
		
		
		mailSendService = new MailSendService(relationshipRepository, mailQueueEntryRepository,
				userNewsRepository, mailQueueService, userMailTemplateService,
				undeliverableMailService, javaMailSender, mailReminder, transactionTemplate, environment);
		
		MockitoAnnotations.initMocks(this);
		
		user = defaultEntityFactory.persistDefaultUser("test@binaere-bauten.de", "test");
	}
	
	@After
	public void teardown()
	{
		defaultEntityFactory.reset();
	}
	
	@Test
	public void testProcessMailQueue() throws Exception
	{
		UserMailQueueEntry mailQueueEntry = defaultEntityFactory.persistDefaultUserMailQueueEntry(user);

		mailSendService.processPendingMailQueue();
		
		mailQueueEntry = userMailQueueEntryRepository.findOne(mailQueueEntry.getId());
		assertThat(mailQueueEntry.getDeliveryStatus(), is(MailDeliveryStatus.SENT));
	}
	
	@Test
	public void testProcessNewMatchMailQueue() throws Exception
	{
		UserMailQueueEntry mailQueueEntry = defaultEntityFactory.persistDefaultUserMailQueueEntry(user);
		defaultEntityFactory.persistDefaultRelationship(user, user, RecommendationCategory.PARTNERSHIP);

		mailSendService.processPendingMailQueue();

		mailQueueEntry = userMailQueueEntryRepository.findOne(mailQueueEntry.getId());
		assertThat(mailQueueEntry.getDeliveryStatus(), is(MailDeliveryStatus.SENT));
	}
	
	@Test
	public void testSendSystemEmail() throws Exception
	{
		Map<String, Object> variables = ImmutableMap.of("social_action_code", InitialSubscriptionOffer.SOCIAL_CODE);
		MailTemplateInstance mailTemplateInstance = MailTemplateInstance.create(templateEngineService, variables,
				UserMailTemplate.ADMIN_UNKNOWN_EXTERNAL_PAYMENT, I18NEntity.Language.DE);
		mailSendService.sendSystemEmail(mailTemplateInstance);
		
		verify(javaMailSender).send(Matchers.any(MimeMessagePreparator.class));
	}
	
	@Test
	public void testSendEmail2User() throws Exception
	{
		MailTemplateInstance mailTemplateInstance = createMailTemplateInstance(UserMailTemplate.REGISTRATION_USER);
		mailSendService.sendEmail(user, mailTemplateInstance);

		verify(javaMailSender).send(Matchers.any(MimeMessagePreparator.class));
	}
	
	@Test
	public void testSendEmail2Admin() throws Exception
	{
		final Admin admin = defaultEntityFactory.persistDefaultAdmin("admin_test@binaere-bauten.de");
		final MailTemplateInstance mailTemplateInstance = userMailTemplateService.createAdminMailTemplateInstance(UserMailTemplate.ADMIN_PASSWORD_RESET, admin, "password");
		
		mailSendService.sendEmail(admin, mailTemplateInstance);
		
		verify(javaMailSender).send(Matchers.any(MimeMessagePreparator.class));
	}
	
	@Test
	public void testSendEmailTurnedOff() throws Exception
	{
		UserSettings userSettings = new UserSettings();
		userSettings.setDisableNewsNotifications(true);
		user.setUserSettings(userSettings);

		when(userRepository.findByEmail(user.getEmail())).thenReturn(user);
		
		MailTemplateInstance mailTemplateInstance = createMailTemplateInstance(UserMailTemplate.NEWS_FROM_GLEICHKLANG);
		mailSendService.sendEmail(user, mailTemplateInstance);
		
		verify(javaMailSender, times(0)).send(Matchers.any(MimeMessagePreparator.class));
	}
	
	@Test
	public void testSendEmailNotConfirmed() throws Exception
	{
		user.setNewEmail("newmail@example.com"); //necessary for MAIL_CHANGE_VERIFICATION
		user.setEmailConfirmed(false);
		userRepository.save(user);
		
		mailSendService.sendEmail(user, createMailTemplateInstance(UserMailTemplate.NEW_MATCH));
		
		verifyZeroInteractions(javaMailSender);
		reset(javaMailSender);
		
		// folgende Nachrichten sollen trotzdem versendet werden
		mailSendService.sendEmail(user, createMailTemplateInstance(UserMailTemplate.SOCIAL_PP_USER));
		mailSendService.sendEmail(user, createMailTemplateInstance(UserMailTemplate.REGISTRATION_USER));
		mailSendService.sendEmail(user, createMailTemplateInstance(UserMailTemplate.MAIL_CHANGE_VERIFICATION));
		
		verify(javaMailSender, times(3)).send(Matchers.any(MimeMessagePreparator.class));
		reset(javaMailSender);
		
		// Gegentest
		user.setEmailConfirmed(true);
		userRepository.save(user);
		
		mailSendService.sendEmail(user, createMailTemplateInstance(UserMailTemplate.NEW_MATCH));
		
		verify(javaMailSender).send(Matchers.any(MimeMessagePreparator.class));
		reset(javaMailSender);
	}
	
	@Test
	public void testSendEmailNotificationDisabled() throws Exception
	{
		user.setEmailConfirmed(true);
		user.getUserSettings().setDisableRecommendationNotifications(false);
		user.getUserSettings().setDisableCipherMessageNotifications(false);
		user.getUserSettings().setDisablePositiveRankingNotifications(false);
		user.getUserSettings().setDisableNewsNotifications(false);
		user.getUserSettings().setDisableFootprintNotifications(false);
		user.getUserSettings().setEnableMarketingNotifications(true);
		userRepository.save(user);
		
		mailSendService.sendEmail(user, createMailTemplateInstance(UserMailTemplate.NEW_MATCH));
		mailSendService.sendEmail(user, createMailTemplateInstance(UserMailTemplate.NEW_BOXNUMBER_CONTACT));
		mailSendService.sendEmail(user, createMailTemplateInstance(UserMailTemplate.NEW_MATCH_POSITIVE));
		mailSendService.sendEmail(user, createMailTemplateInstance(UserMailTemplate.NEWS_FROM_GLEICHKLANG));
		mailSendService.sendEmail(user, createMailTemplateInstance(UserMailTemplate.NEW_FOOTPRINT));
		mailSendService.sendEmail(user, createMailTemplateInstance(UserMailTemplate.MISSING_PAYMENT_REMINDER));
		mailSendService.sendEmail(user, createMailTemplateInstance(UserMailTemplate.MISSING_QUESTIONAIRE_REMINDER));
		
		verify(javaMailSender, times(7)).send(Matchers.any(MimeMessagePreparator.class));
		reset(javaMailSender);
		
		user.getUserSettings().setDisableRecommendationNotifications(true);
		user.getUserSettings().setDisableCipherMessageNotifications(true);
		user.getUserSettings().setDisablePositiveRankingNotifications(true);
		user.getUserSettings().setDisableNewsNotifications(true);
		user.getUserSettings().setDisableFootprintNotifications(true);
		user.getUserSettings().setEnableMarketingNotifications(false);
		userRepository.save(user);
		
		mailSendService.sendEmail(user, createMailTemplateInstance(UserMailTemplate.NEW_MATCH));
		mailSendService.sendEmail(user, createMailTemplateInstance(UserMailTemplate.NEW_BOXNUMBER_CONTACT));
		mailSendService.sendEmail(user, createMailTemplateInstance(UserMailTemplate.NEW_MATCH_POSITIVE));
		mailSendService.sendEmail(user, createMailTemplateInstance(UserMailTemplate.NEWS_FROM_GLEICHKLANG));
		mailSendService.sendEmail(user, createMailTemplateInstance(UserMailTemplate.NEW_FOOTPRINT));
		mailSendService.sendEmail(user, createMailTemplateInstance(UserMailTemplate.MISSING_PAYMENT_REMINDER));
		mailSendService.sendEmail(user, createMailTemplateInstance(UserMailTemplate.MISSING_QUESTIONAIRE_REMINDER));
		
		verifyZeroInteractions(javaMailSender);
	}
	
	private MailTemplateInstance createMailTemplateInstance(UserMailTemplate mailTemplate)
	{
		return MailTemplateInstance.create(templateEngineService, user, mailTemplate, Language.DE);
	}
}

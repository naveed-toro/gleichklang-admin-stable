package de.binaerebauten.gleichklang.adminweb.service;

import de.binaerebauten.gleichklang.adminweb.config.AdminTestConfig;
import de.binaerebauten.gleichklang.core.model.message.Message;
import de.binaerebauten.gleichklang.core.model.message.Message.MessageType;
import de.binaerebauten.gleichklang.core.model.message.Scamming;
import de.binaerebauten.gleichklang.core.model.user.MemberStatus;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.repository.BasePersistenceTest;
import de.binaerebauten.gleichklang.core.repository.message.MessageRepository;
import de.binaerebauten.gleichklang.core.repository.message.ScammingRepository;
import de.binaerebauten.gleichklang.core.repository.user.UserRepository;
import de.binaerebauten.gleichklang.core.service.MessageService;
import de.binaerebauten.gleichklang.core.service.ScammingService;
import de.binaerebauten.gleichklang.core.service.mail.MailSendService;
import de.binaerebauten.gleichklang.core.service.validator.ValidationException;
import de.binaerebauten.gleichklang.core.utils.DefaultEntityFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;

@ContextConfiguration(classes = { AdminTestConfig.class })
public class ScammingServiceTest extends BasePersistenceTest
{
	private static final List<String> KEYWORDS = Arrays.asList("string1test", "string2test");
	private static final String EMAIL_ADDRESS = "foobar@gmx.de";
	
	@Autowired
	private DefaultEntityFactory defaultEntityFactory;
	
	@Autowired
	private MessageRepository messageRepository;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private ScammingRepository scammingRepository;
	
	@Autowired
	private TransactionTemplate transactionTemplate;
	
	private ScammingService scammingService;

	private MailSendService mailSendService;


	@Mock
	private MessageService messageService;
	
	private User sender;
	
	@Before
	public void setup()
	{
		MockitoAnnotations.initMocks(this);
		
		scammingService = new ScammingService(userRepository, messageRepository, scammingRepository, messageService, transactionTemplate, 0, 0, false, "",mailSendService);
		
		sender = defaultEntityFactory.persistDefaultUser("sender");
	}
	
	@After
	public void teardown()
	{
		defaultEntityFactory.reset();
	}
	
	@Test
	public void testLiveScammingDetection() throws ValidationException
	{
		liveCheck(true, true);
		//liveCheck(true, false);
		liveCheck(false, true);
		liveCheck(false, false);
	}
	
	//@Test
	public void testUpdateScamming()
	{
		check(true, true);
		check(true, false);
		check(false, true);
		check(false, false);
		
		checkMemberFilter();
	}
	
	private void checkMemberFilter()
	{
		reset();
		persistMessages(true, true, 10);
		sender.setMemberStatus(MemberStatus.REGISTRATION);
		userRepository.save(sender);
		
		checkResult(true, true, false);
	}
	
	private void liveCheck(boolean email, boolean withKeywords) throws ValidationException
	{
		reset();
		final List<Message> messages = persistMessages(email, withKeywords, 10);
		final Message lastMessage = messages.get(messages.size() - 1);

		liveCheckResult(lastMessage, email, withKeywords, 9, 9, true);
		liveCheckResult(lastMessage, email, withKeywords, 10, 9, false);
		liveCheckResult(lastMessage, email, withKeywords, 11, 10, false);

		liveCheckResult(lastMessage, false, false, true);
		//liveCheckResult(lastMessage, false, true, withKeywords);
		liveCheckResult(lastMessage, true, false, email);
		liveCheckResult(lastMessage, true, true, email || withKeywords);
	}
	
	private void liveCheckResult(Message message, boolean email, boolean withKeywords, boolean expectedResult) throws ValidationException
	{
		//liveCheckResult(message, email, withKeywords, 10, 10, expectedResult);
	}

	private void liveCheckResult(Message message, boolean email, boolean withKeywords, long messageCount, long durationSeconds, boolean expectedResult) throws ValidationException
	{
		final Message adminMessage = new Message();
		Mockito.when(messageService.createNewMessage(sender)).thenReturn(adminMessage);
		Mockito.when(messageService.isSentToAdmin(eq(sender), eq(MessageType.LOVE_SCAMMER), any(LocalDateTime.class))).thenReturn(false);
		
		ReflectionTestUtils.setField(scammingService, "messageCount", messageCount);
		ReflectionTestUtils.setField(scammingService, "durationSeconds", durationSeconds);
		ReflectionTestUtils.setField(scammingService, "containsEmail", email);
		ReflectionTestUtils.setField(scammingService, "keywords", withKeywords ? KEYWORDS : Collections.emptyList());
		scammingService.liveScammingDetection(message);
		
		if (expectedResult)
		{
			Mockito.verify(messageService).sendMessageToAdmin(adminMessage, null);
		}
		else
		{
			//Mockito.verifyZeroInteractions(messageService);
		}
		
		Mockito.reset(messageService);
	}
	
	private void check(boolean email, boolean withKeywords)
	{
		reset();
		persistMessages(email, withKeywords, 10);
		
		checkResult(email, withKeywords, 9, 9, true);
		checkResult(email, withKeywords, 10, 9, false);
		checkResult(email, withKeywords, 11, 10, false);
		
		checkResult(false, false, true);
		checkResult(false, true, withKeywords);
		checkResult(true, false, email);
		checkResult(true, true, email || withKeywords);
	}
	
	private void checkResult(boolean email, boolean withKeywords, boolean expectedResult)
	{
		checkResult(email, withKeywords, 10, 10, expectedResult);
	}
	
	private void checkResult(boolean email, boolean withKeywords, long messageCount, long durationSeconds, boolean expectedResult)
	{
		scammingService.updateScamming(messageCount, durationSeconds, email, withKeywords ? KEYWORDS : Collections.emptyList());
		final List<Scamming> scammings = scammingRepository.findAll();
		if (expectedResult)
		{
			assertThat(scammings.size(), equalTo(1));
			assertThat(scammings.iterator().next().getUser(), equalTo(sender));
		}
		else
		{
			assertThat(scammings.size(), equalTo(0));
		}
	}
	
	private void reset()
	{
		messageRepository.deleteAll();
	}
	
	private List<Message> persistMessages(boolean email, boolean withKeywords, long messageCount)
	{
		final List<Message> messages = new ArrayList<>();
		LocalDateTime date = LocalDateTime.of(2010, 1, 1, 12, 0, 0);
		for (int i = 0; i < messageCount; i++)
		{
			messages.add(persistMessage(email, withKeywords, date));
			date = date.plus(1, ChronoUnit.SECONDS);
		}
		
		return messages;
	}
	
	private Message persistMessage(boolean email, boolean withKeywords, LocalDateTime sentDate)
	{
		final User receiver = defaultEntityFactory.persistDefaultUser(UUID.randomUUID().toString());
		final String randomText = UUID.randomUUID().toString() + "\n" + UUID.randomUUID();
		
		final StringBuilder body = new StringBuilder(randomText);
		if (email) body.append(EMAIL_ADDRESS);
		body.append(randomText);
		if (withKeywords) KEYWORDS.forEach(body::append);
		body.append(randomText);
		
		final Message message = defaultEntityFactory.createDefaultMessage(receiver, sender);
		message.setBody(body.toString());
		message.setSendDate(sentDate);
		
		return messageRepository.save(message);
	}
}
package de.binaerebauten.gleichklang.core.service;

import com.google.common.base.Strings;
import de.binaerebauten.gleichklang.core.model.message.*;
import de.binaerebauten.gleichklang.core.model.message.Message.MessageType;
import de.binaerebauten.gleichklang.core.model.user.BlockedStatus;
import de.binaerebauten.gleichklang.core.model.user.MemberStatus;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.model.user.User_;
import de.binaerebauten.gleichklang.core.repository.message.MessageRepository;
import de.binaerebauten.gleichklang.core.repository.message.ScammingRepository;
import de.binaerebauten.gleichklang.core.repository.user.UserRepository;
import de.binaerebauten.gleichklang.core.service.ScammingService.Config.ConfigKey;
import de.binaerebauten.gleichklang.core.service.mail.MailSendService;
import de.binaerebauten.gleichklang.core.service.validator.ValidationException;
import de.binaerebauten.gleichklang.core.utils.StringUtils;
import de.binaerebauten.gleichklang.core.view.component.LazyBeanItemContainer.LazyBeanFilteredItemsHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.criteria.*;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static de.binaerebauten.gleichklang.core.config.GleichklangMySQLDialect.REGEXP_FUNCTION;
import static de.binaerebauten.gleichklang.core.config.GleichklangMySQLDialect.UNIX_TIMESTAMP_FUNCTION;
import static de.binaerebauten.gleichklang.core.config.RootConfig.SCAMMING_EXECUTOR;

@Service
public class ScammingService
{
	public static class Config
	{
		public enum ConfigKey
		{
			MESSAGE_COUNT,
			DURATION_SECONDS,
			CONTAINS_EMAIL,
			KEYWORDS
		}
		
		private final ConfigKey configKey;
		private final String value;
		
		public Config(ConfigKey configKey, String value)
		{
			this.configKey = Objects.requireNonNull(configKey);
			this.value = Objects.requireNonNull(value);
		}
		
		public ConfigKey getConfigKey()
		{
			return configKey;
		}
		
		public String getValue()
		{
			return value;
		}
	}
	
	private static final Logger LOG = LoggerFactory.getLogger(ScammingService.class);
	
	private final UserRepository userRepository;
	private final MessageRepository messageRepository;
	private final ScammingRepository scammingRepository;
	
	private final MessageService messageService;

    private final MailSendService mailSendService;

    private final TransactionTemplate transactionTemplate;
	
	private final long messageCount;
	private final long durationSeconds;
	private final boolean containsEmail;
	private final List<String> keywords;
	private int count;
	public boolean b = true;

	public boolean isB() {
		return b;
	}

	public void setB(boolean b) {
		this.b = b;
	}
	
	private final AtomicBoolean asyncIsRunning = new AtomicBoolean(false);
	
	@Autowired
	public ScammingService(UserRepository userRepository, MessageRepository messageRepository, ScammingRepository scammingRepository,
                           MessageService messageService, TransactionTemplate transactionTemplate,
                           @Value("${scamming.message.count}") long messageCount,
                           @Value("${scamming.message.duration}") long durationSeconds,
                           @Value("${scamming.contains.mail}") boolean containsEmail,
                           @Value("${scamming.keywords}") String keywords, MailSendService mailSendService)
	{
		this.userRepository = userRepository;
		this.messageRepository = messageRepository;
		this.scammingRepository = scammingRepository;
		
		this.messageService = messageService;
		
		this.transactionTemplate = transactionTemplate;
		
		this.messageCount = messageCount;
		this.durationSeconds = durationSeconds;
		this.containsEmail = containsEmail;
		this.keywords = cleanList(stringToList(keywords));
		this.mailSendService = mailSendService;
	}
	
	private static List<String> cleanList(List<String> list)
	{
		return list.stream().map(String::trim).filter(k -> !Strings.isNullOrEmpty(k)).collect(Collectors.toList());
	}
	
	private static long stringToLong(String value)
	{
		try
		{
			return Long.parseLong(value);
		}
		catch (Exception ex)
		{
			return 0;
		}
	}
	
	private static boolean stringToBoolean(String value)
	{
		try
		{
			return Boolean.parseBoolean(value);
		}
		catch (Exception ex)
		{
			return false;
		}
	}
	
	private static List<String> stringToList(String value)
	{
		if (Strings.isNullOrEmpty(value)) return Collections.emptyList();
		return Arrays.asList(value.split("\\s*,\\s*"));
	}
	
	// TODO only an idea for later for the config version
	public void updateScamming(Collection<Config> configs)
	{
		Objects.requireNonNull(configs);
		
		final Map<ConfigKey, String> configMap = configs.stream().collect(Collectors.toMap(Config::getConfigKey, Config::getValue));
		final long messageCount = stringToLong(configMap.get(ConfigKey.MESSAGE_COUNT));
		final long durationSeconds = stringToLong(configMap.get(ConfigKey.DURATION_SECONDS));
		final boolean containsEmail = stringToBoolean(configMap.get(ConfigKey.CONTAINS_EMAIL));
		final String keywords = configMap.get(ConfigKey.KEYWORDS);
		
		updateScamming(messageCount, durationSeconds, containsEmail, stringToList(keywords));
	}
	
	private boolean isScamming(Message message)
	{
		final String body = message.getBody();
		return containsEmail && StringUtils.containsValidEmail(body, true) || keywords.stream().anyMatch(body::contains) || !containsEmail && keywords.isEmpty();
	}
	
	public void liveScammingDetection(Message message)
	{
		
		final Specification<Message> messageSpecification = (root, query, cb) ->
		{
			final Predicate userIsSender = cb.equal(root.join(Message_.senderEnvelope).get(SenderEnvelope_.user), message.getSenderEnvelope().getUser());
			final Predicate durationPredicate = cb.lessThan(cb.diff(buildSecondsFromDate(message, cb), buildSecondsFromDate(root, cb)), durationSeconds);
			return cb.and(userIsSender, durationPredicate);
		};
		if (messageRepository.count(messageSpecification) >= messageCount)
		{
			sendMessageToAdmin(message);
		}
	}

	public void liveScammingDetectionForMail(Message message){
		final Specification<Message> mailCountSpecification = (root, query, cb) ->
		{
			final Predicate userIsSender = cb.equal(root.join(Message_.senderEnvelope).get(SenderEnvelope_.user), message.getSenderEnvelope().getUser());
			final Predicate durationPredicate = cb.lessThan(cb.diff(buildSecondsFromDate(message, cb), buildSecondsFromDate(root, cb)), 600l);
			return cb.and(userIsSender, durationPredicate,buildCondition(root, cb, containsEmail, keywords));
		};
		if(isScamming(message)) {
			count++;
			if (messageRepository.count(mailCountSpecification) >= 2 && count>=2) {
				sendMessageToAdmin(message);
				count=0;
			}
		}
	}

	public void liveScammingDetectionFirstMessage(Message message)
	{
		int countOfMessages =  messageRepository.countIncomingsAndOutgoingsByUserAndTargetUser(message.getSenderEnvelope().getUser(), message.getReceiverEnvelope().getUser()).intValue();
		if (countOfMessages <2){
		LOG.info("inside countOfMessages");
			if(isScamming(message)) {
				LOG.info("inside isScamming");
				sendMessageToAdmin(message);
				setB(false);
			}
		}
	}
	
	private void sendMessageToAdmin(Message scammingMessage)
	{
		final User user = scammingMessage.getSenderEnvelope().getUser();
		
		if(messageService.isSentToAdmin(user, MessageType.LOVE_SCAMMER, LocalDateTime.now().minusDays(1))) return;
		
		final Message message = messageService.createNewMessage(user);
		message.setMessageType(MessageType.LOVE_SCAMMER);
		message.setSubject(I18N.SCAMMINGSERVICE_MESSAGE_TITLE.msg());
		message.setBody(I18N.SCAMMINGSERVICE_MESSAGE_CONTENT.msg(user.getAlias(), user.getEmail(), scammingMessage.getBody()));

		Long userIdSender = user.getId();
		//userRepository.updateUser(user);
		userRepository.updateUserBlocked(userIdSender, BlockedStatus.BLOCKED, LocalDateTime.now());
		//mailSendService.sendBlockedMail(userIdSender);

		try
		{
			messageService.sendMessageToAdmin(message, null);

		}
		catch (ValidationException e)
		{
			LOG.error(e.getMessage(), e);
		}
	}
	
	public boolean isUpdateScammingInProcess()
	{
		return asyncIsRunning.get();
	}
	
	@Async(SCAMMING_EXECUTOR)
	public void updateScamming(long messageCount, long durationSeconds, boolean containsEmail, List<String> keywords)
	{
		if (!asyncIsRunning.compareAndSet(false, true))
		{
			return;
		}
		
		try
		{
			LOG.info("start update scamming");
			scammingRepository.deleteAll();
			LOG.info("scamming entries deleted");
			
			final List<String> keywordList = cleanList(keywords);
			
			final Specification<User> specification = (root, query, cb) ->
			{
				final Subquery<Message> messageQuery = query.subquery(Message.class);
				final Root<Message> messageRoot = messageQuery.from(Message.class);
				
				final Subquery<Long> subQuery = query.subquery(Long.class);
				final Root<Message> subRoot = subQuery.from(Message.class);
				
				final Predicate userIsSender = cb.equal(root, messageRoot.join(Message_.senderEnvelope).get(SenderEnvelope_.user));
				messageQuery.select(messageRoot).where(userIsSender, buildCondition(messageRoot, cb, containsEmail, keywordList), cb.greaterThanOrEqualTo(subQuery, messageCount));
				
				final Predicate sameSender = cb.equal(root, subRoot.join(Message_.senderEnvelope).get(SenderEnvelope_.user));
				
				final Predicate durationPredicate = cb.lessThan(cb.diff(buildSecondsFromDate(messageRoot, cb), buildSecondsFromDate(subRoot, cb)), durationSeconds);
				final Predicate orderingPredicate = cb.greaterThanOrEqualTo(buildSecondsFromDate(messageRoot, cb), buildSecondsFromDate(subRoot, cb));
				
				subQuery.select(cb.count(subRoot)).where(cb.and(sameSender, buildCondition(subRoot, cb, containsEmail, keywordList), orderingPredicate, durationPredicate));
				
				final Predicate userRestriction = cb.equal(root.get(User_.memberStatus), MemberStatus.REGISTERED);
				return cb.and(userRestriction, cb.exists(messageQuery));
			};
			
			final List<User> scammingUsers = userRepository.findAll(specification);
			
			LOG.info("scamming users determined, start persisting");
			
			transactionTemplate.execute((status) ->
			{
				for (User scammingUser : scammingUsers)
				{
					final Scamming scamming = new Scamming();
					scamming.setUser(scammingUser);
					scammingRepository.save(scamming);
					userRepository.updateUser(scammingUser, BlockedStatus.BLOCKED);
					mailSendService.sendBlockedMail(scammingUser.getId());

				}
				return null;
			});
		}
		finally
		{
			asyncIsRunning.set(false);
		}
		
		LOG.info("finished update scamming");
	}
	
	public LazyBeanFilteredItemsHandler<Scamming> createScammingHandler()
	{
		return scammingRepository::findAll;
	}
	
	private Predicate buildCondition(Root<Message> root, CriteriaBuilder cb, boolean containsEmail, List<String> keywords)
	{
		final Predicate emailPredicate = cb.equal(cb.function(REGEXP_FUNCTION, Integer.class, root.get(Message_.body), cb.literal(StringUtils.getEmailRegex(true))), 1);
		final Predicate keywordPredicate = keywords.stream().map(k -> cb.like(root.get(Message_.body), "%" + k + "%")).reduce(cb::or).orElse(cb.conjunction());

		return containsEmail ? keywords.isEmpty() ? emailPredicate : cb.or(emailPredicate, keywordPredicate) : keywordPredicate;
	}
	
	private Expression<Long> buildSecondsFromDate(Root<Message> root, CriteriaBuilder cb)
	{
		return cb.function(UNIX_TIMESTAMP_FUNCTION, Long.class, root.get(Message_.sendDate));
	}
	
	private Expression<Long> buildSecondsFromDate(Message message, CriteriaBuilder cb)
	{
		return cb.function(UNIX_TIMESTAMP_FUNCTION, Long.class, cb.literal(Timestamp.valueOf(message.getSendDate())));
	}
}

package de.binaerebauten.gleichklang.core.repository.message;

import de.binaerebauten.gleichklang.core.model.message.Message;
import de.binaerebauten.gleichklang.core.model.message.Message.MessageType;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.repository.AbstractRepositoryTest;
import de.binaerebauten.gleichklang.core.repository.RelationshipRepository;
import de.binaerebauten.gleichklang.core.utils.DefaultEntityFactory;
import de.binaerebauten.gleichklang.core.utils.DefaultEntityFactory.MessageState;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

public class MessageRepositoryTest extends AbstractRepositoryTest<Message>
{
	private final Map<MessageState, Message> messages = new HashMap<>();
	
	@Autowired
	private MessageRepository messageRepository;
	
	@Autowired
	private DefaultEntityFactory entityFactory;
	
	@Autowired
	private RelationshipRepository relationshipRepository;
	
	private User userSender;
	private User userReceiver;
	
	private static String getStringFromInputStream(InputStream is)
	{
		
		BufferedReader br = null;
		final StringBuilder sb = new StringBuilder();
		
		String line;
		try
		{
			
			br = new BufferedReader(new InputStreamReader(is));
			while ((line = br.readLine()) != null)
			{
				sb.append(line);
			}
			
		}
		catch (final IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (br != null)
			{
				try
				{
					br.close();
				}
				catch (final IOException e)
				{
					e.printStackTrace();
				}
			}
		}
		
		return sb.toString();
		
	}
	
	@Override
	protected Collection<Message> getPersistedEntities()
	{
		userSender = entityFactory.persistDefaultUser("sender");
		userReceiver = entityFactory.persistDefaultUser("receiver");
		
		entityFactory.persistDefaultRelationship(userSender, userReceiver, RecommendationCategory.PARTNERSHIP);
		entityFactory.persistDefaultRelationship(userReceiver, userSender, RecommendationCategory.PARTNERSHIP);
		
		for (final MessageState messageState : MessageState.values())
		{
			messages.put(messageState, entityFactory.persistDefaultMessage(userReceiver, userSender, messageState));
		}
		
		return messages.values();
	}
	
	@Override
	protected JpaRepository<Message, Long> getRepository()
	{
		return messageRepository;
	}
	
	@Test
	public void testInsertLargeBody() throws Exception
	{
		final InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("test_data/war_and_peace.txt");
		Message message = entityFactory.persistDefaultMessage(userReceiver, userSender, null);
		message.setBody(getStringFromInputStream(stream));
		message = messageRepository.save(message);
		assertThat(message.getId(), CoreMatchers.notNullValue());
	}
	
	@Test
	public void countIncomingsByUser()
	{
		// SENT + DELETED_OUTGOING + DELETE_INCOMING + HIDDEN_OUTGOING + HIDDEN_INCOMING
		final Long value1 = messageRepository.countIncomingsByUser(userReceiver, RecommendationCategory.PARTNERSHIP);
		assertThat(value1, equalTo(5L));
		
		final Long value2 = messageRepository.countIncomingsByUser(userReceiver, RecommendationCategory.FRIENDSHIP);
		assertThat(value2, equalTo(0L));
		
		for(RecommendationCategory rc : RecommendationCategory.values())
		{
			final Long value3 = messageRepository.countIncomingsByUser(userSender, rc);
			assertThat(value3, equalTo(0L));
		}
	}
	
	@Test
	public void countOutgoingsByUser()
	{
		// SENT + DELETED_OUTGOING + DELETE_INCOMING + HIDDEN_OUTGOING + HIDDEN_INCOMING
		final Long value1 = messageRepository.countOutgoingsByUser(userSender, RecommendationCategory.PARTNERSHIP);
		assertThat(value1, equalTo(5L));
		
		final Long value2 = messageRepository.countOutgoingsByUser(userSender, RecommendationCategory.FRIENDSHIP);
		assertThat(value2, equalTo(0L));
		
		for(RecommendationCategory rc : RecommendationCategory.values())
		{
			final Long value3 = messageRepository.countOutgoingsByUser(userReceiver, rc);
			assertThat(value3, equalTo(0L));
		}
	}
	
	//@Test
	public void countNewIncomingMessagesByUser() throws Exception
	{
		final Long value1 = messageRepository.countNewIncomingMessagesByUser(userReceiver, RecommendationCategory.PARTNERSHIP);
		assertThat(value1, equalTo(3L));
		
		final Long value2 = messageRepository.countNewIncomingMessagesByUser(userReceiver, RecommendationCategory.FRIENDSHIP);
		assertThat(value2, equalTo(0L));
		
		for(RecommendationCategory rc : RecommendationCategory.values())
		{
			final Long value3 = messageRepository.countNewIncomingMessagesByUser(userSender, rc);
			assertThat(value3, equalTo(0L));
		}
	}
	
//	@Test
//	public void findIncomingsAndOutgoingsByUserAndTargetUser() throws Exception
//	{
//		{
//			final long expected = 4;
//			final Page<Message> list = messageRepository.findIncomingsAndOutgoingsByUserAndTargetUser(userSender, userReceiver,userReceiver.getBlockedDate().minusMinutes(3), new PageRequest(0, 1000));
//			assertThat(list.getTotalElements(), equalTo(expected));
//			for (final Message message : list.getContent())
//			{
//				assertThat(message, anyOf(equalTo(messages.get(MessageState.SENT)), equalTo(messages.get(MessageState.HIDDEN_INCOMING)), equalTo(messages.get(MessageState.DELETED_INCOMING)), equalTo(messages.get(MessageState.SENT_CANCEL_MESSAGE))));
//			}
//		}
//
//		{
//			final long expected = 4;
//			final Page<Message> list = messageRepository.findIncomingsAndOutgoingsByUserAndTargetUser(userReceiver, userSender,userReceiver.getBlockedDate().minusMinutes(3), new PageRequest(0, 1000));
//			assertThat(list.getTotalElements(), equalTo(expected));
//			for (final Message message : list.getContent())
//			{
//				assertThat(message, anyOf(equalTo(messages.get(MessageState.SENT)), equalTo(messages.get(MessageState.HIDDEN_OUTGOING)), equalTo(messages.get(MessageState.DELETED_OUTGOING)), equalTo(messages.get(MessageState.SENT_CANCEL_MESSAGE))));
//			}
//		}
//	}
	
	@Test
	public void findAllByUserAndTargetUser() throws Exception
	{
		{
			final long expected = 5;
			final Page<Message> list = messageRepository.findAllByUserAndTargetUser(userSender, userReceiver, new PageRequest(0, 1000));
			assertThat(list.getTotalElements(), equalTo(expected));
			for (final Message message : list.getContent())
			{
				assertThat(message, anyOf(equalTo(messages.get(MessageState.SENT)), equalTo(messages.get(MessageState.HIDDEN_INCOMING)), equalTo(messages.get(MessageState.HIDDEN_OUTGOING)), equalTo(messages.get(MessageState.DELETED_INCOMING)), equalTo(messages.get(MessageState.SENT_CANCEL_MESSAGE))));
			}
		}
		
		{
			final long expected = 5;
			final Page<Message> list = messageRepository.findAllByUserAndTargetUser(userReceiver, userSender, new PageRequest(0, 1000));
			assertThat(list.getTotalElements(), equalTo(expected));
			for (final Message message : list.getContent())
			{
				assertThat(message, anyOf(equalTo(messages.get(MessageState.SENT)), equalTo(messages.get(MessageState.HIDDEN_INCOMING)), equalTo(messages.get(MessageState.HIDDEN_OUTGOING)), equalTo(messages.get(MessageState.DELETED_OUTGOING)), equalTo(messages.get(MessageState.SENT_CANCEL_MESSAGE))));
			}
		}
	}
	
	@Test
	public void countIncomingsAndOutgoingsByUserAndTargetUser() throws Exception
	{
		{
			final long expected = 4;
			final Long count = messageRepository.countIncomingsAndOutgoingsByUserAndTargetUser(userSender, userReceiver);
			assertThat(count, equalTo(expected));
		}
		
		{
			final long expected = 4;
			final Long count = messageRepository.countIncomingsAndOutgoingsByUserAndTargetUser(userReceiver, userSender);
			assertThat(count, equalTo(expected));
		}
	}
	
	@Test
	public void countIncomingAdminMessagesByUser() throws Exception
	{
		{
			final long expected = 1;
			final Long count = messageRepository.countIncomingAdminMessagesByUser(userReceiver);
			assertThat(count, equalTo(expected));
		}
	}
	
	@Test
	public void getMessageStatisticsTest()
	{
		final Set<Object[]> result = messageRepository.getMessageStatistics();
		
		assertThat(result, notNullValue());
		assertThat(result.size(), equalTo(1));
		
		final Object[] entry = result.iterator().next();
		
		assertThat(entry[0], equalTo(userSender.getId()));
		assertThat(entry[1], equalTo(userReceiver.getId()));
		assertThat(entry[2], equalTo(5L));
	}
	
	@Test
	public void getSendMessageStatisticsTest()
	{
		final Set<Object[]> result = messageRepository.getSendMessageStatistics(userSender);
		
		assertThat(result, notNullValue());
		assertThat(result.size(), equalTo(5));
		
		for(Object[] entry : result)
		{
			assertThat(entry[0], equalTo(userSender.getId()));
			assertThat(entry[1], equalTo(userReceiver.getId()));
			assertThat(entry[2], CoreMatchers.instanceOf(LocalDateTime.class));
		}
	}
	
	@Test
	public void getReceiveMessageStatisticsTest()
	{
		final Set<Object[]> result = messageRepository.getReceiveMessageStatistics(userReceiver);
		
		assertThat(result, notNullValue());
		assertThat(result.size(), equalTo(5));
		
		for(Object[] entry : result)
		{
			assertThat(entry[0], equalTo(userSender.getId()));
			assertThat(entry[1], equalTo(userReceiver.getId()));
			assertThat(entry[2], CoreMatchers.instanceOf(LocalDateTime.class));
		}
	}
	

	public void isSentToAdminTest()
	{
		final LocalDateTime startDate = LocalDateTime.now().minusDays(1);
		final MessageType messageType = MessageType.LOVE_SCAMMER;
		
		final Message message = entityFactory.persistDefaultMessage(null, userSender, MessageState.SENT);
		
		assertThat(messageRepository.isSentToAdmin(userSender, messageType, startDate), equalTo(false));
		
		message.setMessageType(messageType);
		messageRepository.save(message);
		
		assertThat(messageRepository.isSentToAdmin(userSender, messageType, startDate), equalTo(true));
		
		message.setSendDate(startDate.minusDays(1));
		messageRepository.save(message);
		
		assertThat(messageRepository.isSentToAdmin(userSender, messageType, startDate), equalTo(false));
	}
}

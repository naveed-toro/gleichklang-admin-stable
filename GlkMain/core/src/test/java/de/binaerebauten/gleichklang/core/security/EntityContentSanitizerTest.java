//package de.binaerebauten.gleichklang.core.security;
//
//import de.binaerebauten.gleichklang.core.model.media.MediaGallery;
//import de.binaerebauten.gleichklang.core.model.message.Message;
//import de.binaerebauten.gleichklang.core.model.questionnaire.TextAnswer;
//import de.binaerebauten.gleichklang.core.model.questionnaire.TextQuestion;
//import de.binaerebauten.gleichklang.core.model.user.Address;
//import de.binaerebauten.gleichklang.core.model.user.User;
//import de.binaerebauten.gleichklang.core.repository.BasePersistenceTest;
//import de.binaerebauten.gleichklang.core.utils.DefaultEntityFactory;
//import de.binaerebauten.gleichklang.core.utils.DefaultStaticEntityFactory;
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Ignore;
//import org.junit.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.transaction.support.TransactionCallback;
//import org.springframework.transaction.support.TransactionTemplate;
//
//import javax.persistence.EntityManager;
//
//import static org.hamcrest.CoreMatchers.*;
//import static org.junit.Assert.assertThat;
//
///**
// * Unit test for {@link EntityContentSanitizer}.
// */
//public class EntityContentSanitizerTest extends BasePersistenceTest
//{
//	private final static String DIV_WITH_ONCLICK = "<div onclick='alert()'></div>";
//	private final static String SANITIZED_DIV = "<div></div>";
//
//	@Autowired
//	private DefaultEntityFactory defaultEntityFactory;
//
//	/**
//	 * We use the JPA entity manager here so that it's easier to extend this test.
//	 */
//	@Autowired
//	private EntityManager entityManager;
//
//	@Autowired
//	private TransactionTemplate transactionTemplate;
//
//	private User user;
//
//	@Before
//	public void setup()
//	{
//		user = DefaultStaticEntityFactory.createDefaultUser("test@example.com", "test");
//	}
//
//	@After
//	public void teardown()
//	{
//		defaultEntityFactory.reset();
//	}
//
//	@Test
//	public void testSanitization()
//	{
//		user.setStatusMessage(DIV_WITH_ONCLICK);
//
//		persist(user);
//
//		assertThat("Expected that PrePersist event triggered sanitization",
//				user.getStatusMessage(), is(SANITIZED_DIV));
//
//		user.setStatusMessage(DIV_WITH_ONCLICK);
//
//		User mergedUser = merge(user);
//
//		assertThat("Expected that PreUpdate event triggered sanitization",
//				mergedUser.getStatusMessage(), is(SANITIZED_DIV));
//	}
//
//	@Test
//	public void testSanitization_KeepsNullValues()
//	{
//		User user = DefaultStaticEntityFactory.createDefaultUser("test@example.com", "test");
//
//		user.setStatusMessage(null);
//		user.setFirstName(null);
//		user.setLastName(null);
//
//		persist(user);
//
//		assertThat(user.getStatusMessage(), nullValue());
//		assertThat(user.getFirstName(), nullValue());
//		assertThat(user.getLastName(), nullValue());
//	}
//
//	@Test
//	@Ignore(value = "Dieses Fall funktioniert nicht wegen aktuellen sanitizing Algorithmus. Wir sollen eine Lösung für diesen Fall überlegen")
//	public void testSanitization_EmailChars() throws Exception
//	{
//		String sanitizeContent = "hello+world@example.com";
//		User user = DefaultStaticEntityFactory.createDefaultUser("test@example.com", "test");
//
//		user.setStatusMessage(null);
//		user.setFirstName(sanitizeContent);
//
//		persist(user);
//
//		assertThat(user.getFirstName(), equalTo(sanitizeContent));
//	}
//
//	@Test
//	public void testUser()
//	{
//		user.setStatusMessage(DIV_WITH_ONCLICK);
//		user.setFirstName(DIV_WITH_ONCLICK);
//		user.setLastName(DIV_WITH_ONCLICK);
//
//		persist(user);
//
//		assertThat(user.getStatusMessage(), is(SANITIZED_DIV));
//		assertThat(user.getFirstName(), is(SANITIZED_DIV));
//		assertThat(user.getLastName(), is(SANITIZED_DIV));
//	}
//
//	@Test
//	public void testAddress()
//	{
//
//		final Address address = DefaultStaticEntityFactory.createDefaultAddress(defaultEntityFactory.persistDefaultCountry());
//		address.setCity(DIV_WITH_ONCLICK);
//		address.setStreetWithNumber(DIV_WITH_ONCLICK);
//		user.addAddress(address);
//
//		final Address mergedAddress = persist(user).getAddresses().get(0);
//
//		assertThat(mergedAddress.getCity(), is(SANITIZED_DIV));
//		assertThat(mergedAddress.getStreetWithNumber(), is(SANITIZED_DIV));
//	}
//
//	@Test
//	public void testMessageSanitization()
//	{
//		persist(user);
//
//		final User receiver = defaultEntityFactory.persistDefaultUser("receiver");
//
//		final Message message = defaultEntityFactory.createDefaultMessage(receiver, user);
//
//		message.setSubject(DIV_WITH_ONCLICK);
//		message.setBody(DIV_WITH_ONCLICK);
//
//		persist(message);
//
//		assertThat(message.getSubject(), is(SANITIZED_DIV));
//		assertThat(message.getBody(), is(SANITIZED_DIV));
//	}
//
//	@Test
//	public void testMediaGallery()
//	{
//		MediaGallery mediaGallery = defaultEntityFactory.persistDefaultMediaGallery();
//
//		mediaGallery.setName(DIV_WITH_ONCLICK);
//
//		mediaGallery = merge(mediaGallery);
//
//		assertThat(mediaGallery.getName(), is(SANITIZED_DIV));
//	}
//
//	@Test
//	public void testTextAnswer()
//	{
//		persist(user);
//
//		TextQuestion textQuestion = defaultEntityFactory.persistDefaultTextQuestion();
//
//		TextAnswer textAnswer = new TextAnswer();
//
//		textAnswer.setUser(user);
//		textAnswer.setTextValue(DIV_WITH_ONCLICK);
//		textAnswer.setQuestion(textQuestion);
//
//		persist(textAnswer);
//
//		assertThat(textAnswer.getTextValue(), is(SANITIZED_DIV));
//	}
//
//	private <T> T merge(T entity)
//	{
//		TransactionCallback<T> mergeCallback = s -> {
//			T merge = entityManager.merge(entity);
//
//			return merge;
//		};
//		return perform(mergeCallback);
//	}
//
//	private <T> T persist(T entity)
//	{
//		TransactionCallback<T> persistCallback = s -> {
//			entityManager.persist(entity);
//
//			return entity;
//		};
//		return perform(persistCallback);
//	}
//
//	private <T> T perform(TransactionCallback<T> transactionCallback)
//	{
//		T persistedEntity = transactionTemplate.execute(transactionCallback);
//
//		return persistedEntity;
//	}
//
//}

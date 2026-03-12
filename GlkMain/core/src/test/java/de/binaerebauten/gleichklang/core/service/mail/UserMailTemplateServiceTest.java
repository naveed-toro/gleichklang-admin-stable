package de.binaerebauten.gleichklang.core.service.mail;

import de.binaerebauten.gleichklang.core.config.VelocityServiceTestConfig;
import de.binaerebauten.gleichklang.core.model.locatable.Continent;
import de.binaerebauten.gleichklang.core.model.locatable.Country;
import de.binaerebauten.gleichklang.core.model.mail.UserMailTemplate;
import de.binaerebauten.gleichklang.core.model.matching.Relationship;
import de.binaerebauten.gleichklang.core.model.payment.*;
import de.binaerebauten.gleichklang.core.model.user.EmailLink;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.service.DynamicContentTemplateService;
import de.binaerebauten.gleichklang.core.service.template.TemplateEngineService;
import de.binaerebauten.gleichklang.core.utils.DefaultStaticEntityFactory;
import de.binaerebauten.gleichklang.core.utils.PaymentEntityFactory;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link UserMailTemplateService}.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = VelocityServiceTestConfig.class)
public class UserMailTemplateServiceTest
{
	@Autowired
	private TemplateEngineService templateEngineService;
	
	@Mock
	private EmailLinkService emailLinkService;
	
	private UserMailTemplateService userMailTemplateService;
	
	private final PaymentEntityFactory paymentEntityFactory = new PaymentEntityFactory();

	private final DynamicContentTemplateService dynaContentService = new DynamicContentTemplateService();
	
	private User user;
	private Prepayment prepayment;
	private Subscription subscription;
	private String recommendationCategory;
	private Set<Relationship> recommendations;
	
	private Matcher<String> allTemplateVariablesSet()
	{
		return not(anyOf(containsString("${"), containsString("{{{")));
	}
	
	@Before
	public void setup()
	{
		user = DefaultStaticEntityFactory.createDefaultUser("me+@example.com", "me");
		user.setId(42L);
		user.setNewEmail("changed@example.com");
		
		final EmailLink defaultEmailLink = new EmailLink();
		defaultEmailLink.setUri(URI.create("http://localhost:8080"));
		defaultEmailLink.setUser(user);
		defaultEmailLink.setUniqueToken(UUID.randomUUID().toString());
		
		MockitoAnnotations.initMocks(this);
		when(emailLinkService.createUnsubscribeEmailLink(anyObject(), anyObject())).thenReturn(defaultEmailLink);
		when(emailLinkService.createPasswordEmailLink(anyObject(), anyObject())).thenReturn(defaultEmailLink);
		when(emailLinkService.createValidateEmailLink(anyObject(), anyObject())).thenReturn(defaultEmailLink);
		
		userMailTemplateService = new UserMailTemplateService(emailLinkService, templateEngineService,dynaContentService);
		
		Continent continent = DefaultStaticEntityFactory.createDefaultContinent();
		Country country = DefaultStaticEntityFactory.createDefaultCountry(continent);
		
		BankAccount bankAccount = paymentEntityFactory.createDefaultBankAccount(country);
		
		Invoice invoice = paymentEntityFactory.createDefaultInvoice(user);
		InitialSubscriptionOffer initialSubscriptionOffer = paymentEntityFactory.createIntialSubscriptionOffer("Initial",
				LocalDateTime.now(), 12, RecommendationCategory.FRIENDSHIP, RecommendationCategory.PARTNERSHIP);
		InvoiceItem invoiceItem = paymentEntityFactory.createDefaultInvoiceItem(invoice, initialSubscriptionOffer);
		invoice.getItems().add(invoiceItem);
		prepayment = paymentEntityFactory.createPrepayment(user, bankAccount, invoice, "ExteranlReferenceId");
		prepayment.setCreateDate(LocalDateTime.now());
		
		subscription = paymentEntityFactory.createSubscription(user, initialSubscriptionOffer, LocalDateTime.now());
		
		recommendationCategory = "Partnership";
		recommendations = new HashSet<>();
	}
	
	@Test
	public void testCreateMailTemplateInstance_ADMITTANCE_1() throws Exception
	{
		MailTemplateInstance mailTemplateInstance = userMailTemplateService.createMailTemplateInstance(UserMailTemplate.ADMITTANCE_1, prepayment);
		
		assertThat(mailTemplateInstance, notNullValue());
		//assertThat("Expected that all place holders are replaced!", mailTemplateInstance.getContent(), allTemplateVariablesSet());
	}
	
	
	public void testCreateMailTemplateInstance_ADMITTANCE_2() throws Exception
	{
		MailTemplateInstance mailTemplateInstance = userMailTemplateService.createMailTemplateInstance(UserMailTemplate.ADMITTANCE_1, prepayment);
		
		assertThat(mailTemplateInstance, notNullValue());
		assertThat("Expected that all place holders are replaced!", mailTemplateInstance.getContent(), allTemplateVariablesSet());
	}
	
	
	public void testCreateMailTemplateInstance_CB_ACCOUNTERROR_FIRST() throws Exception
	{
		MailTemplateInstance mailTemplateInstance = userMailTemplateService.createMailTemplateInstance(UserMailTemplate.CB_ACCOUNTERROR_FIRST, prepayment);
		
		assertThat(mailTemplateInstance, notNullValue());
		assertThat("Expected that all place holders are replaced!", mailTemplateInstance.getContent(), allTemplateVariablesSet());
	}
	
	
	public void testCreateMailTemplateInstance_CB_ACCOUNTERROR_RENEWAL_FIRST() throws Exception
	{
		MailTemplateInstance mailTemplateInstance = userMailTemplateService.createMailTemplateInstance(UserMailTemplate.CB_ACCOUNTERROR_RENEWAL_FIRST, prepayment);
		
		assertThat(mailTemplateInstance, notNullValue());
		assertThat("Expected that all place holders are replaced!", mailTemplateInstance.getContent(), allTemplateVariablesSet());
	}
	
	
	public void testCreateMailTemplateInstance_CB_INSUFFICIENT_FIRST() throws Exception
	{
		MailTemplateInstance mailTemplateInstance = userMailTemplateService.createMailTemplateInstance(UserMailTemplate.CB_INSUFFICIENT_FIRST, prepayment);
		
		assertThat(mailTemplateInstance, notNullValue());
		assertThat("Expected that all place holders are replaced!", mailTemplateInstance.getContent(), allTemplateVariablesSet());
	}
	
	
	public void testCreateMailTemplateInstance_CB_INSUFFICIENT_REMINDER() throws Exception
	{
		MailTemplateInstance mailTemplateInstance = userMailTemplateService.createMailTemplateInstance(UserMailTemplate.CB_INSUFFICIENT_REMINDER, prepayment);
		
		assertThat(mailTemplateInstance, notNullValue());
		assertThat("Expected that all place holders are replaced!", mailTemplateInstance.getContent(), allTemplateVariablesSet());
	}
	
	
	public void testCreateMailTemplateInstance_CB_INSUFFICIENT_RENEWAL_FIRST() throws Exception
	{
		MailTemplateInstance mailTemplateInstance = userMailTemplateService.createMailTemplateInstance(UserMailTemplate.CB_INSUFFICIENT_RENEWAL_FIRST, prepayment);
		
		assertThat(mailTemplateInstance, notNullValue());
		assertThat("Expected that all place holders are replaced!", mailTemplateInstance.getContent(), allTemplateVariablesSet());
	}
	
	
	public void testCreateMailTemplateInstance_CB_OTHER_FIRST() throws Exception
	{
		MailTemplateInstance mailTemplateInstance = userMailTemplateService.createMailTemplateInstance(UserMailTemplate.CB_OTHER_FIRST, prepayment);
		
		assertThat(mailTemplateInstance, notNullValue());
		assertThat("Expected that all place holders are replaced!", mailTemplateInstance.getContent(), allTemplateVariablesSet());
	}
	
	
	public void testCreateMailTemplateInstance_CB_OTHER_RENEWAL_FIRST() throws Exception
	{
		MailTemplateInstance mailTemplateInstance = userMailTemplateService.createMailTemplateInstance(UserMailTemplate.CB_OTHER_RENEWAL_FIRST, prepayment);
		
		assertThat(mailTemplateInstance, notNullValue());
		assertThat("Expected that all place holders are replaced!", mailTemplateInstance.getContent(), allTemplateVariablesSet());
	}
	
	@Ignore
	
	public void testCreateMailTemplateInstance_CB_REFUND_FIRST() throws Exception
	{
		MailTemplateInstance mailTemplateInstance = userMailTemplateService.createMailTemplateInstance(UserMailTemplate.CB_REFUND_FIRST, prepayment);
		
		assertThat(mailTemplateInstance, notNullValue());
		assertThat("Expected that all place holders are replaced!", mailTemplateInstance.getContent(), allTemplateVariablesSet());
	}
	
	@Ignore
	
	public void testCreateMailTemplateInstance_CB_REFUND_NEXT() throws Exception
	{
		MailTemplateInstance mailTemplateInstance = userMailTemplateService.createMailTemplateInstance(UserMailTemplate.CB_REFUND_NEXT, prepayment);
		
		assertThat(mailTemplateInstance, notNullValue());
		assertThat("Expected that all place holders are replaced!", mailTemplateInstance.getContent(), allTemplateVariablesSet());
	}
	
	
	public void testCreateMailTemplateInstance_CB_REVOCATION_FIRST() throws Exception
	{
		MailTemplateInstance mailTemplateInstance = userMailTemplateService.createMailTemplateInstance(UserMailTemplate.CB_REVOCATION_FIRST, prepayment);
		
		assertThat(mailTemplateInstance, notNullValue());
		assertThat("Expected that all place holders are replaced!", mailTemplateInstance.getContent(), allTemplateVariablesSet());
	}
	
	
	public void testCreateMailTemplateInstance_CB_REVOCATION_REMINDER() throws Exception
	{
		MailTemplateInstance mailTemplateInstance = userMailTemplateService.createMailTemplateInstance(UserMailTemplate.CB_REVOCATION_REMINDER, prepayment);
		
		assertThat(mailTemplateInstance, notNullValue());
		assertThat("Expected that all place holders are replaced!", mailTemplateInstance.getContent(), allTemplateVariablesSet());
	}
	
	
	public void testCreateMailTemplateInstance_CB_REVOCATION_RENEWAL_FIRST() throws Exception
	{
		MailTemplateInstance mailTemplateInstance = userMailTemplateService.createMailTemplateInstance(UserMailTemplate.CB_REVOCATION_RENEWAL_FIRST, prepayment);
		
		assertThat(mailTemplateInstance, notNullValue());
		assertThat("Expected that all place holders are replaced!", mailTemplateInstance.getContent(), allTemplateVariablesSet());
	}
	
	
	public void testCreateMailTemplateInstance_DONATION() throws Exception
	{
		MailTemplateInstance mailTemplateInstance = userMailTemplateService.createMailTemplateInstance(UserMailTemplate.DONATION, prepayment);
		
		assertThat(mailTemplateInstance, notNullValue());
		assertThat("Expected that all place holders are replaced!", mailTemplateInstance.getContent(), allTemplateVariablesSet());
	}
	
	
	public void testCreateMailTemplateInstance_EXTENSION() throws Exception
	{
		MailTemplateInstance mailTemplateInstance = userMailTemplateService.createMailTemplateInstance(UserMailTemplate.EXTENSION, prepayment);
		
		assertThat(mailTemplateInstance, notNullValue());
		assertThat("Expected that all place holders are replaced!", mailTemplateInstance.getContent(), allTemplateVariablesSet());
	}
	
	
	public void testCreateMailTemplateInstance_MAIL_CHANGE_VERIFICATION() throws Exception
	{
		MailTemplateInstance mailTemplateInstance = userMailTemplateService.createMailTemplateInstance(UserMailTemplate.MAIL_CHANGE_VERIFICATION, user);
		
		assertThat(mailTemplateInstance, notNullValue());
		assertThat("Expected that all place holders are replaced!", mailTemplateInstance.getContent(), allTemplateVariablesSet());
	}
	
	
	public void testCreateMailTemplateInstance_MISSING_PAYMENT_REMINDER() throws Exception
	{
		MailTemplateInstance mailTemplateInstance = userMailTemplateService.createMailTemplateInstance(UserMailTemplate.MISSING_PAYMENT_REMINDER, user);
		
		assertThat(mailTemplateInstance, notNullValue());
		assertThat("Expected that all place holders are replaced!", mailTemplateInstance.getContent(), allTemplateVariablesSet());
	}
	
	
	public void testCreateMailTemplateInstance_MISSING_QUESTIONAIRE_REMINDER() throws Exception
	{
		MailTemplateInstance mailTemplateInstance = userMailTemplateService.createMailTemplateInstance(UserMailTemplate.MISSING_QUESTIONAIRE_REMINDER, user);
		
		assertThat(mailTemplateInstance, notNullValue());
		assertThat("Expected that all place holders are replaced!", mailTemplateInstance.getContent(), allTemplateVariablesSet());
	}
	
	
	public void testCreateMailTemplateInstance_NEW_MATCH() throws Exception
	{
		MailTemplateInstance mailTemplateInstance = userMailTemplateService.createMailTemplateInstance(UserMailTemplate.NEW_MATCH, user, recommendations);
		
		assertThat(mailTemplateInstance, notNullValue());
		assertThat("Expected that all place holders are replaced!", mailTemplateInstance.getContent(), allTemplateVariablesSet());
	}
	
	
	public void testCreateMailTemplateInstance_NEW_MATCH_POSITIVE() throws Exception
	{
		MailTemplateInstance mailTemplateInstance = userMailTemplateService.createMailTemplateInstance(UserMailTemplate.NEW_MATCH_POSITIVE, user, user, RecommendationCategory.PARTNERSHIP);
		
		assertThat(mailTemplateInstance, notNullValue());
		assertThat("Expected that all place holders are replaced!", mailTemplateInstance.getContent(), allTemplateVariablesSet());
	}
	
	
	public void testCreateMailTemplateInstance_NEW_BOXNUMBER_CONTACT() throws Exception
	{
		MailTemplateInstance mailTemplateInstance = userMailTemplateService.createMailTemplateInstance(UserMailTemplate.NEW_BOXNUMBER_CONTACT, user, user, RecommendationCategory.PARTNERSHIP);
		
		assertThat(mailTemplateInstance, notNullValue());
		assertThat("Expected that all place holders are replaced!", mailTemplateInstance.getContent(), allTemplateVariablesSet());
	}
	
	
	public void testCreateMailTemplateInstance_NO_MATCH_MESSAGE() throws Exception
	{
		MailTemplateInstance mailTemplateInstance = userMailTemplateService.createMailTemplateInstance(UserMailTemplate.NO_MATCH_MESSAGE, user);
		
		assertThat(mailTemplateInstance, notNullValue());
		assertThat("Expected that all place holders are replaced!", mailTemplateInstance.getContent(), allTemplateVariablesSet());
	}
	
	
	public void testCreateMailTemplateInstance_OPTIMIZATION_PP() throws Exception
	{
		MailTemplateInstance mailTemplateInstance = userMailTemplateService.createMailTemplateInstance(UserMailTemplate.OPTIMIZATION_PP, prepayment);
		
		assertThat(mailTemplateInstance, notNullValue());
		assertThat("Expected that all place holders are replaced!", mailTemplateInstance.getContent(), allTemplateVariablesSet());
	}

	
	public void testCreateMailTemplateInstance_OPTIMIZATION_DONE() throws Exception
	{
		MailTemplateInstance mailTemplateInstance = userMailTemplateService.createMailTemplateInstance(UserMailTemplate.OPTIMIZATION_DONE, user, recommendationCategory, "text");
		
		assertThat(mailTemplateInstance, notNullValue());
		assertThat("Expected that all place holders are replaced!", mailTemplateInstance.getContent(), allTemplateVariablesSet());
	}
	
	
	public void testCreateMailTemplateInstance_OPTIMIZATION_REMINDER() throws Exception
	{
		MailTemplateInstance mailTemplateInstance = userMailTemplateService.createMailTemplateInstance(UserMailTemplate.OPTIMIZATION_REMINDER, prepayment);
		
		assertThat(mailTemplateInstance, notNullValue());
		assertThat("Expected that all place holders are replaced!", mailTemplateInstance.getContent(), allTemplateVariablesSet());
	}
	
	
	public void testCreateMailTemplateInstance_PP_PAID_CB_NOTIF() throws Exception
	{
		MailTemplateInstance mailTemplateInstance = userMailTemplateService.createMailTemplateInstance(UserMailTemplate.PP_PAID_CB_NOTIF, prepayment);
		
		assertThat(mailTemplateInstance, notNullValue());
		assertThat("Expected that all place holders are replaced!", mailTemplateInstance.getContent(), allTemplateVariablesSet());
	}
	
	
	public void testCreateMailTemplateInstance_PP_PAID_DONATION_NOTIF() throws Exception
	{
		MailTemplateInstance mailTemplateInstance = userMailTemplateService.createMailTemplateInstance(UserMailTemplate.PP_PAID_DONATION_NOTIF, prepayment);
		
		assertThat(mailTemplateInstance, notNullValue());
		assertThat("Expected that all place holders are replaced!", mailTemplateInstance.getContent(), allTemplateVariablesSet());
	}
	
	
	public void testCreateMailTemplateInstance_PP_PAID_OPTIMIZATION_NOTIF() throws Exception
	{
		MailTemplateInstance mailTemplateInstance = userMailTemplateService.createMailTemplateInstance(UserMailTemplate.PP_PAID_OPTIMIZATION_NOTIF, prepayment);
		
		assertThat(mailTemplateInstance, notNullValue());
		assertThat("Expected that all place holders are replaced!", mailTemplateInstance.getContent(), allTemplateVariablesSet());
	}
	
	
	public void testCreateMailTemplateInstance_PREPAYMENT_PAID_NOTFICATION()
	{
		MailTemplateInstance mailTemplateInstance = userMailTemplateService.createMailTemplateInstance(UserMailTemplate.PREPAYMENT_PAID_NOTFICATION, prepayment);
		
		assertThat(mailTemplateInstance, notNullValue());
		assertThat("Expected that all place holders are replaced!", mailTemplateInstance.getContent(), allTemplateVariablesSet());
	}
	
	
	public void testCreateMailTemplateInstance_PREPAYMENT_REMINDER_NEW()
	{
		MailTemplateInstance mailTemplateInstance = userMailTemplateService.createMailTemplateInstance(UserMailTemplate.PREPAYMENT_REMINDER_NEW, prepayment);
		
		assertThat(mailTemplateInstance, notNullValue());
		assertThat("Expected that all place holders are replaced!", mailTemplateInstance.getContent(), allTemplateVariablesSet());
	}
	
	
	public void testCreateMailTemplateInstance_PREPAYMENT_REMINDER_NEXT()
	{
		MailTemplateInstance mailTemplateInstance = userMailTemplateService.createMailTemplateInstance(UserMailTemplate.PREPAYMENT_REMINDER_NEXT, prepayment);
		
		assertThat(mailTemplateInstance, notNullValue());
		assertThat("Expected that all place holders are replaced!", mailTemplateInstance.getContent(), allTemplateVariablesSet());
	}
	
	
	public void testCreateMailTemplateInstance_PREPAYMENT_REMINDER_RENEWAL_NEW()
	{
		MailTemplateInstance mailTemplateInstance = userMailTemplateService.createMailTemplateInstance(UserMailTemplate.PREPAYMENT_REMINDER_RENEWAL_NEW, prepayment);
		
		assertThat(mailTemplateInstance, notNullValue());
		assertThat("Expected that all place holders are replaced!", mailTemplateInstance.getContent(), allTemplateVariablesSet());
	}
	
	
	public void testCreateMailTemplateInstance_PREPAYMENT_REMINDER_RENEWAL_NEXT()
	{
		MailTemplateInstance mailTemplateInstance = userMailTemplateService.createMailTemplateInstance(UserMailTemplate.PREPAYMENT_REMINDER_RENEWAL_NEXT, prepayment);
		
		assertThat(mailTemplateInstance, notNullValue());
		assertThat("Expected that all place holders are replaced!", mailTemplateInstance.getContent(), allTemplateVariablesSet());
	}
	
	
	public void testCreateMailTemplateInstance_REGISTRATION_USER()
	{
		MailTemplateInstance mailTemplateInstance = userMailTemplateService.createMailTemplateInstance(UserMailTemplate.REGISTRATION_USER, user);
		
		assertThat(mailTemplateInstance, notNullValue());
		assertThat("Expected that all place holders are replaced!", mailTemplateInstance.getContent(), allTemplateVariablesSet());
	}

	
	public void testCreateMailTemplateInstance_RENEWAL_CHOSEN_REMINDER()
	{
		MailTemplateInstance mailTemplateInstance = userMailTemplateService.createMailTemplateInstance(UserMailTemplate.RENEWAL_CHOSEN_REMINDER, subscription);

		assertThat(mailTemplateInstance, notNullValue());
		assertThat("Expected that all place holders are replaced!", mailTemplateInstance.getContent(), allTemplateVariablesSet());
	}
	
	
	public void testCreateMailTemplateInstance_RENEWAL_DISABLED_REMINDER()
	{
		MailTemplateInstance mailTemplateInstance = userMailTemplateService.createMailTemplateInstance(UserMailTemplate.RENEWAL_DISABLED_REMINDER, subscription);
		
		assertThat(mailTemplateInstance, notNullValue());
		assertThat("Expected that all place holders are replaced!", mailTemplateInstance.getContent(), allTemplateVariablesSet());
	}
	
	
	public void testCreateMailTemplateInstance_RENEWAL_FAILED_ACTUAL()
	{
		MailTemplateInstance mailTemplateInstance = userMailTemplateService.createMailTemplateInstance(UserMailTemplate.RENEWAL_FAILED_ACTUAL, prepayment);
		
		assertThat(mailTemplateInstance, notNullValue());
		assertThat("Expected that all place holders are replaced!", mailTemplateInstance.getContent(), allTemplateVariablesSet());
	}
	
	
	public void testCreateMailTemplateInstance_RENEWAL_NOTIF_DDCC()
	{
		MailTemplateInstance mailTemplateInstance = userMailTemplateService.createMailTemplateInstance(UserMailTemplate.RENEWAL_NOTIF_DDCC, prepayment);
		
		assertThat(mailTemplateInstance, notNullValue());
		assertThat("Expected that all place holders are replaced!", mailTemplateInstance.getContent(), allTemplateVariablesSet());
	}
	
	
	public void testCreateMailTemplateInstance_SIGNOFF_ACK_MSG()
	{
		MailTemplateInstance mailTemplateInstance = userMailTemplateService.createMailTemplateInstance(UserMailTemplate.SIGNOFF_ACK_MSG, user, "");
		
		assertThat(mailTemplateInstance, notNullValue());
		assertThat("Expected that all place holders are replaced!", mailTemplateInstance.getContent(), allTemplateVariablesSet());
	}
	
	
	public void testCreateMailTemplateInstance_SOCIAL_PP_USER()
	{
		MailTemplateInstance mailTemplateInstance = userMailTemplateService.createMailTemplateInstance(UserMailTemplate.SOCIAL_PP_USER, user);
		
		assertThat(mailTemplateInstance, notNullValue());
		assertThat("Expected that all place holders are replaced!", mailTemplateInstance.getContent(), allTemplateVariablesSet());
	}
	
	
	public void testCreateMailTemplateInstance_SOCIAL_UNREGISTERED_USER()
	{
		MailTemplateInstance mailTemplateInstance = userMailTemplateService.createMailTemplateInstance(UserMailTemplate.SOCIAL_UNREGISTERED_USER);
		
		assertThat(mailTemplateInstance, notNullValue());
		assertThat("Expected that all place holders are replaced!", mailTemplateInstance.getContent(), allTemplateVariablesSet());
	}
	
	
	public void testCreateMailTemplateInstance_SOCIAL_REGISTERED_USER()
	{
		MailTemplateInstance mailTemplateInstance = userMailTemplateService.createMailTemplateInstance(UserMailTemplate.SOCIAL_REGISTERED_USER, user);
		
		assertThat(mailTemplateInstance, notNullValue());
		assertThat("Expected that all place holders are replaced!", mailTemplateInstance.getContent(), allTemplateVariablesSet());
	}
	
}
package de.binaerebauten.gleichklang.adminweb.service;

import de.binaerebauten.gleichklang.core.model.NaturalKeyEntity.NaturalKey;
import de.binaerebauten.gleichklang.core.model.message.Message;
import de.binaerebauten.gleichklang.core.model.message.ReceiverEnvelope;
import de.binaerebauten.gleichklang.core.model.payment.Prepayment;
import de.binaerebauten.gleichklang.core.model.payment.Subscription;
import de.binaerebauten.gleichklang.core.model.questionnaire.*;
import de.binaerebauten.gleichklang.core.model.user.Address;
import de.binaerebauten.gleichklang.core.model.user.ClientInformation;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.model.user.UserSettings;
import de.binaerebauten.gleichklang.core.service.AnswerService;
import de.binaerebauten.gleichklang.core.service.ClientInformationService;
import de.binaerebauten.gleichklang.core.service.MessageService;
import de.binaerebauten.gleichklang.core.service.SubscriptionService;
import de.binaerebauten.gleichklang.core.service.file.AvatarService;
import de.binaerebauten.gleichklang.core.service.file.MediaService;
import de.binaerebauten.gleichklang.core.service.payment.PaymentService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ExportServiceTest
{
	@InjectMocks
	private ExportService exportService;
	
	@Mock
	private AnswerService answerService;
	
	@Mock
	private SubscriptionService subscriptionService;
	
	@Mock
	private PaymentService paymentService;
	
	@Mock
	private ClientInformationService clientInformationService;
	
	@Mock
	private MessageService messageService;
	
	@Mock
	private AvatarService avatarService;
	
	@Mock
	private MediaService mediaService;
	
	@Test
	public void exportUserInformationTest()
	{
		final User user = new User();
		user.setUserSettings(new UserSettings());
		user.getAddresses().add(new Address());
		
		final Message message = new Message();
		final ReceiverEnvelope receiverEnvelope = new ReceiverEnvelope();
		receiverEnvelope.setUser(new User());
		message.setReceiverEnvelope(receiverEnvelope);
		
		when(answerService.getAnswerValue(any(User.class), any(NaturalKey.class))).thenReturn("test");
		when(subscriptionService.findAllSubscriptions(any(User.class))).thenReturn(Collections.singletonList(new Subscription()));
		when(paymentService.findAllPayments(any(User.class))).thenReturn(Collections.singletonList(new Prepayment()));
		when(clientInformationService.findAll(any(User.class))).thenReturn(Collections.singletonList(new ClientInformation()));
		when(messageService.getOutgoingMessages(any(User.class))).thenReturn(Collections.singletonList(message));
		when(messageService.getAdminMessages(any(User.class))).thenReturn(Collections.singletonList(message));
		
		final InputStream stream = exportService.exportUserInformation(user);
		
		final BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
		assertThat(reader.lines().count(), greaterThan(10L));
		
		verify(answerService, atLeastOnce()).getAnswerValue(eq(user), any(NaturalKey.class));
		verify(subscriptionService).findAllSubscriptions(eq(user));
		verify(paymentService).findAllPayments(eq(user));
		verify(clientInformationService).findAll(eq(user));
		verify(messageService).getOutgoingMessages(eq(user));
		verify(messageService).getAdminMessages(eq(user));
	}
	
	@Test
	public void exportQuestionsAndAnswersTest() throws IOException
	{
		final User user = new User();
		final List<Answer> answers = new ArrayList<>();
		
		final TextAnswer textAnswer = new TextAnswer();
		final NumberAnswer numberAnswer = new NumberAnswer();
		
		answers.add(textAnswer);
		answers.add(numberAnswer);
		
		final Questionnaire questionnaire = new Questionnaire();
		final QuestionGroup questionGroup = new QuestionGroup();
		final Question textQuestion = new TextQuestion();
		final Question numberQuestion = new NumberQuestion();
		
		questionGroup.setQuestionnaire(questionnaire);
		textQuestion.setQuestionGroup(questionGroup);
		numberQuestion.setQuestionGroup(questionGroup);
		
		textAnswer.setQuestion(textQuestion);
		numberAnswer.setQuestion(numberQuestion);
		
		textAnswer.setTextValue("test");
		numberAnswer.setNumberValue(42);
		
		when(answerService.getAnswers(any(User.class))).thenReturn(answers);
		
		final InputStream stream = exportService.exportQuestionsAndAnswers(user);
		
		final BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
		
		final String[] header = reader.readLine().split(",");
		assertThat(header.length, equalTo(5));
		checkAnswerRow(reader.readLine(), "test");
		checkAnswerRow(reader.readLine(), "42");
		
		verify(answerService).getAnswers(eq(user));
	}
	
	private void checkAnswerRow(String row, String shouldContain)
	{
		final String[] values = row.split(",");
		assertThat(values.length, equalTo(5));
		assertThat(values[4], containsString(shouldContain));
	}
}

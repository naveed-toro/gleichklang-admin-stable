package de.binaerebauten.gleichklang.core.service;

import de.binaerebauten.gleichklang.core.model.questionnaire.Answer;
import de.binaerebauten.gleichklang.core.model.questionnaire.TextAnswer;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.repository.AnswerRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AnswerServiceTest_V2
{
	@InjectMocks
	private AnswerService answerService;
	
	@Mock
	private AnswerRepository answerRepository;
	
	@Test
	public void getAnswersTest()
	{
		final User user = new User();
		
		final TextAnswer emptyAnswer = new TextAnswer();
		final TextAnswer expectedAnswer = new TextAnswer();
		expectedAnswer.setTextValue("test");
		
		final List<Answer> answers = new ArrayList<>();
		answers.add(emptyAnswer);
		answers.add(expectedAnswer);
		
		when(answerRepository.findByUser(any(User.class))).thenReturn(answers);
		
		final List<Answer> result = answerService.getAnswers(user);
		
		assertThat(result.size(), equalTo(1));
		assertThat(result.iterator().next(), equalTo(expectedAnswer));
		
		verify(answerRepository).findByUser(user);
	}
}

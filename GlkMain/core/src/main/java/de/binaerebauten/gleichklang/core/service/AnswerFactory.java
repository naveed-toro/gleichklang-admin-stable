package de.binaerebauten.gleichklang.core.service;

import de.binaerebauten.gleichklang.core.model.questionnaire.*;
import de.binaerebauten.gleichklang.core.model.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnswerFactory
{
	private static final Logger LOG = LoggerFactory.getLogger(AnswerFactory.class);
	private static final AnswerFactory INSTANCE = new AnswerFactory();
	
	private AnswerFactory()
	{
		
	}
	
	public static AnswerFactory get()
	{
		return INSTANCE;
	}

	public <T extends Answer> T createNewAnswer(Question question)
	{
		return createNewAnswer(question, null);
	}

	public <T extends Answer> T createNewAnswer(Question question, User user)
	{
		T answerObject = null;
		try
		{
			Class<? extends Answer> answerClass = getAnswerClass(question);
			answerObject = (T) answerClass.newInstance();
			answerObject.setUser(user);
			answerObject.setQuestion(question);
		}
		catch (InstantiationException | IllegalAccessException e)
		{
			LOG.error("Answer could not be created", e);
		}
		return answerObject;
	}
	
	private Class<? extends Answer> getAnswerClass(Question question)
	{
		Class<? extends Answer> answerClass;
		if (question instanceof ChoiceQuestion)
		{
			answerClass = ChoiceAnswer.class;
		}
		else if (question instanceof NumberQuestion)
		{
			answerClass = NumberAnswer.class;
		}
		else if (question instanceof TextQuestion)
		{
			answerClass = TextAnswer.class;
		}
		else if (question instanceof RegionQuestion)
		{
			answerClass = RegionAnswer.class;
		}
		else
		{
			LOG.warn("Question is of unsupported type {}", question.getClass());
			answerClass = TextAnswer.class;
		}
		return answerClass;
	}
}

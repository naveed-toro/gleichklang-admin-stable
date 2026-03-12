package de.binaerebauten.gleichklang.core.model.questionnaire;

import com.google.common.collect.ComparisonChain;
import de.binaerebauten.gleichklang.core.model.BaseEntity;
import de.binaerebauten.gleichklang.core.model.user.User;

import javax.persistence.*;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * User's answer for a question.
 */
@Entity
@Table(name = "answer")
public abstract class Answer extends BaseEntity
{
	public static final boolean DEFAULT_RELATIONSHIP_VISIBLE = false;
	
	public enum AnswerType
	{
		TEXT(TextAnswer.class),
		CHOICE(ChoiceAnswer.class),
		NUMBER(NumberAnswer.class),
		REGION(RegionAnswer.class);
		
		private final Class<? extends Answer> answerClass;
		
		private static final Map<Class<? extends Answer>, AnswerType> reversMap = EnumSet.allOf(AnswerType.class)
				.stream()
				.collect(Collectors.toMap(AnswerType::getAnswerClass, v -> v));
		
		AnswerType(Class<? extends Answer> answerClass)
		{
			this.answerClass = answerClass;
		}
		
		public Class<? extends Answer> getAnswerClass()
		{
			return answerClass;
		}
		
		public static AnswerType valueOf(Class<? extends Answer> answerClass)
		{
			return reversMap.get(answerClass);
		}
		
		public static AnswerType valueOf(Answer answer)
		{
			for(Class<? extends Answer> answerClass : reversMap.keySet())
			{
				if(answerClass.isInstance(answer)) return reversMap.get(answerClass);
			}
			
			return null;
		}
		
		public Answer createAnswer()
		{
			try
			{
				return getAnswerClass().newInstance();
			}
			catch (InstantiationException | IllegalAccessException e)
			{
				return null;
			}
		}
	}
	
	/**
	 * Compares answers according to their questions questionnaire and question group
	 * sort order.
	 */
	public static final Comparator<Answer> COMPARATOR = (a1, a2) ->
	{
		final Question q1 = a1.getQuestion();
		final Question q2 = a2.getQuestion();

		final QuestionGroup qg1 = q1.getQuestionGroup();
		final QuestionGroup qg2 = q2.getQuestionGroup();

		final Questionnaire qn1 = qg1.getQuestionnaire();
		final Questionnaire qn2 = qg1.getQuestionnaire();

		return ComparisonChain.start()
				.compare(qn1, qn2, Questionnaire.COMPARATOR)
				.compare(qg1, qg2, QuestionGroup.COMPARATOR)
				.compare(q1, q2, Question.COMPARATOR)
				.result();
	};

	@ManyToOne(fetch = FetchType.LAZY)
	private User user;

	@ManyToOne(fetch = FetchType.EAGER)
	private Question question;

	/**
	 * This flag marks if another user can see this answer when he views
	 * this answer. It's only relevant if the question of this answer
	 * has {@link Question#isAdjustableRelationshipVisibility()} set to true.
	 */
	@Column(name = "relationship_visible")
	private boolean relationshipVisible = DEFAULT_RELATIONSHIP_VISIBLE;

	public User getUser()
	{
		return user;
	}

	public void setUser(User user)
	{
		this.user = user;
	}

	public Question getQuestion()
	{
		return question;
	}

	public void setQuestion(Question question)
	{
		this.question = question;
	}

	public abstract String getValue();

	public abstract boolean isAnswered();

	/**
	 * Returns true iff. this answer is visible to other users.
	 *
	 * @return true iff. this answer is visible to other users
	 */
	public boolean isVisibleToOtherUsers()
	{
		return !question.isAdjustableRelationshipVisibility()
				|| isRelationshipVisible();
	}

	public boolean isRelationshipVisible()
	{
		return relationshipVisible;
	}

	public void setRelationshipVisible(boolean relationshipVisible)
	{
		this.relationshipVisible = relationshipVisible;
	}
}

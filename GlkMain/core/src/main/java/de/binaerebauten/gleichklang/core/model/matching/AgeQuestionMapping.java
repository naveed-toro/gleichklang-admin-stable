package de.binaerebauten.gleichklang.core.model.matching;

import de.binaerebauten.gleichklang.core.model.questionnaire.NumberQuestion;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;

@XmlAccessorType(XmlAccessType.FIELD)
@Entity
public class AgeQuestionMapping extends AbstractQuestionsMapping
{
	@XmlIDREF
	@OneToOne
	@JoinColumn(name = "max_age_question_id")
	@NotNull
	private NumberQuestion maxAgeQuestion;

	@XmlIDREF
	@OneToOne
	@JoinColumn(name = "min_age_question_id")
	@NotNull
	private NumberQuestion minAgeQuestion;

	public NumberQuestion getMaxAgeQuestion()
	{
		return maxAgeQuestion;
	}

	public void setMaxAgeQuestion(NumberQuestion maxAgeQuestion)
	{
		this.maxAgeQuestion = maxAgeQuestion;
	}

	public NumberQuestion getMinAgeQuestion()
	{
		return minAgeQuestion;
	}

	public void setMinAgeQuestion(NumberQuestion minAgeQuestion)
	{
		this.minAgeQuestion = minAgeQuestion;
	}

	@Override
	public <T> T accept(QuestionsMappingVisitor<T> questionsMappingVisitor)
	{
		return questionsMappingVisitor.visit(this);
	}
}

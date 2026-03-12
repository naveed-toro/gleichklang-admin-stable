package de.binaerebauten.gleichklang.core.model.matching;

import de.binaerebauten.gleichklang.core.model.questionnaire.NumberQuestion;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlIDREF;

@XmlAccessorType(XmlAccessType.FIELD)
@Entity
public class NumberQuestionsMapping extends AbstractQuestionsMapping
{
	@XmlIDREF
	@OneToOne
	@JoinColumn(name = "fact_question_id")
	@NotNull
	private NumberQuestion factQuestion;

	@XmlIDREF
	@OneToOne
	@JoinColumn(name = "max_question_id")
	@NotNull
	private NumberQuestion maxQuestion;

	@XmlIDREF
	@OneToOne
	@JoinColumn(name = "min_question_id")
	@NotNull
	private NumberQuestion minQuestion;

	public NumberQuestion getFactQuestion()
	{
		return factQuestion;
	}

	public void setFactQuestion(NumberQuestion factQuestion)
	{
		this.factQuestion = factQuestion;
	}

	public NumberQuestion getMaxQuestion()
	{
		return maxQuestion;
	}

	public void setMaxQuestion(NumberQuestion maxQuestion)
	{
		this.maxQuestion = maxQuestion;
	}

	public NumberQuestion getMinQuestion()
	{
		return minQuestion;
	}

	public void setMinQuestion(NumberQuestion minQuestion)
	{
		this.minQuestion = minQuestion;
	}

	@Override
	public <T> T accept(QuestionsMappingVisitor<T> questionsMappingVisitor)
	{
		return questionsMappingVisitor.visit(this);
	}
}

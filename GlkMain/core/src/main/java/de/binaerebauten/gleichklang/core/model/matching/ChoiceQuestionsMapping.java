package de.binaerebauten.gleichklang.core.model.matching;

import de.binaerebauten.gleichklang.core.model.questionnaire.ChoiceQuestion;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlIDREF;

@XmlAccessorType(XmlAccessType.FIELD)
@Entity
public class ChoiceQuestionsMapping extends AbstractQuestionsMapping
{
    @XmlIDREF
	@ManyToOne
	@JoinColumn(name = "matrix_id")
	@NotNull
	private MatchingMatrix matrix;

    @XmlIDREF
	@ManyToOne
	@JoinColumn(name = "source_question_id")
	@NotNull
	private ChoiceQuestion sourceQuestion;

    @XmlIDREF
	@ManyToOne
	@JoinColumn(name = "target_question_id")
	@NotNull
	private ChoiceQuestion targetQuestion;

	public MatchingMatrix getMatrix()
	{
		return matrix;
	}

	public void setMatrix(MatchingMatrix matrix)
	{
		this.matrix = matrix;
	}

	public ChoiceQuestion getSourceQuestion()
	{
		return sourceQuestion;
	}

	public void setSourceQuestion(ChoiceQuestion sourceQuestion)
	{
		this.sourceQuestion = sourceQuestion;
	}

	public ChoiceQuestion getTargetQuestion()
	{
		return targetQuestion;
	}

	public void setTargetQuestion(ChoiceQuestion targetQuestion)
	{
		this.targetQuestion = targetQuestion;
	}

	@Override
	public <T> T accept(QuestionsMappingVisitor<T> questionsMappingVisitor)
	{
		return questionsMappingVisitor.visit(this);
	}
}

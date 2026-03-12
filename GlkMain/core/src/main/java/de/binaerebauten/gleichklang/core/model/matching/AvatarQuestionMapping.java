package de.binaerebauten.gleichklang.core.model.matching;

import de.binaerebauten.gleichklang.core.model.questionnaire.Choice;
import de.binaerebauten.gleichklang.core.model.questionnaire.ChoiceQuestion;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlIDREF;

@XmlAccessorType(XmlAccessType.FIELD)
@Entity
public class AvatarQuestionMapping extends AbstractQuestionsMapping
{
    @XmlIDREF
	@ManyToOne
	@JoinColumn(name = "avatar_question_id")
	@NotNull
	private ChoiceQuestion avatarQuestion;

    @XmlIDREF
	@ManyToOne
	@JoinColumn(name = "true_choice_id")
	@NotNull
	private Choice trueChoice;

	public ChoiceQuestion getAvatarQuestion()
	{
		return avatarQuestion;
	}

	public void setAvatarQuestion(ChoiceQuestion avatarQuestion)
	{
		this.avatarQuestion = avatarQuestion;
	}

	public Choice getTrueChoice()
	{
		return trueChoice;
	}

	public void setTrueChoice(Choice trueChoice)
	{
		this.trueChoice = trueChoice;
	}

	@Override
	public <T> T accept(QuestionsMappingVisitor<T> questionsMappingVisitor)
	{
		return questionsMappingVisitor.visit(this);
	}
}

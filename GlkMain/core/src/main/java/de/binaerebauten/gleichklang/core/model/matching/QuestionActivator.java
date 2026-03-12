package de.binaerebauten.gleichklang.core.model.matching;

import de.binaerebauten.gleichklang.core.model.questionnaire.Question;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;

@XmlAccessorType(XmlAccessType.FIELD)
@Entity
public class QuestionActivator extends Activator
{
    @XmlIDREF
	@OneToOne
	@JoinColumn(name = "enables_question_id")
	@NotNull
	private Question enablesQuestion;

	public Question getEnablesQuestion()
	{
		return enablesQuestion;
	}

	public void setEnablesQuestion(Question enablesQuestion)
	{
		this.enablesQuestion = enablesQuestion;
	}
}

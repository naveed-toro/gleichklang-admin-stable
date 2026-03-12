package de.binaerebauten.gleichklang.core.model.matching;

import de.binaerebauten.gleichklang.core.model.questionnaire.QuestionGroup;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlIDREF;

@XmlAccessorType(XmlAccessType.FIELD)
@Entity
public class QuestionGroupActivator extends Activator
{
    @XmlIDREF
	@OneToOne
	@JoinColumn(name = "enables_question_group_id")
	@NotNull
	private QuestionGroup enablesQuestionGroup;

	public QuestionGroup getEnablesQuestionGroup()
	{
		return enablesQuestionGroup;
	}

	public void setEnablesQuestionGroup(QuestionGroup enablesQuestionGroup)
	{
		this.enablesQuestionGroup = enablesQuestionGroup;
	}
}

package de.binaerebauten.gleichklang.core.model.matching;

import de.binaerebauten.gleichklang.core.model.questionnaire.Questionnaire;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlIDREF;

@XmlAccessorType(XmlAccessType.FIELD)
@Entity
public class QuestionnaireActivator extends Activator
{
    @XmlIDREF
	@OneToOne
	@JoinColumn(name = "enables_questionnaire_id")
	@NotNull
	private Questionnaire enablesQuestionnaire;

	public Questionnaire getEnablesQuestionnaire()
	{
		return enablesQuestionnaire;
	}

	public void setEnablesQuestionnaire(Questionnaire enablesQuestionnaire)
	{
		this.enablesQuestionnaire = enablesQuestionnaire;
	}
}

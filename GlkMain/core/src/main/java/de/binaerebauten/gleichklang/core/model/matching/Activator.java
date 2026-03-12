package de.binaerebauten.gleichklang.core.model.matching;

import de.binaerebauten.gleichklang.core.model.BaseEntity;
import de.binaerebauten.gleichklang.core.model.questionnaire.Choice;
import de.binaerebauten.gleichklang.core.model.questionnaire.ChoiceQuestion;
import de.binaerebauten.gleichklang.core.utils.DefaultEnumI18N;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.*;
import java.util.Set;

@XmlTransient
@XmlSeeAlso({QuestionActivator.class, QuestionGroupActivator.class, QuestionnaireActivator.class})
@Entity
@Table(name = "activator")
public abstract class Activator extends BaseEntity
{
	public enum ActivatorType implements DefaultEnumI18N
	{
		QUESTION(QuestionActivator.class),
		QUESTION_GROUP(QuestionGroupActivator.class),
		QUESTIONNAIRE(QuestionnaireActivator.class);

		private final Class<? extends Activator> activatorClass;

		ActivatorType(Class<? extends Activator> activatorClass)
		{
			this.activatorClass = activatorClass;
		}

		public Class<? extends Activator> getActivatorClass()
		{
			return activatorClass;
		}

		@Override
		public String toString()
		{
			return msg();
		}
	}

	@XmlAttribute
	@Column(name = "natural_key")
	@NotNull
	private String naturalKey;

	@XmlElementWrapper
	@XmlElement(name = "activatingChoice")
    @XmlIDREF
	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "activator_choice", inverseJoinColumns = { @JoinColumn(name = "choice_id") })
	@NotEmpty
	private Set<Choice> activatingChoices;

	@XmlIDREF
	@ManyToOne
	@JoinColumn(name = "activating_question_id")
	@NotNull
	private ChoiceQuestion activatingQuestion;

	@XmlAttribute
	@XmlID
	public String getUniqueXmlKey() {
		return this.getClass().getSimpleName() + "_" + naturalKey;
	}

	public String getNaturalKey()
	{
		return naturalKey;
	}

	public void setNaturalKey(String naturalKey)
	{
		this.naturalKey = naturalKey;
	}

	public Set<Choice> getActivatingChoices()
	{
		return activatingChoices;
	}

	public void setActivatingChoices(Set<Choice> activatingChoices)
	{
		this.activatingChoices = activatingChoices;
	}

	public ChoiceQuestion getActivatingQuestion()
	{
		return activatingQuestion;
	}

	public void setActivatingQuestion(ChoiceQuestion activatingQuestion)
	{
		this.activatingQuestion = activatingQuestion;
	}
}

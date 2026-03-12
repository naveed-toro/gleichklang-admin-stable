package de.binaerebauten.gleichklang.core.model.questionnaire;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
@Entity
public class ChoiceQuestion extends Question
{
    @XmlAttribute
	@NotNull
	@Column(name = "selection_type")
	@Enumerated(EnumType.STRING)
	private SelectionType selectionType;

    @XmlAttribute
	@NotNull
	@Column(name = "representation_type")
	@Enumerated(EnumType.STRING)
	private RepresentationType representationType = ChoiceQuestion.RepresentationType.DEFAULT;

    @XmlIDREF
	@NotNull
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "choice_group_id")
	private ChoiceGroup choiceGroup;
	
	/**
	 * A default choice for optional and important questions
	 * to prevent users being excluded by matching procedure
	 * in case they not answered this question.
	 */
	@XmlIDREF
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "default_choice_id")
	private Choice defaultChoice;

	public SelectionType getSelectionType()
	{
		return selectionType;
	}

	public void setSelectionType(SelectionType selectionType)
	{
		this.selectionType = selectionType;
	}
	
	public RepresentationType getRepresentationType()
	{
		return representationType;
	}

	public void setRepresentationType(RepresentationType representationType)
	{
		this.representationType = representationType;
	}

	public ChoiceGroup getChoiceGroup()
	{
		return choiceGroup;
	}

	public void setChoiceGroup(ChoiceGroup choiceGroup)
	{
		this.choiceGroup = choiceGroup;
	}
	
	public Choice getDefaultChoice()
	{
		return defaultChoice;
	}
	
	public void setDefaultChoice(Choice defaultChoice)
	{
		this.defaultChoice = defaultChoice;
	}
	
	public enum SelectionType
	{
		SINGLE, MULTIPLE;

		@Override
		public String toString()
		{
			return name();
		}
	}

    @XmlType(name ="choiceQuestionRepresentation")
	public enum RepresentationType
	{
		AFFINITY,
		RADIO,
		DEFAULT,
		AFFINITY_INVERTED
	}
}

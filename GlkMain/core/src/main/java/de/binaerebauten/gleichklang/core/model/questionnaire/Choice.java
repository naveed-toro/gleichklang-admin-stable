package de.binaerebauten.gleichklang.core.model.questionnaire;

import de.binaerebauten.gleichklang.core.model.I18NEntity.BaseName;
import de.binaerebauten.gleichklang.core.model.LocalizedEntity;
import de.binaerebauten.gleichklang.core.model.SortableEntity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlIDREF;

@XmlAccessorType(XmlAccessType.FIELD)
@Entity
@Table(name = "choice")
public class Choice extends LocalizedEntity implements SortableEntity<Long>
{
    @XmlIDREF
	@NotNull
	@ManyToOne
	@JoinColumn(name = "choice_group_id")
	private ChoiceGroup choiceGroup;

    @XmlAttribute
	@NotNull
	@Column(name = "sort_order")
	private int sortOrder;

	@Override
	public String getName(){
		return msg(BaseName.CHOICE_VALUE);
	}

	public ChoiceGroup getChoiceGroup()
	{
		return choiceGroup;
	}

	public void setChoiceGroup(ChoiceGroup choiceGroup)
	{
		this.choiceGroup = choiceGroup;
	}

	@Override
	public int getSortOrder()
	{
		return sortOrder;
	}

	@Override
	public void setSortOrder(int sortOrder)
	{
		this.sortOrder = sortOrder;
	}
	
	public int getPosition()
	{
		int position = 0;
		
		for (final Choice choice : choiceGroup.getChoices())
		{
			if(choice.equals(this)) break;
			position++;
		}
		
		return position;
	}

	@Override
	protected BaseName doGetBaseName()
	{
		return BaseName.CHOICE_VALUE;
	}
}

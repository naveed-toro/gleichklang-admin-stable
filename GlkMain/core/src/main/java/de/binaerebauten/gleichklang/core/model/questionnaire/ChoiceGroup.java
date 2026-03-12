package de.binaerebauten.gleichklang.core.model.questionnaire;

import de.binaerebauten.gleichklang.core.model.DeletableEntity;
import de.binaerebauten.gleichklang.core.model.I18NEntity.BaseName;
import de.binaerebauten.gleichklang.core.model.LocalizedEntity;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@Entity
@Table(name = "choice_group")
public class ChoiceGroup extends LocalizedEntity implements DeletableEntity<Long>
{
	@XmlAttribute
	@NotNull
	@Column
	private boolean deleted;

    @XmlElement(name = "choice")
    @XmlElementWrapper
	@NotEmpty
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "choiceGroup")
	@OrderBy("sortOrder")
	@Fetch(FetchMode.SUBSELECT)
	private List<Choice> choices = new ArrayList<>();

	public List<Choice> getChoices()
	{
		return choices;
	}

	public void setChoices(List<Choice> choices)
	{
		this.choices = choices;
	}

	@Override
	public boolean isDeleted()
	{
		return deleted;
	}

	@Override
	public void setDeleted(boolean deleted)
	{
		this.deleted = deleted;
	}
	
	@Override
	protected BaseName doGetBaseName()
	{
		return BaseName.CHOICE_GROUP;
	}
	
	@Override
	public String getName()
	{
		return msg(BaseName.CHOICE_GROUP);
	}
}

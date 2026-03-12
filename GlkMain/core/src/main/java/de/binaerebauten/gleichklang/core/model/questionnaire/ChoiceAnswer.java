package de.binaerebauten.gleichklang.core.model.questionnaire;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
public class ChoiceAnswer extends Answer
{
	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "choice_answer",
			joinColumns = { @JoinColumn(name = "answer_id") },
			inverseJoinColumns = { @JoinColumn(name = "choice_id") })
	private Set<Choice> choices = new HashSet<>();
	
	public Set<Choice> getChoices()
	{
		return choices;
	}
	
	public void setChoices(Set<Choice> choices)
	{
		this.choices = choices;
	}

	@Override
	public String getValue()
	{
		return choices.stream().map(Choice::getName).collect(Collectors.joining(", "));
	}

	@Override
	public boolean isAnswered()
	{
		return !choices.isEmpty();
	}
}

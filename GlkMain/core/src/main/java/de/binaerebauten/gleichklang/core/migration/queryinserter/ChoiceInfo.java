package de.binaerebauten.gleichklang.core.migration.queryinserter;

public class ChoiceInfo
{
	private Long id;
	private String question_id;
	private String choice;

	public Long getId()
	{
		return id;
	}

	public void setId(Long id)
	{
		this.id = id;
	}

	public String getQuestion_id()
	{
		return question_id;
	}

	public void setQuestion_id(String question_id)
	{
		this.question_id = question_id;
	}

	public String getChoice()
	{
		return choice;
	}

	public void setChoice(String choice)
	{
		this.choice = choice;
	}
	
	@Override
	public String toString()
	{
		return "ChoiceInfo{" +
				"id=" + id +
				", question_id='" + question_id + '\'' +
				", choice='" + choice + '\'' +
				'}';
	}
}

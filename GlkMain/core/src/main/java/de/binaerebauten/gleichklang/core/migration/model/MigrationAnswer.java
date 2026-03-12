package de.binaerebauten.gleichklang.core.migration.model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class MigrationAnswer
{
	private Long id;
	private String questionId;
	private String DTYPE;
	private String legacyId;
	private Timestamp changeDate;
	private Timestamp createDate;
	private Integer numberValue;
	private String textValue;
	private Long userId;
	private List<Long> choices = new ArrayList<>();

	public Long getId()
	{
		return id;
	}

	public void setId(Long id)
	{
		this.id = id;
	}

	public String getQuestionId()
	{
		return questionId;
	}

	public void setQuestionId(String questionId)
	{
		this.questionId = questionId;
	}

	public String getDTYPE()
	{
		return DTYPE;
	}

	public void setDTYPE(String DTYPE)
	{
		this.DTYPE = DTYPE;
	}

	public String getLegacyId()
	{
		return legacyId;
	}

	public void setLegacyId(String legacyId)
	{
		this.legacyId = legacyId;
	}

	public Timestamp getChangeDate()
	{
		return changeDate;
	}

	public void setChangeDate(Timestamp changeDate)
	{
		this.changeDate = changeDate;
	}

	public Timestamp getCreateDate()
	{
		return createDate;
	}

	public void setCreateDate(Timestamp createDate)
	{
		this.createDate = createDate;
	}

	public Integer getNumberValue()
	{
		return numberValue;
	}

	public void setNumberValue(Integer numberValue)
	{
		this.numberValue = numberValue;
	}

	public String getTextValue()
	{
		return textValue;
	}

	public void setTextValue(String textValue)
	{
		this.textValue = textValue;
	}

	public Long getUserId()
	{
		return userId;
	}

	public void setUserId(Long userId)
	{
		this.userId = userId;
	}

	public List<Long> getChoices()
	{
		return choices;
	}

	public void setChoices(List<Long> choices)
	{
		this.choices = choices;
	}
}

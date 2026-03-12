package de.binaerebauten.gleichklang.core.migration.queryinserter;

public class QuestionInfo
{
	private String id;
	private String label;
	private String DTYPE;
	private String selectionType;
	private Integer questionnaireId;
	private String questionnaireName;
	private Boolean required;
	private Boolean visible;
	private Integer questionGroupId;
	private Integer choiceGroupId;

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public String getLabel()
	{
		return label;
	}

	public void setLabel(String label)
	{
		this.label = label;
	}

	public String getDTYPE()
	{
		return DTYPE;
	}

	public void setDTYPE(String DTYPE)
	{
		this.DTYPE = DTYPE;
	}

	public String getSelectionType()
	{
		return selectionType;
	}

	public void setSelectionType(String selectionType)
	{
		this.selectionType = selectionType;
	}

	public Integer getQuestionnaireId()
	{
		return questionnaireId;
	}

	public void setQuestionnaireId(Integer questionnaireId)
	{
		this.questionnaireId = questionnaireId;
	}

	public String getQuestionnaireName()
	{
		return questionnaireName;
	}

	public void setQuestionnaireName(String questionnaireName)
	{
		this.questionnaireName = questionnaireName;
	}

	public Boolean getRequired()
	{
		return required;
	}

	public void setRequired(Boolean required)
	{
		this.required = required;
	}

	public Integer getQuestionGroupId()
	{
		return questionGroupId;
	}

	public void setQuestionGroupId(Integer questionGroupId)
	{
		this.questionGroupId = questionGroupId;
	}

	public Integer getChoiceGroupId()
	{
		return choiceGroupId;
	}

	public void setChoiceGroupId(Integer choiceGroupId)
	{
		this.choiceGroupId = choiceGroupId;
	}

	public Boolean getVisible()
	{
		return visible;
	}

	public void setVisible(Boolean visible)
	{
		this.visible = visible;
	}

	@Override
	public String toString()
	{
		return String.format("QuestionInfo{id=%s, label=%s, DTYPE=%s}", getId(), getLabel(), getDTYPE());
	}
}

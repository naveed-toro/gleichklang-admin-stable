package de.binaerebauten.gleichklang.core.migration.model;

import de.binaerebauten.gleichklang.core.model.questionnaire.ChoiceQuestion.SelectionType;

public class MigrationQuestion extends MigrationLocalisedBaseEntity implements Comparable<MigrationQuestion>
{

	private QUESTION_DTYPE DTYPE;
	private String label;
	private boolean required;

	private MigrationQuestionGroup questionGroup;
	private MigrationChoiceGroup choiceGroup;
	private SelectionType selectionType;
	private String representationType;

	private int minVal;
	private int maxVal;
	private int maxLength;
	private int numberOfLines;

	private int sortOrder;
	private boolean deleted = false;
	private boolean visible = true;

	public MigrationQuestion(String name, MigrationQuestionGroup questionGroup, int sortOrder)
	{
		this.setLabel(name);
		final String key = questionGroup.getQuestionnaire().getI18nKey() + "." + name;
		this.setLegacyId(key);
		this.setI18nKey(key);
		this.setQuestionGroup(questionGroup);
		this.setDeleted(false);
		this.setVisible(true);
		this.setSortOrder(sortOrder);
	}

	public String getLabel()
	{
		return label;
	}

	public void setLabel(String label)
	{
		this.label = label;
	}

	public boolean isRequired()
	{
		return required;
	}

	public void setRequired(boolean required)
	{
		this.required = required;
	}

	@Override
	public String toString()
	{
		return super.toString();
	}

	@Override
	public int compareTo(MigrationQuestion question)
	{
		return (int) (this.getId() - question.getId());
	}

	public QUESTION_DTYPE getDTYPE()
	{
		return DTYPE;
	}

	public void setDTYPE(QUESTION_DTYPE DTYPE)
	{
		this.DTYPE = DTYPE;
	}

	public SelectionType getSelectionType()
	{
		return selectionType;
	}

	public void setSelectionType(SelectionType selectionType)
	{
		this.selectionType = selectionType;
	}

	public int getMinVal()
	{
		return minVal;
	}

	public void setMinVal(int minVal)
	{
		this.minVal = minVal;
	}

	public int getMaxVal()
	{
		return maxVal;
	}

	public void setMaxVal(int maxVal)
	{
		this.maxVal = maxVal;
	}

	public int getMaxLength()
	{
		return maxLength;
	}

	public void setMaxLength(int maxLength)
	{
		this.maxLength = maxLength;
	}

	public int getNumberOfLines()
	{
		return numberOfLines;
	}

	public void setNumberOfLines(int numberOfLines)
	{
		this.numberOfLines = numberOfLines;
	}

	public MigrationQuestionGroup getQuestionGroup()
	{
		return questionGroup;
	}

	public void setQuestionGroup(MigrationQuestionGroup questionGroup)
	{
		this.questionGroup = questionGroup;
	}

	public int getSortOrder()
	{
		return sortOrder;
	}

	public void setSortOrder(int sortOrder)
	{
		this.sortOrder = sortOrder;
	}

	public boolean isDeleted()
	{
		return deleted;
	}

	public void setDeleted(boolean deleted)
	{
		this.deleted = deleted;
	}

	public MigrationChoiceGroup getChoiceGroup()
	{
		return choiceGroup;
	}

	public void setChoiceGroup(MigrationChoiceGroup choiceGroup)
	{
		this.choiceGroup = choiceGroup;
	}

	public boolean isVisible()
	{
		return visible;
	}

	public void setVisible(boolean visible)
	{
		this.visible = visible;
	}

	public String getRepresentationType()
	{
		if(representationType == null){
			return "DEFAULT";
		}
		return representationType;
	}

	public void setRepresentationType(String representationType)
	{
		this.representationType = representationType;
	}

	public enum QUESTION_DTYPE
	{
		BooleanQuestion,
		ChoiceQuestion,
		NumberQuestion,
		TextQuestion,
		RegionQuestion
	}
}

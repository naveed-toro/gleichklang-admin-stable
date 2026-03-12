package de.binaerebauten.gleichklang.core.model.questionnaire;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
public class NumberAnswer extends Answer
{
	@Column(name = "number_value")
	private Integer numberValue;

	public Integer getNumberValue()
	{
		return numberValue;
	}

	public void setNumberValue(Integer numberValue)
	{
		this.numberValue = numberValue;
	}

	@Override
	public String getValue()
	{
		return String.valueOf(numberValue);
	}

	@Override
	public boolean isAnswered()
	{
		NumberQuestion question = (NumberQuestion) getQuestion();
		return numberValue != null && (numberValue >= question.getMinVal() && numberValue <= question.getMaxVal() || question.getMinVal() + question.getMaxVal() == 0);
	}
}

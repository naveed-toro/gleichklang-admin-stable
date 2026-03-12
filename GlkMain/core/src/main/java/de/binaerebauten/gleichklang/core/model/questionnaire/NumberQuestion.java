package de.binaerebauten.gleichklang.core.model.questionnaire;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.validation.constraints.Min;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.FIELD)
@Entity
public class NumberQuestion extends Question
{
	@Min(0)
	@Column(name = "min_value")
	private int minVal;

	@Min(0)
	@Column(name = "max_value")
	private int maxVal;

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
}

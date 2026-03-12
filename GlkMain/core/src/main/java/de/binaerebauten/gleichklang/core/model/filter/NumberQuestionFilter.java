package de.binaerebauten.gleichklang.core.model.filter;

import de.binaerebauten.gleichklang.core.model.questionnaire.NumberQuestion;
import de.binaerebauten.gleichklang.core.utils.filter.FilterVisitor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
@Entity
public class NumberQuestionFilter extends UserFilter
{
    @XmlIDREF
	@ManyToOne
	@JoinColumn(name = "number_question_id")
	@NotNull
	private NumberQuestion numberQuestion;
	
	@XmlAttribute
	@Column(name = "min_value")
	@NotNull
	private int min;
	
	@XmlAttribute
	@Column(name = "max_value")
	@NotNull
	private int max;
	
	public NumberQuestion getNumberQuestion()
	{
		return numberQuestion;
	}
	
	public void setNumberQuestion(NumberQuestion numberQuestion)
	{
		this.numberQuestion = numberQuestion;
	}
	
	public int getMin()
	{
		return min;
	}
	
	public void setMin(int min)
	{
		this.min = min;
	}
	
	public int getMax()
	{
		return max;
	}
	
	public void setMax(int max)
	{
		this.max = max;
	}
	
	@Override
	public <T> T accept(FilterVisitor<T> filterVisitor)
	{
		return filterVisitor.visit(this);
	}

	@Override
	public String getName()
	{
		return getNumberQuestion().getName() + ": " + min + " - " + max;
	}
}

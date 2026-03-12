package de.binaerebauten.gleichklang.core.model.filter;

import de.binaerebauten.gleichklang.core.model.questionnaire.TextQuestion;
import de.binaerebauten.gleichklang.core.utils.XmlCDATAAdapter;
import de.binaerebauten.gleichklang.core.utils.filter.FilterVisitor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@Entity
public class TextQuestionFilter extends UserFilter
{
    @XmlIDREF
    @XmlAttribute
	@ManyToOne
	@JoinColumn(name = "text_question_id")
	@NotNull
	private TextQuestion textQuestion;

    @XmlValue
    @XmlJavaTypeAdapter(XmlCDATAAdapter.class)
	@Column(name = "text_value")
	@NotNull
	private String text;

	@Override
	public <T> T accept(FilterVisitor<T> filterVisitor)
	{
		return filterVisitor.visit(this);
	}

	public TextQuestion getTextQuestion()
	{
		return textQuestion;
	}

	public void setTextQuestion(TextQuestion textQuestion)
	{
		this.textQuestion = textQuestion;
	}

	public String getText()
	{
		return text;
	}

	public void setText(String text)
	{
		this.text = text;
	}

	@Override
	public String getName()
	{
		return getTextQuestion().getName() + ": " + text;
	}
}

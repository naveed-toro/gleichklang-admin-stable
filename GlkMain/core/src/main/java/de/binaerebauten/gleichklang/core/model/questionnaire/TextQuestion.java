package de.binaerebauten.gleichklang.core.model.questionnaire;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@Entity
public class TextQuestion extends Question
{
	@Min(1)
	@Max(65535)
	@Column(name = "max_length")
	private int maxLength;

	@Min(1)
	@Max(25)
	@Column(name = "number_of_lines")
	private int numberOfLines;

    @XmlAttribute
	@NotNull
	@Column(name = "representation_type")
	@Enumerated(EnumType.STRING)
	private RepresentationType representationType = RepresentationType.DEFAULT;

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

	public RepresentationType getRepresentationType()
	{
		return representationType;
	}

	public void setRepresentationType(RepresentationType representationType)
	{
		this.representationType = representationType;
	}

    @XmlType(name ="textQuestionRepresentation")
	public enum RepresentationType{
		DEFAULT, RICH_TEXT
	}
}

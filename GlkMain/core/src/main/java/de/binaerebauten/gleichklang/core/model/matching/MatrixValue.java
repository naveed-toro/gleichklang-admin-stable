package de.binaerebauten.gleichklang.core.model.matching;

import de.binaerebauten.gleichklang.core.model.BaseEntity;
import de.binaerebauten.gleichklang.core.model.questionnaire.Choice;
import de.binaerebauten.gleichklang.core.utils.DefaultEnumI18N;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlIDREF;

@XmlAccessorType(XmlAccessType.FIELD)
@Entity
@Table(name = "matching_matrix_value")
public class MatrixValue extends BaseEntity
{
	public enum Strictness implements DefaultEnumI18N
	{
		_1,
		_2,
		_3,
		_4,
		EXCLUSION;

		public static final Strictness firstStrictness = Strictness.values()[0];
		public static final Strictness lastStrictness = Strictness.values()[Strictness.values().length - 1];

		@Override
		public String toString()
		{
			return msg();
		}
	}

    @XmlIDREF
	@ManyToOne
	@JoinColumn(name = "matrix_id")
	@NotNull
	private MatchingMatrix matrix;

    @XmlIDREF
	@ManyToOne
	@JoinColumn(name = "source_choice_id")
	@NotNull
	private Choice sourceChoice;

    @XmlIDREF
	@ManyToOne
	@JoinColumn(name = "target_choice_id")
	@NotNull
	private Choice targetChoice;

    @XmlAttribute
	@Column
	@Enumerated(EnumType.ORDINAL)
	@NotNull
	private Strictness strictness;

	public MatchingMatrix getMatrix()
	{
		return matrix;
	}

	public void setMatrix(MatchingMatrix matrix)
	{
		this.matrix = matrix;
	}

	public Choice getSourceChoice()
	{
		return sourceChoice;
	}

	public void setSourceChoice(Choice sourceChoice)
	{
		this.sourceChoice = sourceChoice;
	}

	public Choice getTargetChoice()
	{
		return targetChoice;
	}

	public void setTargetChoice(Choice targetChoice)
	{
		this.targetChoice = targetChoice;
	}

	public Strictness getStrictness()
	{
		return strictness;
	}

	public void setStrictness(Strictness strictness)
	{
		this.strictness = strictness;
	}

    /**
     * Helper method for identifying matrix value for map
     *
     * @return
     */
    public String getUniqueKey() {
        return matrix.getName() + ":" + sourceChoice.getI18nKey() + ":" + targetChoice.getI18nKey();
    }
}

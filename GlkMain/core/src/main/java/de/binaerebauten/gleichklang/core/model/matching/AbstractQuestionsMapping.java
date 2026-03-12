package de.binaerebauten.gleichklang.core.model.matching;

import de.binaerebauten.gleichklang.core.model.BaseEntity;
import de.binaerebauten.gleichklang.core.model.matching.MatrixValue.Strictness;
import de.binaerebauten.gleichklang.core.utils.DefaultEnumI18N;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;


@XmlTransient
@XmlSeeAlso({NumberQuestionsMapping.class, AgeQuestionMapping.class, AffinityMapping.class, ChoiceQuestionsMapping.class, AvatarQuestionMapping.class})
@Entity
@Table(name = "questions_mapping")
public abstract class AbstractQuestionsMapping extends BaseEntity
{
	public enum MatcherType implements DefaultEnumI18N
	{
		MATRIX(ChoiceQuestionsMapping.class),
		NUMBER(NumberQuestionsMapping.class),
		AFFINITY(AffinityMapping.class),
		AGE(AgeQuestionMapping.class),
		AVATAR(AvatarQuestionMapping.class);

		private final Class<? extends AbstractQuestionsMapping> mappingClass;

		MatcherType(Class<? extends AbstractQuestionsMapping> mappingClass)
		{
			this.mappingClass = mappingClass;
		}

		public Class<? extends AbstractQuestionsMapping> getMappingClass()
		{
			return mappingClass;
		}

		@Override
		public String toString()
		{
			return msg();
		}
	}

    @XmlAttribute
	@Column(name = "natural_key")
	@NotNull
	private String naturalKey;
	
	@XmlAttribute
	@Column(name = "default_empty_strictness")
	@Enumerated(EnumType.ORDINAL)
	@NotNull
	private Strictness defaultEmptyStrictness = Strictness.firstStrictness;
	
	@XmlAttribute
	@XmlID
	public String getUniqueXMLKey() {
		return this.getClass().getSimpleName() + "_" + naturalKey;
	}
	
	@Transient
    @XmlTransient
    private String uniqueXMLKey;

	public abstract <T> T accept(QuestionsMappingVisitor<T> questionsMappingVisitor);

	public String getNaturalKey()
	{
		return naturalKey;
	}

	public void setNaturalKey(String naturalKey)
	{
		this.naturalKey = naturalKey;
	}
	
	public Strictness getDefaultEmptyStrictness()
	{
		return defaultEmptyStrictness;
	}
	
	public void setDefaultEmptyStrictness(Strictness defaultEmptyStrictness)
	{
		this.defaultEmptyStrictness = defaultEmptyStrictness;
	}
}

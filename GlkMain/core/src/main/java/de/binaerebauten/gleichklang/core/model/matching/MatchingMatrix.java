package de.binaerebauten.gleichklang.core.model.matching;

import de.binaerebauten.gleichklang.core.model.BaseEntity;
import de.binaerebauten.gleichklang.core.model.questionnaire.ChoiceGroup;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.*;
import java.util.HashSet;
import java.util.Set;

@XmlAccessorType(XmlAccessType.FIELD)
@Entity
@Table(name = "matching_matrix")
public class MatchingMatrix extends BaseEntity
{
    @XmlID
    @XmlAttribute
    @NotBlank
    @Size(max = 31)
    private String name;

    @XmlIDREF
	@ManyToOne
	@JoinColumn(name = "source_choice_group_id")
	@NotNull
	private ChoiceGroup sourceChoiceGroup;

    @XmlIDREF
	@ManyToOne
	@JoinColumn(name = "target_choice_group_id")
	@NotNull
	private ChoiceGroup targetChoiceGroup;

    @XmlElement(name = "matrixValues")
    @XmlElementWrapper
	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "matrix")
	@NotEmpty
	private Set<MatrixValue> matrixValues = new HashSet<>(0);

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public ChoiceGroup getSourceChoiceGroup()
	{
		return sourceChoiceGroup;
	}

	public void setSourceChoiceGroup(ChoiceGroup sourceChoiceGroup)
	{
		this.sourceChoiceGroup = sourceChoiceGroup;
	}

	public ChoiceGroup getTargetChoiceGroup()
	{
		return targetChoiceGroup;
	}

	public void setTargetChoiceGroup(ChoiceGroup targetChoiceGroup)
	{
		this.targetChoiceGroup = targetChoiceGroup;
	}

	public Set<MatrixValue> getMatrixValues()
	{
		return matrixValues;
	}

	public void setMatrixValues(Set<MatrixValue> matrixValues)
	{
		this.matrixValues = matrixValues;
	}
}

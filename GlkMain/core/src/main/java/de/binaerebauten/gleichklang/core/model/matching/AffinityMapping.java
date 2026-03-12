package de.binaerebauten.gleichklang.core.model.matching;

import de.binaerebauten.gleichklang.core.model.questionnaire.ChoiceQuestion;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.xml.bind.annotation.*;
import java.util.HashSet;
import java.util.Set;

@XmlAccessorType(XmlAccessType.FIELD)
@Entity
public class AffinityMapping extends AbstractQuestionsMapping
{
    @XmlAttribute
	@Column(name = "max_distance")
	@Min(0)
	private int maxDistance;

	@XmlIDREF
    @XmlElement(name = "question")
    @XmlElementWrapper
	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "affinity_question", inverseJoinColumns = { @JoinColumn(name = "question_id") })
	@NotEmpty
	private Set<ChoiceQuestion> questions = new HashSet<>(0);

	public int getMaxDistance()
	{
		return maxDistance;
	}

	public void setMaxDistance(int maxDistance)
	{
		this.maxDistance = maxDistance;
	}

	public Set<ChoiceQuestion> getQuestions()
	{
		return questions;
	}

	public void setQuestions(Set<ChoiceQuestion> questions)
	{
		this.questions = questions;
	}

	@Override
	public <T> T accept(QuestionsMappingVisitor<T> questionsMappingVisitor)
	{
		return questionsMappingVisitor.visit(this);
	}
}

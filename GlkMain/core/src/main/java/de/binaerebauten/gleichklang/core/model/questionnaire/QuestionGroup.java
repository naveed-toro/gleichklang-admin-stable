package de.binaerebauten.gleichklang.core.model.questionnaire;

import de.binaerebauten.gleichklang.core.model.DeletableEntity;
import de.binaerebauten.gleichklang.core.model.I18NEntity.BaseName;
import de.binaerebauten.gleichklang.core.model.LocalizedEntity;
import de.binaerebauten.gleichklang.core.model.SortableEntity;

import javax.persistence.*;
import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@Entity
@Table(name = "question_group")
public class QuestionGroup extends LocalizedEntity implements DeletableEntity<Long>, SortableEntity<Long>
{
    @XmlElement(name = "question")
	@OneToMany(mappedBy = "questionGroup")
	@OrderBy("sortOrder")
	private List<Question> questions = new ArrayList<>();

	@XmlIDREF
	@ManyToOne
	@JoinColumn(name = "questionnaire_id")
	private Questionnaire questionnaire;

	@XmlAttribute
	@Column(name = "sort_order")
	private int sortOrder;

	@XmlAttribute
	private boolean deleted = false;

	public String getName(){
		return msg(BaseName.QUESTION_GROUP_NAME);
	}

	public String getDescription()
	{
		return msg(BaseName.QUESTION_GROUP_DESCRIPTION);
	}

	public List<Question> getQuestions()
	{
		return questions;
	}

	public void setQuestions(List<Question> questions)
	{
		this.questions = questions;
	}

	public Questionnaire getQuestionnaire()
	{
		return questionnaire;
	}

	public void setQuestionnaire(Questionnaire questionnaire)
	{
		this.questionnaire = questionnaire;
	}

	public void addQuestion(Question question)
	{
		question.setQuestionGroup(this);
		this.questions.add(question);
	}

	@Override
	public int getSortOrder()
	{
		return sortOrder;
	}

	@Override
	public void setSortOrder(int sortOrder)
	{
		this.sortOrder = sortOrder;
	}

	@Override
	public boolean isDeleted()
	{
		return deleted;
	}

	public boolean isActive(){
		return !isDeleted();
	}

	@Override
	public void setDeleted(boolean deleted)
	{
		this.deleted = deleted;
	}
	
	public boolean isDeletedRecursive()
	{
		return isDeleted() || questionnaire.isDeleted();
	}
	
	@Override
	protected BaseName doGetBaseName()
	{
		return BaseName.QUESTION_GROUP_NAME;
	}
}

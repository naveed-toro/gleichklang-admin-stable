package de.binaerebauten.gleichklang.core.model.questionnaire;

import de.binaerebauten.gleichklang.core.model.DeletableEntity;
import de.binaerebauten.gleichklang.core.model.I18NEntity.BaseName;
import de.binaerebauten.gleichklang.core.model.LocalizedEntity;
import de.binaerebauten.gleichklang.core.model.SortableEntity;
import de.binaerebauten.gleichklang.core.utils.DefaultEnumI18N;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@XmlTransient
@XmlSeeAlso({ TextQuestion.class, NumberQuestion.class, ChoiceQuestion.class, RegionQuestion.class })
@Entity
@Table(name = "question")
public abstract class Question extends LocalizedEntity implements DeletableEntity<Long>, SortableEntity<Long>
{
	public enum QuestionType implements DefaultEnumI18N
	{
		NUMBER_QUESTION(NumberQuestion.class),
		CHOICE_QUESTION(ChoiceQuestion.class),
		TEXT_QUESTION(TextQuestion.class),
		REGION_QUESTION(RegionQuestion.class);
		
		private final Class<? extends Question> questionClass;
		
		QuestionType(Class<? extends Question> questionClass)
		{
			this.questionClass = questionClass;
		}
		
		public Class<? extends Question> getQuestionClass()
		{
			return questionClass;
		}
		
		@Override
		public String toString()
		{
			return msg();
		}
	}
	
	public enum Requirement
	{
		REQUIRED,
		IMPORTANT,
		OPTIONAL;
		
		public static final List<Requirement> optionalRequirements = Arrays.asList(OPTIONAL, IMPORTANT);
		public static final List<Requirement> requiredRequirements = Collections.singletonList(REQUIRED);
	}
	
	@XmlAttribute
	@NotNull
	@Enumerated(EnumType.STRING)
	@Column
	private Requirement requirement = Requirement.OPTIONAL;
	
	@XmlAttribute
	@NotNull
	@Column
	private boolean deleted;
	
	@XmlAttribute
	@NotNull
	@Column(name = "sort_order")
	private int sortOrder;
	
	@XmlAttribute
	@Column(name = "only_admin_visible")
	private boolean onlyAdminVisible = false;
	
	/**
	 * This flag controls if a user can surpress that other users
	 * can see an answer to this question {@link Answer#relationshipVisible}.
	 */
	@XmlAttribute
	@NotNull
	@Column(name = "adjustable_relationship_visibility")
	private boolean adjustableRelationshipVisibility;
	
	@XmlIDREF
	@NotNull
	@ManyToOne
	@JoinColumn(name = "question_group_id")
	private QuestionGroup questionGroup;
	
	public Requirement getRequirement()
	{
		return requirement;
	}
	
	public void setRequirement(Requirement requirement)
	{
		this.requirement = requirement;
	}
	
	public boolean isRequired()
	{
		return Requirement.REQUIRED.equals(getRequirement());
	}
	
	public QuestionGroup getQuestionGroup()
	{
		return questionGroup;
	}
	
	public void setQuestionGroup(
			QuestionGroup questionGroup)
	{
		this.questionGroup = questionGroup;
	}
	
	@Override
	protected BaseName doGetBaseName()
	{
		return BaseName.QUESTION_NAME;
	}
	
	public String getName()
	{
		return msg(BaseName.QUESTION_NAME);
	}
	
	public String getDescription()
	{
		return msg(BaseName.QUESTION_DESCRIPTION);
	}
	
	@Override
	public boolean isDeleted()
	{
		return deleted;
	}
	
	@Override
	public void setDeleted(boolean deleted)
	{
		this.deleted = deleted;
	}
	
	public boolean isDeletedRecursive()
	{
		return isDeleted() || questionGroup.isDeletedRecursive();
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
	
	public boolean isOnlyAdminVisible()
	{
		return onlyAdminVisible;
	}
	
	public void setOnlyAdminVisible(boolean onlyAdminVisible)
	{
		this.onlyAdminVisible = onlyAdminVisible;
	}
	
	public boolean isAdjustableRelationshipVisibility()
	{
		return adjustableRelationshipVisibility;
	}
	
	public void setAdjustableRelationshipVisibility(boolean adjustableRelationshipVisibility)
	{
		this.adjustableRelationshipVisibility = adjustableRelationshipVisibility;
	}
	
	public boolean isActive()
	{
		return !isDeleted() && !isOnlyAdminVisible();
	}
}

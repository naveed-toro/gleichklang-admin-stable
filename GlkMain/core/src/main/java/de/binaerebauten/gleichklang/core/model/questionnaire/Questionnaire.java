package de.binaerebauten.gleichklang.core.model.questionnaire;

import com.vaadin.server.ThemeResource;
import de.binaerebauten.gleichklang.core.model.DeletableEntity;
import de.binaerebauten.gleichklang.core.model.I18NEntity.BaseName;
import de.binaerebauten.gleichklang.core.model.LocalizedEntity;
import de.binaerebauten.gleichklang.core.model.SortableEntity;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;

import javax.persistence.*;
import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@Entity
@Table(name = "questionnaire")
public class Questionnaire extends LocalizedEntity implements SortableEntity<Long>, DeletableEntity<Long>
{
	public enum Icon
	{
		DEFAULT,
        LOCATION,
        THOUGHT,
		ABOUTME,
		SEARCH;

		public ThemeResource getResource() {
		    ThemeResource icon = null;
		    switch (this) {
                case DEFAULT:
                    icon = new ThemeResource("img/icon_questionnaire_default-outline.svg");
                    break;

                case LOCATION:
                    icon = new ThemeResource("img/icon_questionnaire_location-outline.svg");
                    break;

                case THOUGHT:
                    icon = new ThemeResource("img/icon_questionnaire_thought-outline.svg");
                    break;

				case ABOUTME:
					icon = new ThemeResource("img/icon_questionnaire_aboutme-outline.svg");
					break;

				case SEARCH:
					icon = new ThemeResource("img/icon_questionnaire_search-outline.svg");
					break;
            }

            return icon;
        }
	}
	
	public static final Comparator<Questionnaire> COMPARE_BY_CATEGORY_AND_SORTORDER = Comparator
			.comparing(Questionnaire::getRecommendationCategory,
					Comparator.nullsFirst(Comparator.naturalOrder()))
			.thenComparing(SortableEntity.COMPARATOR);
	
	@XmlAttribute
	@Column(name = "recommendation_category")
	@Enumerated(EnumType.STRING)
	private RecommendationCategory recommendationCategory;
	
	@XmlElementWrapper(name = "questionGroups")
	@XmlElement(name = "questionGroup")
	@OneToMany(mappedBy = "questionnaire")
	@OrderBy("sortOrder")
	private List<QuestionGroup> questionGroups = new ArrayList<>();
	
	@XmlAttribute
	@Enumerated(EnumType.STRING)
	private Icon icon;
	
	@XmlAttribute
	@Column(name = "sort_order")
	private int sortOrder;
	
	@XmlAttribute
	private boolean deleted = false;
	
	public RecommendationCategory getRecommendationCategory()
	{
		return recommendationCategory;
	}
	
	public void setRecommendationCategory(RecommendationCategory recommendationCategory)
	{
		this.recommendationCategory = recommendationCategory;
	}
	
	public List<QuestionGroup> getQuestionGroups()
	{
		return questionGroups;
	}
	
	public void setQuestionGroups(List<QuestionGroup> questionGroups)
	{
		this.questionGroups = questionGroups;
	}
	
	public void addQuestionGroup(QuestionGroup questionGroup)
	{
		questionGroup.setQuestionnaire(this);
		this.questionGroups.add(questionGroup);
	}
	
	public Icon getIcon()
	{
		return icon;
	}
	
	public void setIcon(Icon icon)
	{
		this.icon = icon;
	}
	
	public int getSortOrder()
	{
		return sortOrder;
	}
	
	public void setSortOrder(int sortOrder)
	{
		this.sortOrder = sortOrder;
	}
	
	public boolean isDeleted()
	{
		return deleted;
	}
	
	public void setDeleted(boolean deleted)
	{
		this.deleted = deleted;
	}
	
	public String getName()
	{
		return msg(BaseName.QUESTIONNAIRE_NAME);
	}
	
	public String getDescription()
	{
		return msg(BaseName.QUESTIONNAIRE_DESCRIPTION);
	}
	
	@Override
	protected BaseName doGetBaseName()
	{
		return BaseName.QUESTIONNAIRE_NAME;
	}
	
}

package de.binaerebauten.gleichklang.core.presenter.filter;

import de.binaerebauten.gleichklang.core.model.filter.AbstractFilter.FilterType;
import de.binaerebauten.gleichklang.core.model.filter.TemplateContext;
import de.binaerebauten.gleichklang.core.model.filter.TemplateFilter;
import de.binaerebauten.gleichklang.core.model.filter.UserFilter.UserFilterType;
import de.binaerebauten.gleichklang.core.model.locatable.LocatableEntity;
import de.binaerebauten.gleichklang.core.model.questionnaire.Choice;
import de.binaerebauten.gleichklang.core.model.questionnaire.ChoiceQuestion;
import de.binaerebauten.gleichklang.core.model.questionnaire.NumberQuestion;
import de.binaerebauten.gleichklang.core.model.questionnaire.TextQuestion;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.service.FilterControlService;
import de.binaerebauten.gleichklang.core.view.component.FilterControlComponent.FilterControlFeature;
import de.binaerebauten.gleichklang.core.view.component.FilterControlComponent.FilterControlHandler;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.*;

public class DefaultFilterControlHandler implements FilterControlHandler
{
	private final FilterControlService filterControlService;
	private final EnumSet<FilterControlFeature> filterControlFeatures;
	private final EnumSet<UserFilterType> userFilterTypes;
	private final EnumSet<FilterType> filterTypes;
	private final EnumSet<UserFilterType> pinnedUserFilterTypes;
	private boolean considerSingleUsage = false;
	private TemplateContext templateContext;
	
	public DefaultFilterControlHandler(FilterControlService filterControlService)
	{
		Objects.requireNonNull(filterControlService);
		this.filterControlService = filterControlService;
		
		filterControlFeatures = EnumSet.allOf(FilterControlFeature.class);
		userFilterTypes = EnumSet.allOf(UserFilterType.class);
		filterTypes = EnumSet.of(FilterType.USER_FILTER);
		pinnedUserFilterTypes = EnumSet.noneOf(UserFilterType.class);
	}
	
	@Override
	public Page<User> getUsers(Specification<User> specification, Pageable pageable)
	{
		return filterControlService.getUsers(specification, pageable);
	}
	
	@Override
	public Collection<TemplateFilter> getTemplateFilters()
	{
		return filterControlService.getTemplateFilters(templateContext);
	}

	@Override
	public <T extends LocatableEntity> List<T> getLocatableEntities(Class<T> type, LocatableEntity parent)
	{
		return filterControlService.getLocatableEntities(type, parent);
	}

	@Override
	public List<Choice> getSexChoiceQuestionChoices()
	{
		return filterControlService.getSexChoiceQuestion();
	}
	
	@Override
	public List<ChoiceQuestion> getChoiceQuestions()
	{
		return filterControlService.getChoiceQuestions();
	}
	
	@Override
	public List<TextQuestion> getTextQuestions()
	{
		return filterControlService.getTextQuestions();
	}
	
	@Override
	public List<NumberQuestion> getNumberQuestions()
	{
		return filterControlService.getNumberQuestions();
	}
	
	@Override
	public EnumSet<FilterControlFeature> getActivatedFeatures()
	{
		return filterControlFeatures;
	}
	
	@Override
	public EnumSet<FilterType> getActivatedFilters()
	{
		return filterTypes;
	}
	
	@Override
	public EnumSet<UserFilterType> getActivatedUserFilters()
	{
		return userFilterTypes;
	}
	
	@Override
	public EnumSet<UserFilterType> getPinnedUserFilters()
	{
		return pinnedUserFilterTypes;
	}
	
	@Override
	public boolean considerSingleUsage()
	{
		return considerSingleUsage;
	}
	
	public void setConsiderSingleUsage(boolean considerSingleUsage)
	{
		this.considerSingleUsage = considerSingleUsage;
	}
	
	public void setFilterControlFeatures(FilterControlFeature... filterControlFeatures)
	{
		this.filterControlFeatures.clear();
		this.filterControlFeatures.addAll(Arrays.asList(filterControlFeatures));
	}

	public void addFilterControlFeatures(FilterControlFeature... filterControlFeatures)
	{
		this.filterControlFeatures.addAll(Arrays.asList(filterControlFeatures));
	}
	
	public void removeFilterControlFeatures(FilterControlFeature... filterControlFeatures)
	{
		Arrays.stream(filterControlFeatures).forEach(this.filterControlFeatures::remove);
	}
	
	public void setUserFilterTypes(UserFilterType... userFilterTypes)
	{
		this.userFilterTypes.clear();
		this.userFilterTypes.addAll(Arrays.asList(userFilterTypes));
		
		if (this.userFilterTypes.isEmpty())
			filterTypes.remove(FilterType.USER_FILTER);
		else filterTypes.add(FilterType.USER_FILTER);
	}
	
	public void setPinnedUserFilterTypes(UserFilterType... pinnedUserFilterTypes)
	{
		this.pinnedUserFilterTypes.clear();
		this.pinnedUserFilterTypes.addAll(Arrays.asList(pinnedUserFilterTypes));
	}
	
	public void setTemplateContext(TemplateContext templateContext)
	{
		this.templateContext = templateContext;

		if(templateContext == TemplateContext.RELATIONSHIP)
		{
			if(filterTypes !=null)
			filterTypes.remove(FilterType.TEMPLATE_FILTER);
			return;
		}

		if (filterControlService.getTemplateFilters(templateContext).isEmpty())
		{
			filterTypes.remove(FilterType.TEMPLATE_FILTER);		}
		else
		{
			filterTypes.add(FilterType.TEMPLATE_FILTER);
		}
	}
}

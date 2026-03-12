package de.binaerebauten.gleichklang.core.service;

import de.binaerebauten.gleichklang.core.model.NaturalKeyEntity.NaturalKey;
import de.binaerebauten.gleichklang.core.model.filter.TemplateContext;
import de.binaerebauten.gleichklang.core.model.filter.TemplateFilter;
import de.binaerebauten.gleichklang.core.model.locatable.LocatableEntity;
import de.binaerebauten.gleichklang.core.model.questionnaire.*;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.repository.FilterRepository;
import de.binaerebauten.gleichklang.core.repository.LocatableRepository;
import de.binaerebauten.gleichklang.core.repository.QuestionRepository;
import de.binaerebauten.gleichklang.core.repository.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Service
public class FilterControlService
{
	@Autowired
	private LocatableRepository locatableRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private QuestionRepository questionRepository;

	@Autowired
	private FilterRepository filterRepository;

	public <T extends LocatableEntity> List<T> getLocatableEntities(Class<T> type, LocatableEntity parent)
	{
		if (parent == null) return locatableRepository.findByType(type);
		return locatableRepository.findByParentAndType(parent, type);
	}

	public List<Choice> getSexChoiceQuestion()
	{
		final Question sexQuestion = questionRepository.findByNaturalKey(NaturalKey.SEX);
		if(sexQuestion instanceof ChoiceQuestion) return ((ChoiceQuestion) sexQuestion).getChoiceGroup().getChoices();
		return new ArrayList<>();
	}

	public List<ChoiceQuestion> getChoiceQuestions()
	{
		return questionRepository.findByType(ChoiceQuestion.class);
	}

	public List<TextQuestion> getTextQuestions()
	{
		return questionRepository.findByType(TextQuestion.class);
	}
	
	public List<NumberQuestion> getNumberQuestions()
	{
		return questionRepository.findByType(NumberQuestion.class);
	}

	public Page<User> getUsers(Specification<User> specification, Pageable pageable)
	{
		return userRepository.findAll(specification, pageable);
	}

	public Set<TemplateFilter> getTemplateFilters(TemplateContext templateContext)
	{
		if(templateContext == null) return Collections.emptySet();
		
		return filterRepository.findTemplateByContext(templateContext);
	}
}

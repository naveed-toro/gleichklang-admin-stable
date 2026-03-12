package de.binaerebauten.gleichklang.adminweb.service;

import de.binaerebauten.gleichklang.core.model.I18NEntity;
import de.binaerebauten.gleichklang.core.model.questionnaire.*;
import de.binaerebauten.gleichklang.core.repository.*;
import de.binaerebauten.gleichklang.core.service.validator.NotDeletableValidationException;
import de.binaerebauten.gleichklang.core.service.validator.UniqueValidationException;
import de.binaerebauten.gleichklang.core.service.validator.ValidationException;
import de.binaerebauten.gleichklang.core.utils.CheckedTransactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import javax.validation.ConstraintViolationException;
import java.util.Collection;
import java.util.Objects;

/**
 * This service provides methods to save questionaire related entities.
 */
@Service
public class QuestionnaireAdminService
{
	private final QuestionnaireRepository questionnaireRepository;
	private final QuestionRepository questionRepository;
	private final QuestionGroupRepository questionGroupRepository;
	private final ChoiceGroupRepository choiceGroupRepository;
	private final ActivatorRepository activatorRepository;
	
	private final TranslationService translationService;
	
	@Autowired
	public QuestionnaireAdminService(QuestionnaireRepository questionnaireRepository,
			QuestionRepository questionRepository, QuestionGroupRepository questionGroupRepository,
			ChoiceGroupRepository choiceGroupRepository, ActivatorRepository activatorRepository,
			TranslationService translationService)
	{
		Objects.requireNonNull(questionnaireRepository);
		Objects.requireNonNull(questionRepository);
		Objects.requireNonNull(questionGroupRepository);
		Objects.requireNonNull(choiceGroupRepository);
		Objects.requireNonNull(activatorRepository);
		
		Objects.requireNonNull(translationService);
		
		this.questionnaireRepository = questionnaireRepository;
		this.questionRepository = questionRepository;
		this.questionGroupRepository = questionGroupRepository;
		this.choiceGroupRepository = choiceGroupRepository;
		this.activatorRepository = activatorRepository;
		
		this.translationService = translationService;
	}
	
	@CheckedTransactional
	public void saveQuestionnaire(Questionnaire questionnaire, Collection<I18NEntity> i18nNames,
			Collection<I18NEntity> i18nDescriptions)
			throws ValidationException
	{
		try
		{
			if (questionnaire.getId() == null)
				questionnaire.setSortOrder(this.questionnaireRepository.findMaxSortOrder(null) + 1);
			
			translationService.saveAndFlush(i18nNames);
			translationService.saveAndFlush(i18nDescriptions);
			
			this.questionnaireRepository.saveAndFlush(questionnaire);
		}
		catch (final DataIntegrityViolationException e)
		{
			throw new UniqueValidationException(I18N.QUESTIONNAIREADMINSERVICE_VALIDATION_UNIQUEI18N.msg());
		}
		catch (final ConstraintViolationException e)
		{
			throw new ValidationException(e);
		}
	}
	
	@CheckedTransactional
	public void saveQuestionGroup(QuestionGroup questionGroup, Collection<I18NEntity> i18nNames,
			Collection<I18NEntity> i18nDescriptions)
			throws ValidationException
	{
		try
		{
			if (questionGroup.getId() == null || !questionGroupRepository.findOne(
					questionGroup.getId()).getQuestionnaire().equals(questionGroup.getQuestionnaire()))
				questionGroup.setSortOrder(this.questionGroupRepository.findMaxSortOrder(null) + 1);
			
			translationService.saveAndFlush(i18nNames);
			translationService.saveAndFlush(i18nDescriptions);
			
			this.questionGroupRepository.saveAndFlush(questionGroup);
		}
		catch (final DataIntegrityViolationException e)
		{
			throw new UniqueValidationException(I18N.QUESTIONNAIREADMINSERVICE_VALIDATION_UNIQUEI18N.msg());
		}
		catch (final ConstraintViolationException e)
		{
			throw new ValidationException(e);
		}
	}
	
	@CheckedTransactional
	public void saveQuestion(Question question, Collection<I18NEntity> i18NEntities) throws ValidationException
	{
		/* check for activator */
		if (question.getId() != null && question.isRequired())
		{
			if (activatorRepository.existsEnablesQuestion(question) ||
					activatorRepository.existsEnablesQuestionGroup(question.getQuestionGroup()) ||
					activatorRepository.existsEnablesQuestionnaire(question.getQuestionGroup().getQuestionnaire()))
			{
				throw new ValidationException(I18N.QUESTIONNAIREADMINSERVICE_VALIDATION_REQUIREDNOTPOSSIBLE.msg());
			}
		}
		
		try
		{
			if (question.getId() == null || !questionRepository.findOne(question.getId()).getQuestionGroup().equals(question.getQuestionGroup()))
			{
				question.setSortOrder(this.questionRepository.findMaxSortOrder(null) + 1);
			}
			
			translationService.saveAndFlush(i18NEntities);
			questionRepository.save(question);
		}
		catch (final DataIntegrityViolationException e)
		{
			throw new UniqueValidationException(I18N.QUESTIONNAIREADMINSERVICE_VALIDATION_UNIQUEI18N.msg());
		}
		catch (final ConstraintViolationException e)
		{
			throw new ValidationException(e);
		}
	}
	
	@CheckedTransactional
	public void saveChoiceGroup(ChoiceGroup choiceGroup, Collection<I18NEntity> i18nChoiceGroups, Collection<I18NEntity> i18NChoices) throws ValidationException
	{
		try
		{
			for (int i = 0; i < choiceGroup.getChoices().size(); i++)
			{
				choiceGroup.getChoices().get(i).setSortOrder(i);
			}
			
			translationService.saveAndFlush(i18nChoiceGroups);
			translationService.saveAndFlush(i18NChoices);
			
			this.choiceGroupRepository.saveAndFlush(choiceGroup);
			
		}
		catch (final DataIntegrityViolationException e)
		{
			throw new UniqueValidationException(e);
		}
		catch (final ConstraintViolationException e)
		{
			throw new ValidationException(e);
		}
	}
	
	@CheckedTransactional
	public void delete(Question question) throws NotDeletableValidationException
	{
		this.questionRepository.markAsDeleted(question);
	}
}

package de.binaerebauten.gleichklang.adminweb.presenter;

import de.binaerebauten.gleichklang.adminweb.service.QuestionnaireAdminService;
import de.binaerebauten.gleichklang.adminweb.view.QuestionnaireAdminView;
import de.binaerebauten.gleichklang.adminweb.view.QuestionnaireAdminView.QuestionnaireAdminViewListener;
import de.binaerebauten.gleichklang.adminweb.view.popup.ChoiceGroupPopup;
import de.binaerebauten.gleichklang.adminweb.view.popup.QuestionGroupPopup;
import de.binaerebauten.gleichklang.adminweb.view.popup.QuestionPopup;
import de.binaerebauten.gleichklang.adminweb.view.popup.QuestionnairePopup;
import de.binaerebauten.gleichklang.core.model.I18NEntity;
import de.binaerebauten.gleichklang.core.model.I18NEntity.BaseName;
import de.binaerebauten.gleichklang.core.model.questionnaire.*;
import de.binaerebauten.gleichklang.core.model.questionnaire.Question.QuestionType;
import de.binaerebauten.gleichklang.core.presenter.NavigatePresenter;
import de.binaerebauten.gleichklang.core.repository.*;
import de.binaerebauten.gleichklang.core.view.filter.DeletedFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.domain.Specification;

import java.util.*;

public class QuestionnaireAdminPresenter extends NavigatePresenter implements QuestionnaireAdminViewListener
{
	private static final Logger LOG = LoggerFactory.getLogger(QuestionnaireAdminPresenter.class);

	private final QuestionnaireAdminView view;

	private final QuestionnaireRepository questionnaireRepository;
	private final QuestionGroupRepository questionGroupRepository;
	private final QuestionRepository questionRepository;
	private final ChoiceGroupRepository choiceGroupRepository;
	private final I18NRepository i18nRepository;

	private final QuestionnaireAdminService questionnaireAdminService;

	private final Specification<ChoiceGroup> choiceGroupFilter;

	public QuestionnaireAdminPresenter(ApplicationContext ctx, QuestionnaireAdminView view)
	{
		super(view);
		
		this.view = view;

		questionnaireAdminService = ctx.getBean(QuestionnaireAdminService.class);
		questionnaireRepository = ctx.getBean(QuestionnaireRepository.class);
		questionGroupRepository = ctx.getBean(QuestionGroupRepository.class);
		questionRepository = ctx.getBean(QuestionRepository.class);
		choiceGroupRepository = ctx.getBean(ChoiceGroupRepository.class);
		i18nRepository = ctx.getBean(I18NRepository.class);
		choiceGroupFilter = new DeletedFilter<>();

		view.setMoveQuestionnaireHandler(questionnaireRepository::moveItems);
		view.setMoveQuestionGroupHandler(questionGroupRepository::moveItems);
		view.setMoveQuestionHandler(questionRepository::moveItems);
		view.setListener(this);
	}

	@Override
	public void enter(String parameters)
	{
		refreshView();
	}

	@Override
	public void newQuestionnaire()
	{
		final QuestionnairePopup popup = new QuestionnairePopup(questionnaireAdminService::saveQuestionnaire);
		popup.addCloseListener(e -> refreshView());
		tryOpenPopup(popup);
	}

	@Override
	public void editQuestionnaire(Questionnaire questionnaire)
	{
		final List<I18NEntity> i18nNames = i18nRepository.findByBaseNameAndKey(BaseName.QUESTIONNAIRE_NAME, questionnaire.getI18nKey());
		final List<I18NEntity> i18nDescriptions = i18nRepository.findByBaseNameAndKey(BaseName.QUESTIONNAIRE_DESCRIPTION, questionnaire.getI18nKey());

		final QuestionnairePopup popup = new QuestionnairePopup(questionnaire, i18nNames, i18nDescriptions, questionnaireAdminService::saveQuestionnaire);
		popup.addCloseListener(e -> refreshView());
		tryOpenPopup(popup);
	}

	@Override
	public void deleteQuestionnaire(Questionnaire questionnaire)
	{
		questionnaireRepository.markAsDeleted(questionnaire);
		refreshView();
	}

	@Override
	public void undeleteQuestionnaire(Questionnaire questionnaire)
	{
		questionnaireRepository.restoreDeleted(questionnaire);
		refreshView();
	}

	@Override
	public void newQuestionGroup(Questionnaire questionnaire)
	{
		final QuestionGroup questionGroup = new QuestionGroup();
		questionGroup.setQuestionnaire(questionnaire);

		final QuestionGroupPopup popup = new QuestionGroupPopup(questionGroup, null, null, questionnaireRepository.findAllNotDeleted(), questionnaireAdminService::saveQuestionGroup);
		popup.addCloseListener(e -> refreshView());
		tryOpenPopup(popup);
	}

	@Override
	public void editQuestionGroup(QuestionGroup item)
	{
		final List<I18NEntity> i18nNames = i18nRepository.findByBaseNameAndKey(BaseName.QUESTION_GROUP_NAME, item.getI18nKey());
		final List<I18NEntity> i18nDescriptions = i18nRepository.findByBaseNameAndKey(BaseName.QUESTION_GROUP_DESCRIPTION, item.getI18nKey());

		final QuestionGroupPopup popup = new QuestionGroupPopup(item, i18nNames, i18nDescriptions, questionnaireRepository.findAllNotDeleted(), questionnaireAdminService::saveQuestionGroup);
		popup.addCloseListener(e -> refreshView());
		tryOpenPopup(popup);
	}

	@Override
	public void deleteQuestionGroup(QuestionGroup item)
	{
		questionGroupRepository.markAsDeleted(item);
		refreshView();
	}

	@Override
	public void undeleteQuestionGroup(QuestionGroup questionGroup)
	{
		questionGroupRepository.restoreDeleted(questionGroup);
		refreshView();
	}

	@Override
	public void newQuestion(QuestionGroup questionGroup)
	{
		final List<Question> questions = new ArrayList<>();
		for (QuestionType questionType : QuestionType.values())
		{
			try
			{
				final Question question = questionType.getQuestionClass().newInstance();
				question.setQuestionGroup(questionGroup);
				questions.add(question);
			}
			catch (InstantiationException | IllegalAccessException e)
			{
				LOG.error("Can't instantiate Mapping", e);
			}
		}

		final QuestionPopup popup = new QuestionPopup(
				questions,
				questionGroupRepository.findAllNotDeleted(),
				choiceGroupRepository.findAll(choiceGroupFilter),
				questionnaireAdminService::saveQuestion);

		popup.addCloseListener(e -> refreshView());
		tryOpenPopup(popup);
	}

	@Override
	public void editQuestion(Question question)
	{
		final QuestionPopup popup = new QuestionPopup(
				question,
				i18nRepository.findByBaseNameAndKey(BaseName.QUESTION_NAME, question.getI18nKey()),
				i18nRepository.findByBaseNameAndKey(BaseName.QUESTION_DESCRIPTION, question.getI18nKey()),
				questionGroupRepository.findAllNotDeleted(),
				choiceGroupRepository.findAll(choiceGroupFilter),
				questionnaireAdminService::saveQuestion);

		popup.addCloseListener(e -> refreshView());
		tryOpenPopup(popup);
	}

	@Override
	public void deleteQuestion(Question question)
	{
		questionRepository.markAsDeleted(question);
		refreshView();
	}

	@Override
	public void undeleteQuestion(Question question)
	{
		questionRepository.restoreDeleted(question);
		refreshView();
	}

	@Override
	public void newChoiceGroup()
	{
		final ChoiceGroup choiceGroup = new ChoiceGroup();

		final ChoiceGroupPopup popup = new ChoiceGroupPopup(choiceGroup, new HashSet<>(), new HashMap<>(), questionnaireAdminService::saveChoiceGroup);
		popup.addCloseListener(e -> refreshView());
		tryOpenPopup(popup);
	}

	@Override
	public void editChoiceGroup(ChoiceGroup choiceGroup)
	{
		final Collection<I18NEntity> i18nChoiceGroups = i18nRepository.findByBaseNameAndKey(BaseName.CHOICE_GROUP, choiceGroup.getI18nKey());
		final Map<Choice, Collection<I18NEntity>> i18nMap = new HashMap<>();
		for (Choice choice : choiceGroup.getChoices())
		{
			i18nMap.put(choice, i18nRepository.findByBaseNameAndKey(BaseName.CHOICE_VALUE, choice.getI18nKey()));
		}

		final ChoiceGroupPopup popup = new ChoiceGroupPopup(choiceGroup, i18nChoiceGroups, i18nMap, questionnaireAdminService::saveChoiceGroup);
		popup.addCloseListener(e -> refreshView());
		tryOpenPopup(popup);
	}

	@Override
	public void deleteChoiceGroup(ChoiceGroup choiceGroup)
	{
		choiceGroupRepository.markAsDeleted(choiceGroup);
		refreshView();
	}

	@Override
	public void undeleteChoiceGroup(ChoiceGroup choiceGroup)
	{
		choiceGroupRepository.restoreDeleted(choiceGroup);
		refreshView();
	}

	private void refreshView()
	{
		view.setQuestionnaireHandler(questionnaireRepository::findAll);
		view.setQuestionGroupHandler(questionGroupRepository::findAll);
		view.setQuestionHandler(questionRepository::findAll);
		view.setChoiceGroupHandler(choiceGroupRepository::findAll);
	}
	
}

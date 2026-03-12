package de.binaerebauten.gleichklang.adminweb.presenter;

import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import de.binaerebauten.gleichklang.adminweb.service.SetupMatchingService;
import de.binaerebauten.gleichklang.adminweb.service.matching.ExplanationLog;
import de.binaerebauten.gleichklang.adminweb.service.matching.GenerateSuggestionService;
import de.binaerebauten.gleichklang.adminweb.service.matching.MatchingService;
import de.binaerebauten.gleichklang.adminweb.view.MatchingView;
import de.binaerebauten.gleichklang.adminweb.view.MatchingView.MatchingTab;
import de.binaerebauten.gleichklang.adminweb.view.popup.*;
import de.binaerebauten.gleichklang.core.model.matching.*;
import de.binaerebauten.gleichklang.core.model.matching.AbstractQuestionsMapping.MatcherType;
import de.binaerebauten.gleichklang.core.model.matching.Activator.ActivatorType;
import de.binaerebauten.gleichklang.core.model.matching.MatchStatistic.MatchingScope;
import de.binaerebauten.gleichklang.core.model.matching.MatrixValue.Strictness;
import de.binaerebauten.gleichklang.core.model.questionnaire.Choice;
import de.binaerebauten.gleichklang.core.model.questionnaire.Question;
import de.binaerebauten.gleichklang.core.model.questionnaire.QuestionGroup;
import de.binaerebauten.gleichklang.core.model.questionnaire.Questionnaire;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.presenter.NavigatePresenter;
import de.binaerebauten.gleichklang.core.presenter.filter.DefaultFilterControlHandler;
import de.binaerebauten.gleichklang.core.repository.*;
import de.binaerebauten.gleichklang.core.repository.matching.MatchRepository;
import de.binaerebauten.gleichklang.core.repository.matching.MatchStatisticRepository;
import de.binaerebauten.gleichklang.core.repository.user.UserRepository;
import de.binaerebauten.gleichklang.core.service.FilterControlService;
import de.binaerebauten.gleichklang.core.service.validator.ValidationException;
import de.binaerebauten.gleichklang.core.utils.filter.FilterSpecificationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.util.concurrent.ListenableFuture;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static de.binaerebauten.gleichklang.core.model.filter.TemplateContext.MATCH;
import static de.binaerebauten.gleichklang.core.model.filter.UserFilter.UserFilterType.ALIAS_FILTER;
import static de.binaerebauten.gleichklang.core.model.filter.UserFilter.UserFilterType.MAIL_FILTER;
import static de.binaerebauten.gleichklang.core.model.questionnaire.Question.Requirement.optionalRequirements;
import static de.binaerebauten.gleichklang.core.view.component.FilterControlComponent.FilterControlFeature.PREVIEW;

public class MatchingPresenter extends NavigatePresenter implements MatchingView.MatchingViewListener
{
	private static final Logger LOG = LoggerFactory.getLogger(MatchingPresenter.class);
	
	private final MatchingView view;
	private final MatrixRepository matrixRepository;
	private final MatchingService matchingService;
	private final GenerateSuggestionService generateSuggestionService;
	private final SetupMatchingService setupMatchingService;
	private final ChoiceGroupRepository choiceGroupRepository;
	private final QuestionMappingRepository questionMappingRepository;
	private final QuestionRepository questionRepository;
	private final QuestionGroupRepository questionGroupRepository;
	private final QuestionnaireRepository questionnaireRepository;
	private final ActivatorRepository activatorRepository;
	private final MatchRepository matchRepository;
	private final MatchStatisticRepository matchStatisticRepository;
	private final UserRepository userRepository;
	
	private final DefaultFilterControlHandler matchFilterControlHandler;
	
	private final FilterSpecificationBuilder filterSpecificationBuilder;
	
	public MatchingPresenter(ApplicationContext ctx, MatchingView view)
	{
		super(view);
		
		this.view = view;
		
		matchingService = ctx.getBean(MatchingService.class);
		generateSuggestionService = ctx.getBean(GenerateSuggestionService.class);
		setupMatchingService = ctx.getBean(SetupMatchingService.class);
		matrixRepository = ctx.getBean(MatrixRepository.class);
		choiceGroupRepository = ctx.getBean(ChoiceGroupRepository.class);
		questionMappingRepository = ctx.getBean(QuestionMappingRepository.class);
		questionRepository = ctx.getBean(QuestionRepository.class);
		questionGroupRepository = ctx.getBean(QuestionGroupRepository.class);
		questionnaireRepository = ctx.getBean(QuestionnaireRepository.class);
		activatorRepository = ctx.getBean(ActivatorRepository.class);
		matchRepository = ctx.getBean(MatchRepository.class);
		matchStatisticRepository = ctx.getBean(MatchStatisticRepository.class);
		filterSpecificationBuilder = ctx.getBean(FilterSpecificationBuilder.class);
		userRepository = ctx.getBean(UserRepository.class);
		
		final FilterControlService filterControlService = ctx.getBean(FilterControlService.class);
		
		matchFilterControlHandler = new DefaultFilterControlHandler(filterControlService);
		matchFilterControlHandler.setTemplateContext(MATCH);
		matchFilterControlHandler.setUserFilterTypes(ALIAS_FILTER, MAIL_FILTER);
		matchFilterControlHandler.removeFilterControlFeatures(PREVIEW);
		
		view.setAliasGetter(ctx.getBean(UserRepository.class)::findAliasById);
		view.setListener(this);
	}
	
	@Override
	public void enter(String parameters)
	{
		this.view.setAutoMatchingEnabled(matchingService.isAutoMatchingEnabled());
		this.view.setMatchFilterHandlerAndBuilder(matchFilterControlHandler, filterSpecificationBuilder);
		refreshView();
	}
	
	@Override
	public void newMatrix()
	{
		final NewMatrixPopup newMatrixPopup = new NewMatrixPopup(new MatchingMatrix(), choiceGroupRepository.findAllNotDeleted(), this::newMatchingMatrix);
		newMatrixPopup.addCloseListener(e -> refreshView());
		tryOpenPopup(newMatrixPopup);
	}
	
	private void newMatchingMatrix(MatchingMatrix matchingMatrix) throws ValidationException
	{
		generateMatrixValues(matchingMatrix);
		setupMatchingService.saveMatrix(matchingMatrix);
	}
	
	private void generateMatrixValues(MatchingMatrix matchingMatrix)
	{
		for (Choice targetChoice : matchingMatrix.getTargetChoiceGroup().getChoices())
		{
			for (Choice sourceChoice : matchingMatrix.getSourceChoiceGroup().getChoices())
			{
				if (!matchingMatrix.getMatrixValues().stream().anyMatch(matrixValue -> matrixValue.getSourceChoice().equals(sourceChoice) && matrixValue.getTargetChoice().equals(targetChoice)))
				{
					final MatrixValue matrixValue = new MatrixValue();
					matrixValue.setMatrix(matchingMatrix);
					matrixValue.setSourceChoice(sourceChoice);
					matrixValue.setTargetChoice(targetChoice);
					matrixValue.setStrictness(Strictness._1);
					matchingMatrix.getMatrixValues().add(matrixValue);
				}
			}
		}
	}

	@Override
	public void editMatrix(MatchingMatrix matchingMatrix)
	{
		if (matchingMatrix == null) return;

		generateMatrixValues(matchingMatrix);
		matchingMatrix.setChangeDate(LocalDateTime.now());
		final EditMatrixPopup editMatrixPopup = new EditMatrixPopup(matchingMatrix, setupMatchingService::saveMatrix);
		editMatrixPopup.addCloseListener(e -> refreshView());
		tryOpenPopup(editMatrixPopup);
	}
	
	@Override
	public void deleteMatrix(MatchingMatrix matchingMatrix)
	{
		try
		{
			setupMatchingService.deleteMatrix(matchingMatrix);
			refreshView();
		}
		catch (ValidationException e)
		{
			Notification.show(I18N.MATCHINGPRESENTER_NOTIFICATION_UNDELETABLEMATRIX.msg(), Type.ERROR_MESSAGE);
		}
	}
	
	@Override
	public void newQuestionMapping()
	{
		final Collection<AbstractQuestionsMapping> questionsMappings = new ArrayList<>();
		for (MatcherType matcherType : MatcherType.values())
		{
			try
			{
				questionsMappings.add(matcherType.getMappingClass().newInstance());
			}
			catch (InstantiationException | IllegalAccessException e)
			{
				LOG.error("Can't instantiate Mapping", e);
			}
		}
		
		final QuestionMappingPopup questionMappingPopup = new QuestionMappingPopup(questionsMappings, questionRepository.findAllNotDeleted(), matrixRepository.findAll(), setupMatchingService::saveQuestionsMapping);
		questionMappingPopup.addCloseListener(e -> refreshView());
		tryOpenPopup(questionMappingPopup);
	}
	
	@Override
	public void editQuestionMapping(AbstractQuestionsMapping questionsMapping)
	{
		final QuestionMappingPopup questionMappingPopup = new QuestionMappingPopup(questionsMapping, questionRepository.findAllNotDeleted(), matrixRepository.findAll(), setupMatchingService::saveQuestionsMapping);
		questionMappingPopup.addCloseListener(e -> refreshView());
		tryOpenPopup(questionMappingPopup);
	}
	
	@Override
	public void deleteQuestionMapping(AbstractQuestionsMapping questionMapping)
	{
		if (questionMapping == null) return;
		
		questionMappingRepository.delete(questionMapping);
		refreshView();
	}
	
	@Override
	public void newActivator()
	{
		final List<Activator> activators = new ArrayList<>();
		for (ActivatorType activatorType : ActivatorType.values())
		{
			try
			{
				activators.add(activatorType.getActivatorClass().newInstance());
			}
			catch (InstantiationException | IllegalAccessException e)
			{
				LOG.error("Can't instantiate Mapping", e);
			}
		}
		
		final List<Questionnaire> questionnaires = questionnaireRepository.findAllQuestionnaireForRequirement(optionalRequirements);
		final List<QuestionGroup> questionGroups = questionGroupRepository.findAllQuestionGroupsForRequirement(optionalRequirements);
		final List<Question> questions = questionRepository.findAllQuestionsForRequirement(optionalRequirements);
		final ActivatorPopup activatorPopup = new ActivatorPopup(activators, questionnaires, questionGroups, questions, questionRepository.findAllActivatingQuestions(), setupMatchingService::saveActivator);
		activatorPopup.addCloseListener(e -> refreshView());
		tryOpenPopup(activatorPopup);
	}
	
	@Override
	public void editActivator(Activator activator)
	{
		final List<Questionnaire> questionnaires = questionnaireRepository.findAllQuestionnaireForRequirement(optionalRequirements);
		final List<QuestionGroup> questionGroups = questionGroupRepository.findAllQuestionGroupsForRequirement(optionalRequirements);
		final List<Question> questions = questionRepository.findAllQuestionsForRequirement(optionalRequirements);
		final ActivatorPopup activatorPopup = new ActivatorPopup(activator, questionnaires, questionGroups, questions, questionRepository.findAllActivatingQuestions(), setupMatchingService::saveActivator);
		activatorPopup.addCloseListener(e -> refreshView());
		tryOpenPopup(activatorPopup);
	}
	
	@Override
	public void deleteActivator(Activator activator)
	{
		if (activator == null) return;
		
		activatorRepository.delete(activator);
		refreshView();
	}
	
	@Override
	public void startMatching()
	{
		view.disableMatching();
		final ListenableFuture<Void> future = matchingService.startMatching();
		Notification.show("Matching gestartet!");
		future.addCallback(result -> view.enableMatching(), result ->
		{
			Notification.show("Matching fehlgeschlagen!", Type.ERROR_MESSAGE);
			LOG.error("Matching failure", result);
			view.enableMatching();
		});
	}
	
	@Override
	public void startDebugMatching(Collection<String> userList)
	{
		final ExplanationLog explanationLog = new ExplanationLog();
		final List<Long> userIds = new ArrayList<>();
		
		for (String aliasMail : userList)
		{
			User user = userRepository.findByAlias(aliasMail);
			if (user == null)
			{
				user = userRepository.findByEmail(aliasMail);
			}
			if (user == null)
			{
				explanationLog.addDebugMessage(aliasMail + " not found!");
			}
			else
			{
				userIds.add(user.getId());
			}
		}
		
		if (!userIds.isEmpty())
			matchingService.startMatching(userIds, explanationLog);
		view.setMatchingDebugResult(explanationLog.getMessage());
	}
	
	@Override
	public void generateSuggestion()
	{
		generateSuggestionService.generateSuggestion();
		refreshView();
	}
	
	@Override
	public void openMatchStatistic(MatchStatistic matchStatistic)
	{
		final MatchStatisticPopup popup = new MatchStatisticPopup(matchStatistic);
		tryOpenPopup(popup);
	}
	
	@Override
	public void onTabSelected(MatchingTab selectedTab)
	{
		switch (selectedTab)
		{
			case MATRIX:
				this.view.setMatricesHandler(matrixRepository::findAll);
				break;
			case QUESTION_MAPPER:
				this.view.setQuestionMappingHandler(questionMappingRepository::findAll);
				break;
			case ACTIVATOR:
				this.view.setActivatorHandler(activatorRepository::findAll);
				break;
			case MATCH:
				this.view.setMatchHandler(matchRepository::findAll);
				break;
			case MATCH_STATISTIC:
				this.view.setMatchStatisticHandler(matchStatisticRepository::findAll);
		}
	}
	
	@Override
	public long getCountRelationships(MatchStatistic matchStatistic)
	{
		if (MatchingScope.SUGGESTION != matchStatistic.getMatchingScope())
			return 0;
		
		return generateSuggestionService.countRelationships(matchStatistic.getStartDate(), matchStatistic.getEndDate());
	}
	
	@Override
	public long getCountAffectedUsers(MatchStatistic matchStatistic)
	{
		if (MatchingScope.SUGGESTION != matchStatistic.getMatchingScope())
			return 0;
		
		return generateSuggestionService.countAffectedUsers(matchStatistic.getStartDate(), matchStatistic.getEndDate());
	}
	
	private void refreshView()
	{
		onTabSelected(view.getSelectedMatchingTab());
	}
}

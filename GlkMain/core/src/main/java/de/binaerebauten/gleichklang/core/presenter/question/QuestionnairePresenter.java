package de.binaerebauten.gleichklang.core.presenter.question;

import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinSession;
import de.binaerebauten.gleichklang.core.model.matching.Activator;
import de.binaerebauten.gleichklang.core.model.questionnaire.*;
import de.binaerebauten.gleichklang.core.presenter.NavigatePresenter;
import de.binaerebauten.gleichklang.core.service.AnswerService;
import de.binaerebauten.gleichklang.core.service.LocatableService;
import de.binaerebauten.gleichklang.core.service.validator.UniqueValidationException;
import de.binaerebauten.gleichklang.core.view.QuestionnaireView;
import de.binaerebauten.gleichklang.core.view.QuestionnaireView.QuestionnaireActivationListener;
import de.binaerebauten.gleichklang.core.view.component.MessageBox;
import de.binaerebauten.gleichklang.core.view.component.MessageBox.MessageBoxButtons;
import de.binaerebauten.gleichklang.core.view.component.MessageBox.MessageBoxStyle;
import de.binaerebauten.gleichklang.core.view.component.question.QuestionGroupComponent;
import org.springframework.context.ApplicationContext;

import java.util.*;
import java.util.stream.Collectors;

public class QuestionnairePresenter extends NavigatePresenter implements QuestionnaireView.QuestionnaireViewListener
{
	/**
	 * Which level should be (de)activated on direct change (before save)
	 */
	public enum ActivatorLevel
	{
		QUESTION, //only activate questions inside the current question group
		QUESTION_GROUP, //only activate question_groups inside the current questionnaire + all questions
		QUESTIONNAIRE //allow switching to new questionnaire without saving
	}
	
	private final Questionnaire questionnaire;
	private final QuestionnaireView questionnaireView;
	private final Map<QuestionGroup, QuestionGroupComponent> questionGroupMap = new HashMap<>();
	private final QuestionnaireActivationListener questionnaireActivationListener;
	private final QuestionnaireActivation activation;
	private final AnswerService answerService;
	private final boolean withAdminVisible;
	private final LocatableService locatableService;
	private final ActivatorLevel activatorLevel;
	
	public QuestionnairePresenter(ApplicationContext ctx,
			QuestionnaireActivationListener questionnaireActivationListener,
			QuestionnaireActivation activation,
			QuestionnaireView view,
			boolean withAdminVisible,
			ActivatorLevel activatorLevel)
	{
		super(view);
		
		this.activatorLevel = activatorLevel;
		this.questionnaire = view.getQuestionnaire();
		this.questionnaireActivationListener = questionnaireActivationListener;
		this.questionnaireView = view;
		this.activation = activation;
		this.withAdminVisible = withAdminVisible;
		
		this.locatableService = ctx.getBean(LocatableService.class);
		this.answerService = ctx.getBean(AnswerService.class);
		
		this.questionnaireView.setListener(this);
	}
	
	@Override
	public void enter(String parameters)
	{
		createSubViews();
	}
	
	@Override
	public void createSubViews()
	{
		this.questionnaireView.removeAllQuestionGroups();
		questionGroupMap.clear();
		
		final QuestionnaireAnswers questionnaireAnswers = answerService.getQuestionnaireWithAnswers(questionnaire, activation, withAdminVisible);
		final List<QuestionGroup> questionGroups = questionnaireAnswers.getQuestionGroups();

		QuestionGroupComponent questionGroupTemp=null;
		for (QuestionGroup questionGroup : questionGroups)
		{
			final QuestionGroupComponent questionGroupComponent = new QuestionGroupComponent(questionGroup, questionnaireAnswers.getAnswers(questionGroup), locatableService, activation);
			questionGroupComponent.setActivationListener(this::onChangedActivators);
			
			questionnaireView.addQuestionGroupComponent(questionGroupComponent);
			questionGroupTemp=questionGroupComponent;
			questionGroupMap.put(questionGroup, questionGroupComponent);
		}



		////////Corona Component changes //////////
		VaadinRequest vaadinRequest = VaadinService.getCurrentRequest();
		String coronaComponent=(String) vaadinRequest.getAttribute("CoronaComponent");
		if(coronaComponent!=null)
		{
			questionGroupMap.entrySet().forEach(questionGroupQuestionGroupComponentEntry -> {
				if (questionGroupQuestionGroupComponentEntry.getKey().getI18nKey().equals("kontaktliste")) {
					this.questionnaireView.getQuestionGroupContainer().setTab(questionGroupQuestionGroupComponentEntry.getValue());
				}
			});
		}
		////////Corona Component changes //////////

		updateQuestionVisibility();
		updateQuestionGroupVisibility();
	}
	
	private void updateQuestionVisibility()
	{
		final List<Question> questions = activation.getActivatableQuestions().stream()
				.filter(question -> questionGroupMap.containsKey(question.getQuestionGroup()))
				.collect(Collectors.toList());
		
		for (Question question : questions)
		{
			final boolean enabled = activation.isEnabledQuestion(question);
			final QuestionGroupComponent questionGroupComponent = questionGroupMap.get(question.getQuestionGroup());
			questionGroupComponent.setQuestionActivated(question, enabled);
		}
	}
	
	private void updateQuestionGroupVisibility()
	{
		final List<QuestionGroup> questionGroups = new ArrayList<>(activation.getActivatableQuestionGroups());
		questionGroups.retainAll(questionGroupMap.keySet());
		
		for (QuestionGroup questionGroup : questionGroups)
		{
			final boolean enabled = activation.isEnabledQuestionGroup(questionGroup);
			final QuestionGroupComponent questionGroupComponent = questionGroupMap.get(questionGroup);
			this.questionnaireView.setQuestionGroupActivated(questionGroupComponent, enabled);
		}
	}
	
	private void updateQuestionnaireVisibility()
	{
		final List<Questionnaire> questionnaires = new ArrayList<>(activation.getActivatableQuestionnaires());
		
		for (Questionnaire questionnaire : questionnaires)
		{
			final boolean enabled = activation.isEnabledQuestionnaire(questionnaire);
			questionnaireActivationListener.activateQuestionnaire(questionnaire, enabled);
		}
	}
	
	public QuestionnaireView getQuestionnaireView()
	{
		return questionnaireView;
	}
	
	@Override
	public void save(List<Answer> answers)
	{
		try
		{
			answerService.save(answers);
		}
		catch (UniqueValidationException e)
		{
			MessageBox.show("Fehler beim Speichern", MessageBoxButtons.OK, MessageBoxStyle.ATTENTION, null);
			return;
		}
		
		updateQuestionVisibility();
		updateQuestionGroupVisibility();
		updateQuestionnaireVisibility();
	}
	
	private void onChangedActivators(Collection<Activator> changedActivators)
	{
		switch(activatorLevel)
		{
			case QUESTION:
				// already done in {@link QuestionGroupComponent#onActivationChanged}
				break;
			case QUESTION_GROUP:
				updateQuestionVisibility(); // also for questions in other question_groups
				updateQuestionGroupVisibility();
				break;
			case QUESTIONNAIRE:
				updateQuestionVisibility(); // also for questions in other question_groups
				updateQuestionGroupVisibility();
				updateQuestionnaireVisibility();
				break;
		}
	}
}

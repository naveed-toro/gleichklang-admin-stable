package de.binaerebauten.gleichklang.memberweb.view.component;

import com.google.common.collect.Multimap;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.*;
import de.binaerebauten.gleichklang.core.model.questionnaire.Question.Requirement;
import de.binaerebauten.gleichklang.core.model.questionnaire.Questionnaire;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;
import de.binaerebauten.gleichklang.memberweb.view.I18N;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by rgoerner on 20.05.16.
 */
public class IncompleteQuestionnaireComponent extends CustomComponent
{
	public interface ShowIncompleteQuestionsHandler
	{
		void onQuestionnaireClicked(Questionnaire incompleteQuestionnaire, Collection<Requirement> requirements);
	}
	
	private final ShowIncompleteQuestionsHandler questionsHandler;
	private final VerticalLayout wrapper;
	
	private VerticalLayout requiredQuestionsLayout;
	
	private int nrOfIncompleteQuestionnaires;
	
	public IncompleteQuestionnaireComponent(ShowIncompleteQuestionsHandler handler)
	{
		questionsHandler = handler;
		
		wrapper = new VerticalLayout();
		wrapper.setSizeFull();
		setCompositionRoot(wrapper);
		
		wrapper.addComponent(createHeader());
		
		nrOfIncompleteQuestionnaires = 0;
	}
	
	private VerticalLayout createHeader()
	{
		final VerticalLayout headerWrapper = new VerticalLayout();
		headerWrapper.setSizeFull();
		
		final HorizontalLayout container = new HorizontalLayout();
		container.setSizeFull();
		
		final HorizontalLayout headerText = new HorizontalLayout();
		headerText.addComponent(new Label(I18N.INCOMPLETE_QUESTIONS_LABEL.msg()));
		headerText.setStyleName(CssStyle.QUESTIONNAIRE_HEADER.getStyleName());
		
		container.addComponent(headerText);
		
		headerWrapper.addComponents(container);
		
		return headerWrapper;
	}
	
	private VerticalLayout createCarousel()
	{
		final VerticalLayout carouselLayout = new VerticalLayout();
		carouselLayout.setSizeFull();
		
		final VerticalLayout questionnaireWrapper = new VerticalLayout();
		
		requiredQuestionsLayout = new VerticalLayout();
		requiredQuestionsLayout.addStyleName(CssStyle.REQUIRED.getStyleName());
		requiredQuestionsLayout.setCaption(I18N.INCOMPLETE_QUESTIONS_REQUIRED_LABEL.msg());
		
		questionnaireWrapper.addComponent(requiredQuestionsLayout);
		
		carouselLayout.addComponents(questionnaireWrapper);
		
		return carouselLayout;
	}
	
	public VerticalLayout createIncompleteQuestionnaires(Multimap<Questionnaire, Requirement> incompleteQuestionnaires)
	{
		wrapper.removeAllComponents();
		wrapper.addComponent(createHeader());
		wrapper.setStyleName(CssStyle.QUESTIONNAIRE_CAROUSEL.getStyleName());
		
		final Panel incompleteQuestionnairesPanel = createIncompleteQuestionnairesPanel(incompleteQuestionnaires);
		wrapper.addComponent(incompleteQuestionnairesPanel);
		
		return wrapper;
		
	}
	
	private Panel createIncompleteQuestionnairesPanel(Multimap<Questionnaire, Requirement> incompleteQuestionnaires)
	{
		Panel incompleteQuestionnairesPanel = new Panel();
		incompleteQuestionnairesPanel.setSizeFull();
		
		nrOfIncompleteQuestionnaires = 0;
		
		final VerticalLayout carouselContainer = createCarousel();
		
		List<Questionnaire> questionnaires = incompleteQuestionnaires.keySet().stream()
				.sorted(Questionnaire.COMPARATOR).collect(Collectors.toList());
		
		for (Questionnaire incompleteQuestionnaire : questionnaires)
		{
			Collection<Requirement> values = incompleteQuestionnaires.get(incompleteQuestionnaire);
			final List<Requirement> requirements = values.stream().filter(value -> Requirement.REQUIRED.equals(value) || Requirement.IMPORTANT.equals(value)).collect(Collectors.toList());
			
			if (!requirements.isEmpty())
			{
				addIncompleteRow(incompleteQuestionnaire, requirements);
				nrOfIncompleteQuestionnaires++;
			}
		}
		incompleteQuestionnairesPanel.setContent(carouselContainer);
		
		if (requiredQuestionsLayout.getComponentCount() == 0)
		{
			wrapper.removeAllComponents();
			wrapper.setVisible(false);
		}
		
		return incompleteQuestionnairesPanel;
	}
	
	private HorizontalLayout addIncompleteRow(Questionnaire incompleteQuestionnaire, Collection<Requirement> requirements)
	{
		HorizontalLayout layout = new HorizontalLayout();
		
		Component incompleteButton = getIncompleteButton(incompleteQuestionnaire, requirements);
		
		requiredQuestionsLayout.addComponent(incompleteButton);
		
		return layout;
	}
	
	private Button getIncompleteButton(Questionnaire incompleteQuestionnaire, Collection<Requirement> requirements)
	{
		Button incompleteButton = new Button(incompleteQuestionnaire.getName());
		incompleteButton.setData(incompleteQuestionnaire);
		incompleteButton.setStyleName(CssStyle.TEXT_BUTTON.getStyleName());
		incompleteButton.setIcon(FontAwesome.QUESTION_CIRCLE);
		
		incompleteButton.addClickListener(event -> showIncompleteAnswers(incompleteQuestionnaire, requirements));
		return incompleteButton;
	}
	
	private void showIncompleteAnswers(Questionnaire incompleteQuestionnaire, Collection<Requirement> requirements)
	{
		questionsHandler.onQuestionnaireClicked(incompleteQuestionnaire, requirements);
	}
	
	public int getNrOfIncompleteQuestionnaires()
	{
		return this.nrOfIncompleteQuestionnaires;
	}
}

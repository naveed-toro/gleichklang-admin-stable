package de.binaerebauten.gleichklang.core.view.component.question;

import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.VerticalLayout;
import de.binaerebauten.gleichklang.core.model.questionnaire.Answer;
import de.binaerebauten.gleichklang.core.view.component.validator.SaveHelper;
import de.binaerebauten.gleichklang.core.view.commit_strategy.ValidationStrategy;
import de.binaerebauten.gleichklang.core.view.commit_strategy.DefaultValidationStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class QuestionGroupVerticalLayout extends CustomComponent implements QuestionGroupContainer
{
	private final List<QuestionGroupComponent> questionGroupComponents = new ArrayList<>();
	private final ComponentContainer componentContainer = new VerticalLayout();
	private final SaveHelper saveHelper;
	private SaveListener saveListener = null;
	
	/**
	 * Using without any including saveHelper
	 */
	public QuestionGroupVerticalLayout()
	{
		this(new DefaultValidationStrategy(), false);
		saveHelper.getValidationComponent().setVisible(false);
	}
	
	public QuestionGroupVerticalLayout(ValidationStrategy validationStrategy, boolean withSaveButton)
	{
		Objects.requireNonNull(validationStrategy);
		
		saveHelper = new SaveHelper(this::save);
		saveHelper.getValidationComponent().setValidationStrategy(validationStrategy);
		saveHelper.getSaveButton().setVisible(withSaveButton);
		
		final VerticalLayout layout = new VerticalLayout();
		
		layout.addComponent(saveHelper.getValidationComponent());
		layout.addComponent(componentContainer);
		layout.addComponent(saveHelper.getSaveButton());
				
		setCompositionRoot(layout);
	}
	
	@Override
	public void addQuestionGroupComponent(QuestionGroupComponent questionGroupComponent, String caption)
	{
		componentContainer.addComponent(questionGroupComponent);
		questionGroupComponents.add(questionGroupComponent);
		saveHelper.addFields(questionGroupComponent.getFields());
	}

	@Override
	public void setQuestionGroupActivated(QuestionGroupComponent questionGroupView, boolean activated)
	{
		questionGroupView.setVisible(activated);
	}

	@Override
	public void setTab(QuestionGroupComponent questionGroupView)
	{

	}
	
	@Override
	public void removeAllComponents()
	{
		questionGroupComponents.clear();
		componentContainer.removeAllComponents();
		saveHelper.removeAllFields();
	}
	
	private List<Answer> getAnswers()
	{
		return questionGroupComponents.stream().flatMap(v -> v.getAnswers().stream()).collect(Collectors.toList());
	}
	
	@Override
	public void quickRegister(String value)
	{
		questionGroupComponents.forEach(v -> v.quickRegister(value));
	}
	
	@Override
	public void setSaveListener(SaveListener saveListener)
	{
		this.saveListener = saveListener;
	}

	@Override
	public void setTabsVisible(boolean visible) {

	}

	private void save()
	{
		if(saveListener != null) saveListener.save(getAnswers());
	}
	
	@Override
	public void saveComplete(SaveResultListener saveResultListener)
	{
		saveHelper.saveComplete(saveResultListener);
	}
}

package de.binaerebauten.gleichklang.core.view.component.question;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.SelectedTabChangeListener;
import com.vaadin.ui.VerticalLayout;
import de.binaerebauten.gleichklang.core.view.commit_strategy.DefaultValidationStrategy;
import de.binaerebauten.gleichklang.core.view.component.FooterCommandBar;
import de.binaerebauten.gleichklang.core.view.component.validator.SaveHelper;
import de.binaerebauten.gleichklang.core.view.component.validator.ValidationResult;

import java.util.*;

public class QuestionGroupTabSheet extends CustomComponent implements QuestionGroupContainer
{
	private class QuestionGroupTab extends CustomComponent
	{
		private final SaveHelper saveHelper;
		private final QuestionGroupComponent questionGroupComponent;
		
		public QuestionGroupTab(QuestionGroupComponent questionGroupComponent)
		{
			Objects.requireNonNull(questionGroupComponent);
			
			this.questionGroupComponent = questionGroupComponent;
			
			saveHelper = new SaveHelper(this::save);
			saveHelper.setCommitOnlyVisibleFields(false);
			saveHelper.addFields(questionGroupComponent.getFields());
			saveHelper.getValidationComponent().setValidationStrategy(validationStrategy);

			final VerticalLayout layout = new VerticalLayout();
			layout.setSpacing(true);

            final FooterCommandBar commandBar = new FooterCommandBar(saveHelper.getSaveButton());

			layout.addComponent(saveHelper.getValidationComponent());
			layout.addComponent(questionGroupComponent);
			layout.addComponent(commandBar);

			setCompositionRoot(layout);
		}
		
		private void save()
		{
			if(saveListener != null)
			{
				saveListener.save(questionGroupComponent.getAnswers());
			}
		}
	}
	
	private final DefaultValidationStrategy validationStrategy;
	private final Map<QuestionGroupComponent, QuestionGroupTab> questionGroupTabMap = new HashMap<>();
	private final TabSheet tabSheet;
	private final List<SelectedTabChangeListener> selectedTabChangeListeners = new ArrayList<>();
	private SaveListener saveListener = null;
	
	public QuestionGroupTabSheet()
	{
		this.validationStrategy = new DefaultValidationStrategy();
		
		tabSheet = new TabSheet();
		setCompositionRoot(tabSheet);
	}
	
	@Override
	public void addQuestionGroupComponent(QuestionGroupComponent questionGroupComponent, String caption)
	{
		if (questionGroupComponent == null || questionGroupTabMap.containsKey(questionGroupComponent))
			return;
		
		final QuestionGroupTab questionGroupTab = new QuestionGroupTab(questionGroupComponent);
		questionGroupTabMap.put(questionGroupComponent, questionGroupTab);
		
		selectedTabChangeListeners.forEach(tabSheet::removeSelectedTabChangeListener);
		tabSheet.addTab(questionGroupTab, caption);
		selectedTabChangeListeners.forEach(tabSheet::addSelectedTabChangeListener);
	}
	
	@Override
	public void setTab(QuestionGroupComponent questionGroupComponent)
	{
		if (questionGroupComponent == null || !questionGroupTabMap.containsKey(questionGroupComponent))
			return;

		tabSheet.setSelectedTab(questionGroupTabMap.get(questionGroupComponent));
	}
	
	@Override
	public void setQuestionGroupActivated(QuestionGroupComponent questionGroupComponent, boolean activated)
	{
		if (questionGroupComponent == null || !questionGroupTabMap.containsKey(questionGroupComponent))
			return;
		
		final TabSheet.Tab tab = tabSheet.getTab(questionGroupTabMap.get(questionGroupComponent));
		
		if (tab != null)
		{
			tab.setVisible(activated);
		}
	}
	
	public void addSelectedTabChangeListener(SelectedTabChangeListener listener)
	{
		if (listener == null) return;
		
		selectedTabChangeListeners.add(listener);
		tabSheet.addSelectedTabChangeListener(listener);
	}
	
	@Override
	public void removeAllComponents()
	{
		selectedTabChangeListeners.forEach(tabSheet::removeSelectedTabChangeListener);
		tabSheet.removeAllComponents();
		selectedTabChangeListeners.forEach(tabSheet::addSelectedTabChangeListener);
	}
	
	@Override
	public void quickRegister(String value)
	{
		questionGroupTabMap.keySet().forEach(v -> v.quickRegister(value));
	}
	
	public QuestionGroupComponent getSelectedTab()
	{
		return ((QuestionGroupTab)tabSheet.getSelectedTab()).questionGroupComponent;
	}
	
	@Override
	public void setSaveListener(SaveListener saveListener)
	{
		this.saveListener = saveListener;
	}

	@Override
	public void saveComplete(SaveResultListener saveResultListener)
	{
		final ValidationResult collectResult = new ValidationResult(validationStrategy);
		questionGroupTabMap.values().forEach(components -> components.saveHelper.saveComplete(collectResult::addValidationResult));
		if(saveResultListener != null) saveResultListener.saveResult(collectResult);
	}

	@Override
	public void setTabsVisible(boolean visible) {
		tabSheet.setTabsVisible(visible);
	}
}

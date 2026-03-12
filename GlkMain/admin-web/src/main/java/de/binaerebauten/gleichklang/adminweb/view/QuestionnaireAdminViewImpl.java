package de.binaerebauten.gleichklang.adminweb.view;

import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.VerticalLayout;
import de.binaerebauten.gleichklang.adminweb.view.QuestionnaireAdminView.QuestionnaireAdminViewListener;
import de.binaerebauten.gleichklang.core.model.questionnaire.*;
import de.binaerebauten.gleichklang.core.utils.DefaultI18N;
import de.binaerebauten.gleichklang.core.view.AbstractNavigateView;
import de.binaerebauten.gleichklang.core.view.component.LazyBeanItemContainer.LazyBeanFilteredItemsHandler;
import de.binaerebauten.gleichklang.core.view.component.LazyBeanTable;
import de.binaerebauten.gleichklang.core.view.component.TableControl;
import de.binaerebauten.gleichklang.core.view.component.TableControl.MoveHandler;
import de.binaerebauten.gleichklang.core.view.filter.SimpleAttributeFilter;

import java.util.stream.Collectors;

public class QuestionnaireAdminViewImpl extends AbstractNavigateView<QuestionnaireAdminViewListener> implements QuestionnaireAdminView
{
	private final TableControl<Questionnaire> questionnaireTableControl;
	private final TableControl<QuestionGroup> questionGroupTableControl;
	private final TableControl<Question> questionTableControl;
	private final TableControl<ChoiceGroup> choiceGroupTableControl;
	
	private final SimpleAttributeFilter<QuestionGroup, Questionnaire> questionGroupFilter;
	private final SimpleAttributeFilter<Question, QuestionGroup> questionFilter;
	
	public QuestionnaireAdminViewImpl()
	{
		questionGroupFilter = new SimpleAttributeFilter<>(QuestionGroup_.questionnaire);
		questionFilter = new SimpleAttributeFilter<>(Question_.questionGroup);
		
		questionnaireTableControl = createQuestionnaireAdministration();
		questionGroupTableControl = createQuestionGroupAdministration();
		questionTableControl = createQuestionAdministration();
		choiceGroupTableControl = createChoiceGroupAdministration();
		
		final TabSheet tabSheet = createTabSheet();
		
		final VerticalLayout layout = new VerticalLayout();
		layout.addComponent(tabSheet);
		layout.setSizeFull();
		layout.setMargin(true);
		layout.setSpacing(true);
		setCompositionRoot(layout);
	}
	
	private TabSheet createTabSheet()
	{
		final TabSheet tabSheet = new TabSheet();
		
		tabSheet.addTab(questionnaireTableControl, I18N.QUESTIONNAIRE_TAB_QUESTIONNAIRE.msg());
		
		final Tab questionGroupTab = tabSheet.addTab(questionGroupTableControl, I18N.QUESTIONNAIRE_TAB_QUESTIONGROUPS.msg());
		questionGroupTab.setVisible(false);
		questionGroupFilter.addValueChangeListener(value -> activateTab(tabSheet, questionGroupTab));
		
		final Tab questionTab = tabSheet.addTab(questionTableControl, I18N.QUESTIONNAIRE_TAB_QUESTIONS.msg());
		questionTab.setVisible(false);
		questionFilter.addValueChangeListener(value -> activateTab(tabSheet, questionTab));
		
		tabSheet.addTab(choiceGroupTableControl, I18N.QUESTIONNAIRE_TAB_CHOICEGROUP.msg());
		tabSheet.setWidth(97, Unit.PERCENTAGE);
		
		return tabSheet;
	}
	
	private void activateTab(TabSheet tabSheet, Tab tab)
	{
		tab.setVisible(true);
		tabSheet.setSelectedTab(tab);
	}
	
	private TableControl<ChoiceGroup> createChoiceGroupAdministration()
	{
		final TableControl<ChoiceGroup> tableControl = new TableControl<>(createChoiceGroupTable());
		tableControl.setMargin(true);
		
		tableControl.setNewCallback(() -> fireEvent(QuestionnaireAdminViewListener::newChoiceGroup));
		tableControl.setEditCallback(item -> fireEvent(eventAction -> eventAction.editChoiceGroup(item)));
		tableControl.setDeleteCallback(item -> fireEvent(eventAction -> eventAction.deleteChoiceGroup(item)));
		tableControl.setUndeleteCallback(item -> fireEvent(eventAction -> eventAction.undeleteChoiceGroup(item)));
		
		return tableControl;
	}
	
	private TableControl<QuestionGroup> createQuestionGroupAdministration()
	{
		final TableControl<QuestionGroup> tableControl = new TableControl<>(createQuestionGroupTable());
		tableControl.setMargin(true);
		
		tableControl.setNewCallback(() -> fireEvent(eventAction -> eventAction.newQuestionGroup(questionGroupFilter.getValue())));
		tableControl.setEditCallback(item -> fireEvent(eventAction -> eventAction.editQuestionGroup(item)));
		tableControl.setDeleteCallback(item -> fireEvent(eventAction -> eventAction.deleteQuestionGroup(item)));
		tableControl.setUndeleteCallback(item -> fireEvent(eventAction -> eventAction.undeleteQuestionGroup(item)));
		
		return tableControl;
	}
	
	private TableControl<Questionnaire> createQuestionnaireAdministration()
	{
		final TableControl<Questionnaire> tableControl = new TableControl<>(createQuestionnaireTable());
		tableControl.setMargin(true);
		
		tableControl.setNewCallback(() -> fireEvent(QuestionnaireAdminViewListener::newQuestionnaire));
		tableControl.setEditCallback(item -> fireEvent(eventAction -> eventAction.editQuestionnaire(item)));
		tableControl.setDeleteCallback(item -> fireEvent(eventAction -> eventAction.deleteQuestionnaire(item)));
		tableControl.setUndeleteCallback(item -> fireEvent(eventAction -> eventAction.undeleteQuestionnaire(item)));
		
		return tableControl;
	}
	
	private TableControl<Question> createQuestionAdministration()
	{
		final TableControl<Question> tableControl = new TableControl<>(createQuestionTable());
		tableControl.setMargin(true);
		
		tableControl.setNewCallback(() -> fireEvent(eventAction -> eventAction.newQuestion(questionFilter.getValue())));
		tableControl.setEditCallback(item -> fireEvent(eventAction -> eventAction.editQuestion(item)));
		tableControl.setDeleteCallback(item -> fireEvent(eventAction -> eventAction.deleteQuestion(item)));
		tableControl.setUndeleteCallback(item -> fireEvent(eventAction -> eventAction.undeleteQuestion(item)));
		
		return tableControl;
	}
	
	private LazyBeanTable<ChoiceGroup> createChoiceGroupTable()
	{
		final LazyBeanTable<ChoiceGroup> table = new LazyBeanTable<>();
		table.setSelectable(true);
		table.setMultiSelect(true);
		table.setSizeFull();
		
		table.addGeneratedColumn(I18N.QUESTIONNAIRE_HEADER_NAME.msg(), DefaultI18N::getName);
		table.addGeneratedColumn(I18N.QUESTIONNAIRE_CHOICEGROUP_TABLE_VALUES.msg(), (choiceGroup) -> choiceGroup.getChoices().stream().map(Choice::getName).collect(Collectors.joining(" ")));
		table.addContainerProperty(I18N.TABLE_HEADER_CREATEDATE.msg(), ChoiceGroup_.createDate);
		table.addContainerProperty(I18N.TABLE_HEADER_CHANGEDATE.msg(), ChoiceGroup_.changeDate);
		table.addContainerProperty(I18N.TABLE_HEADER_DELETED.msg(), ChoiceGroup_.deleted);
		
		return table;
	}
	
	private LazyBeanTable<QuestionGroup> createQuestionGroupTable()
	{
		final LazyBeanTable<QuestionGroup> table = new LazyBeanTable<>();
		table.setSelectable(true);
		table.setMultiSelect(true);
		table.setSizeFull();
		
		table.addContainerProperty(I18N.QUESTIONNAIRE_HEADER_KEY.msg(), QuestionGroup_.i18nKey);
		table.addGeneratedColumn(I18N.QUESTIONNAIRE_HEADER_NAME.msg(), QuestionGroup::getName);
		table.addGeneratedColumn(I18N.QUESTIONNAIRE_HEADER_QUESTIONNAIRE.msg(), (questionGroup) -> questionGroup.getQuestionnaire().getName());
		table.addContainerProperty(I18N.TABLE_HEADER_CREATEDATE.msg(), QuestionGroup_.createDate);
		table.addContainerProperty(I18N.TABLE_HEADER_CHANGEDATE.msg(), QuestionGroup_.changeDate);
		table.addContainerProperty(I18N.TABLE_HEADER_DELETED.msg(), QuestionGroup_.deleted);
		
		table.addItemClickListener(questionFilter::setValue, true);
		
		questionGroupFilter.setItemComponent(table);
		
		return table;
	}
	
	private LazyBeanTable<Questionnaire> createQuestionnaireTable()
	{
		final LazyBeanTable<Questionnaire> table = new LazyBeanTable<>();
		table.setSelectable(true);
		table.setMultiSelect(true);
		table.setSizeFull();
		
		table.addContainerProperty(I18N.QUESTIONNAIRE_HEADER_KEY.msg(), Questionnaire_.i18nKey);
		table.addContainerProperty(I18N.QUESTIONNAIRE_HEADER_RECOMMENDATIONCATEGORY.msg(), Questionnaire_.recommendationCategory);
		table.addGeneratedColumn(I18N.QUESTIONNAIRE_HEADER_NAME.msg(), (source, itemId, columnId) -> itemId.getName());
		table.addContainerProperty(I18N.TABLE_HEADER_CREATEDATE.msg(), Questionnaire_.createDate);
		table.addContainerProperty(I18N.TABLE_HEADER_CHANGEDATE.msg(), Questionnaire_.changeDate);
		table.addContainerProperty(I18N.TABLE_HEADER_DELETED.msg(), Questionnaire_.deleted);
		
		table.addItemClickListener(questionGroupFilter::setValue, true);
		
		return table;
	}
	
	private LazyBeanTable<Question> createQuestionTable()
	{
		final LazyBeanTable<Question> table = new LazyBeanTable<>();
		table.setSelectable(true);
		table.setMultiSelect(true);
		table.setSizeFull();
		
		table.addContainerProperty(I18N.QUESTIONNAIRE_HEADER_KEY.msg(), Question_.i18nKey);
		table.addGeneratedColumn(I18N.QUESTIONNAIRE_HEADER_NAME.msg(), (source, itemId, columnId) -> itemId.msg());
		table.addGeneratedColumn(I18N.QUESTIONNAIRE_HEADER_QUESTIONGROUP.msg(), (source, itemId, columnId) -> itemId.getQuestionGroup().getName());
		table.addContainerProperty(I18N.TABLE_HEADER_CREATEDATE.msg(), Question_.createDate);
		table.addContainerProperty(I18N.TABLE_HEADER_CHANGEDATE.msg(), Question_.changeDate);
		table.addContainerProperty(I18N.TABLE_HEADER_DELETED.msg(), Question_.deleted);
		
		questionFilter.setItemComponent(table);
		
		return table;
	}
	
	@Override
	public void setQuestionGroupHandler(LazyBeanFilteredItemsHandler<QuestionGroup> handler)
	{
		questionGroupTableControl.getTable().setHandler(handler);
	}
	
	@Override
	public void setQuestionnaireHandler(LazyBeanFilteredItemsHandler<Questionnaire> handler)
	{
		questionnaireTableControl.getTable().setHandler(handler);
	}
	
	@Override
	public void setQuestionHandler(LazyBeanFilteredItemsHandler<Question> handler)
	{
		questionTableControl.getTable().setHandler(handler);
	}
	
	@Override
	public void setChoiceGroupHandler(LazyBeanFilteredItemsHandler<ChoiceGroup> handler)
	{
		choiceGroupTableControl.getTable().setHandler(handler);
	}
	
	@Override
	public void setMoveQuestionnaireHandler(MoveHandler<Questionnaire> moveItemsHandler)
	{
		questionnaireTableControl.setMoveHandler(moveItemsHandler);
	}
	
	@Override
	public void setMoveQuestionGroupHandler(MoveHandler<QuestionGroup> moveItemsHandler)
	{
		questionGroupTableControl.setMoveHandler(moveItemsHandler);
	}
	
	@Override
	public void setMoveQuestionHandler(MoveHandler<Question> moveItemsHandler)
	{
		questionTableControl.setMoveHandler(moveItemsHandler);
	}
}

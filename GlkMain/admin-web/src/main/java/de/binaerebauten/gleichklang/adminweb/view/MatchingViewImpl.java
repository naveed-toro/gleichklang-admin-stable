package de.binaerebauten.gleichklang.adminweb.view;

import com.google.common.base.Strings;
import com.google.common.collect.BiMap;
import com.google.common.collect.EnumHashBiMap;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.*;
import com.vaadin.ui.TabSheet.Tab;
import de.binaerebauten.gleichklang.adminweb.view.MatchingView.MatchingViewListener;
import de.binaerebauten.gleichklang.adminweb.view.util.RecommendationCategoryVisitor;
import de.binaerebauten.gleichklang.core.model.BaseEntity;
import de.binaerebauten.gleichklang.core.model.BaseEntity_;
import de.binaerebauten.gleichklang.core.model.matching.*;
import de.binaerebauten.gleichklang.core.model.matching.AbstractQuestionsMapping.MatcherType;
import de.binaerebauten.gleichklang.core.model.matching.Activator.ActivatorType;
import de.binaerebauten.gleichklang.core.model.questionnaire.Choice;
import de.binaerebauten.gleichklang.core.model.questionnaire.Question;
import de.binaerebauten.gleichklang.core.model.questionnaire.QuestionGroup;
import de.binaerebauten.gleichklang.core.model.questionnaire.Questionnaire;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.presenter.filter.DefaultFilterControlHandler;
import de.binaerebauten.gleichklang.core.utils.filter.FilterSpecificationBuilder;
import de.binaerebauten.gleichklang.core.view.AbstractNavigateView;
import de.binaerebauten.gleichklang.core.view.component.*;
import de.binaerebauten.gleichklang.core.view.component.LazyBeanItemContainer.LazyBeanFilteredItemsHandler;
import de.binaerebauten.gleichklang.core.view.component.LazyBeanItemContainer.LazyBeanItemsHandler;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;
import de.binaerebauten.gleichklang.core.view.filter.AbstractCategoryFilter;
import de.binaerebauten.gleichklang.core.view.filter.SimpleUserFilter.SimpleUserIdFilter;
import org.springframework.data.jpa.domain.Specification;

import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("serial")
public class MatchingViewImpl extends AbstractNavigateView<MatchingViewListener> implements MatchingView
{
	
	private static class MatchCategoryFilter extends AbstractCategoryFilter<Match>
	{
		@Override
		protected Specification<Match> createSingleFilter(RecommendationCategory category)
		{
			return (root, query, cb) -> cb.equal(root.get(Match_.category), category);
		}
	}
	
	private final LazyBeanTable<MatchingMatrix> matrixTable;
	private final LazyBeanTable<AbstractQuestionsMapping> mappingTable;
	private final LazyBeanTable<Activator> activatorTable;
	private final LazyBeanTable<Match> matchTable;
	private final LazyBeanPagingComponent<MatchStatistic> matchStatisticTable;
	
	private final ComponentReplacer<FilterControlComponent> matchFilterComponent = new ComponentReplacer<>();
	private final SimpleUserIdFilter<Match> matchFilter;
	
	//match debugging
	private final Label matchResults = new Label();
	
	private final Button startMatchingButton;
	private final Component waitingComponent;
	private final Button generateSuggestionButton;
	private final BiMap<MatchingTab, Tab> tabs = EnumHashBiMap.create(MatchingTab.class);
	private final TabSheet tabSheet;
	private Label waitingLabel;
	
	private AliasGetter aliasGetter = null;
	
	public MatchingViewImpl()
	{
		startMatchingButton = createMatchingButton();
		waitingComponent = waitingNotification();
		generateSuggestionButton = createSuggestionButton();
		
		matrixTable = createMatrixTable();
		mappingTable = createMappingTable();
		activatorTable = createActivatorTable();
		matchTable = createMatchTable();
		matchStatisticTable = createMatchStatisticTable();
		
		matchFilter = new SimpleUserIdFilter<>(Arrays.asList(Match_.sourceUserId, Match_.targetUserId));
		matchFilter.setItemComponent(matchTable);
		
		tabSheet = createTabSheet();
		
		final VerticalLayout layout = new VerticalLayout();
		layout.setMargin(true);
		layout.setSpacing(true);
		layout.addComponent(tabSheet);
		layout.setSizeFull();
		setCompositionRoot(layout);
	}
	
	@Override
	public void setAliasGetter(AliasGetter aliasGetter)
	{
		this.aliasGetter = aliasGetter;
	}
	
	private Component waitingNotification()
	{
		HorizontalLayout horizontalLayout = new HorizontalLayout();
		
		Image waiting = new Image();
		waiting.setIcon(new ThemeResource("img/waiting.gif"));
		waiting.setHeight(50, Unit.PIXELS);
		
		waitingLabel = new Label();
		
		horizontalLayout.addComponents(waiting, waitingLabel);
		horizontalLayout.setVisible(false);
		
		return horizontalLayout;
	}
	
	private TabSheet createTabSheet()
	{
		final TabSheet tabSheet = new TabSheet();
		
		tabs.put(MatchingTab.MATRIX, tabSheet.addTab(createMatrixAdministration(), I18N.MATCHING_TAB_MATRIXADMINISTRATION.msg()));
		tabs.put(MatchingTab.QUESTION_MAPPER, tabSheet.addTab(createQuestionMapper(), I18N.MATCHING_TAB_QUESTIONMAPPER.msg()));
		tabs.put(MatchingTab.ACTIVATOR, tabSheet.addTab(createActivatorAdministration(), I18N.MATCHING_TAB_ACTIVATORADMINISTRATION.msg()));
		tabs.put(MatchingTab.MATCH, tabSheet.addTab(createMatchAdministration(), I18N.MATCHING_TAB_MATCHADMINISTRATION.msg()));
		tabs.put(MatchingTab.MATCH_STATISTIC, tabSheet.addTab(createMatchStatistic(), I18N.MATCHING_TAB_MATCHSTATISTIC.msg()));
		tabs.put(MatchingTab.MATCHING_DEBUGGING, tabSheet.addTab(createMatchingDebugging(), I18N.MATCHING_TAB_MATCHINGDEBUGGING.msg()));
		
		tabSheet.addSelectedTabChangeListener(event -> fireEvent(eventAction -> eventAction.onTabSelected(getSelectedMatchingTab())));
		tabSheet.setWidth(97, Unit.PERCENTAGE);
		
		return tabSheet;
	}
	
	private Component createMatchingDebugging()
	{
		final VerticalLayout layout = new VerticalLayout();
		layout.setMargin(true);
		layout.setSpacing(true);
		
		// user list
		final Collection<String> userList = new ArrayList<>();
		final VerticalLayout userLayout = new VerticalLayout();
		userLayout.setSpacing(true);
		
		// user input
		final HorizontalLayout userInputLayout = new HorizontalLayout();
		userInputLayout.setSpacing(true);
		
		final TextField userTextField = ComponentFactory.getInstance().createField(TextField.class);
		userTextField.setInputPrompt("Email or Alias");
		
		final Button addUser = new Button("+");
		addUser.addClickListener(event ->
		{
			final String value = userTextField.getValue();
			userTextField.setValue(null);
			if(Strings.isNullOrEmpty(value)) return;
			userList.add(value);
			
			// create user entry
			final HorizontalLayout userEntryLayout = new HorizontalLayout();
			userEntryLayout.setSpacing(true);
			
			final Button removeButton = new Button("-");
			removeButton.addClickListener(removeEvent ->
			{
				userList.remove(value);
				userLayout.removeComponent(userEntryLayout);
			});
			
			userEntryLayout.addComponent(new Label(value));
			userEntryLayout.addComponent(removeButton);
			
			userLayout.addComponent(userEntryLayout);
		});
		
		userInputLayout.addComponents(userTextField, addUser);
		
		// control buttons (start and reset)
		final HorizontalLayout controlLayout = new HorizontalLayout();
		controlLayout.setSpacing(true);
		
		final Button resetButton = new Button("Reset");
		resetButton.addClickListener(event ->
		{
			userList.clear();
			userLayout.removeAllComponents();
			matchResults.setValue(null);
		});
		
		final Button startButton = new Button("Start");
		startButton.addClickListener(event -> fireEvent(action -> action.startDebugMatching(userList)));
		
		controlLayout.addComponents(startButton, resetButton);
		
		// put layouts together
		layout.addComponents(userInputLayout, controlLayout, userLayout, matchResults);
		
		return layout;
	}
	
	@Override
	public void setMatchingDebugResult(String result)
	{
		matchResults.setValue(result);
	}
	
	private LazyBeanTable<Activator> createActivatorTable()
	{
		final LazyBeanTable<Activator> table = new LazyBeanTable<>();
		table.setSelectable(true);
		table.setSizeFull();
		
		table.addGeneratedColumn(I18N.ACTIVATOR_HEADER_ACTIVATORTYPE.msg(), (source, itemId, columnId) -> Arrays.stream(ActivatorType.values())
				.filter(matcherType -> matcherType.getActivatorClass().equals(itemId.getClass()))
				.findFirst()
				.get());
		
		table.addContainerProperty(I18N.ACTIVATOR_HEADER_NAME.msg(), Activator_.naturalKey);
		
		table.addGeneratedColumn(I18N.ACTIVATOR_HEADER_ACTIVATINGQUESTION.msg(), (source, itemId, columnId) -> getName(itemId.getActivatingQuestion()));
		
		table.addGeneratedColumn(I18N.ACTIVATOR_HEADER_ACTIVATINGCHOICEVALUES.msg(), (source, itemId, columnId) -> itemId.getActivatingChoices().stream().map(Choice::getName).collect(Collectors.joining(", ")));
		
		table.addGeneratedColumn(I18N.ACTIVATOR_HEADER_ENABLES.msg(), (source, itemId, columnId) ->
		{
			if (itemId instanceof QuestionActivator)
				return getName(((QuestionActivator) itemId).getEnablesQuestion());
			if (itemId instanceof QuestionnaireActivator)
				return getName(((QuestionnaireActivator) itemId).getEnablesQuestionnaire());
			if (itemId instanceof QuestionGroupActivator)
				return getName(((QuestionGroupActivator) itemId).getEnablesQuestionGroup());
			return "";
		});
		
		table.addContainerProperty(I18N.TABLE_HEADER_CREATEDATE.msg(), Activator_.createDate);
		table.addContainerProperty(I18N.TABLE_HEADER_CHANGEDATE.msg(), Activator_.changeDate);
		
		return table;
	}
	
	private boolean isInvalid(AbstractQuestionsMapping mapping)
	{
		if (mapping.accept(new RecommendationCategoryVisitor()) == null)
			return true;
		
		if (mapping instanceof ChoiceQuestionsMapping)
		{
			final ChoiceQuestionsMapping choiceQuestionsMapping = (ChoiceQuestionsMapping) mapping;
			return choiceQuestionsMapping.getSourceQuestion().isDeletedRecursive() ||
					choiceQuestionsMapping.getTargetQuestion().isDeletedRecursive();
		}
		
		if (mapping instanceof AffinityMapping)
		{
			final AffinityMapping affinityMapping = (AffinityMapping) mapping;
			return affinityMapping.getQuestions().stream().anyMatch(Question::isDeletedRecursive);
		}
		
		if (mapping instanceof NumberQuestionsMapping)
		{
			final NumberQuestionsMapping numberQuestionsMapping = (NumberQuestionsMapping) mapping;
			return numberQuestionsMapping.getFactQuestion().isDeletedRecursive() ||
					numberQuestionsMapping.getMinQuestion().isDeletedRecursive() ||
					numberQuestionsMapping.getMaxQuestion().isDeletedRecursive();
		}
		
		if (mapping instanceof AgeQuestionMapping)
		{
			final AgeQuestionMapping ageQuestionMapping = (AgeQuestionMapping) mapping;
			return ageQuestionMapping.getMinAgeQuestion().isDeletedRecursive() ||
					ageQuestionMapping.getMaxAgeQuestion().isDeletedRecursive();
		}
		
		if (mapping instanceof AvatarQuestionMapping)
		{
			final AvatarQuestionMapping avatarQuestionMapping = (AvatarQuestionMapping) mapping;
			return avatarQuestionMapping.getAvatarQuestion().isDeletedRecursive();
		}
		
		return false;
	}
	
	private String getName(Question question)
	{
		return question.isDeletedRecursive() ? "INVALID" : question.getName();
	}
	
	private String getName(QuestionGroup questionGroup)
	{
		return questionGroup.isDeletedRecursive() ? "INVALID" : questionGroup.getName();
	}
	
	private String getName(Questionnaire questionnaire)
	{
		return questionnaire.isDeleted() ? "INVALID" : questionnaire.getName();
	}
	
	private LazyBeanTable<AbstractQuestionsMapping> createMappingTable()
	{
		final LazyBeanTable<AbstractQuestionsMapping> table = new LazyBeanTable<>();
		table.setSelectable(true);
		table.setSizeFull();
		
		table.addGeneratedColumn("Recommendation Category", (source, itemId, columnId) ->
		{
			final EnumSet<RecommendationCategory> recommendationCategories = itemId.accept(new RecommendationCategoryVisitor());
			if (recommendationCategories == null) return "INVALID";
			return recommendationCategories.stream().map(Enum::toString).collect(Collectors.joining(", "));
		});
		
		table.addContainerProperty(I18N.MATCHINGMAPPING_HEADER_NAME.msg(), AbstractQuestionsMapping_.naturalKey);
		
		table.addGeneratedColumn(I18N.MATCHINGMAPPING_HEADER_MATRIX.msg(), (source, itemId, columnId) ->
		{
			if (itemId instanceof ChoiceQuestionsMapping)
				return ((ChoiceQuestionsMapping) itemId).getMatrix().getName();
			return "";
		});
		
		table.addGeneratedColumn(I18N.MATCHINGMAPPING_HEADER_QUESTIONS.msg(), (source, itemId, columnId) ->	getSummary(itemId));
		
		table.addGeneratedColumn(I18N.MATCHINGMAPPING_HEADER_MAPPERYTYPE.msg(), (source, itemId, columnId) -> Arrays.stream(MatcherType.values())
				.filter(matcherType -> matcherType.getMappingClass().equals(itemId.getClass()))
				.map(MatcherType::toString)
				.findFirst()
				.orElse("INVALID"));
		
		table.addGeneratedColumn(I18N.MATCHINGMAPPING_HEADER_MAXDISTANCE.msg(), (source, itemId, columnId) ->
		{
			if (itemId instanceof AffinityMapping)
				return ((AffinityMapping) itemId).getMaxDistance();
			return "";
		});
		
		table.addContainerProperty(I18N.TABLE_HEADER_CREATEDATE.msg(), AbstractQuestionsMapping_.createDate);
		table.addContainerProperty(I18N.TABLE_HEADER_CHANGEDATE.msg(), AbstractQuestionsMapping_.changeDate);
		
		table.setCellStyleGenerator((source, itemId, propertyId) -> isInvalid(itemId) ? CssStyle.DANGER : null);
		
		return table;
	}
	
	private String getSummary(AbstractQuestionsMapping itemId)
	{
		if (itemId instanceof ChoiceQuestionsMapping)
		{
			final ChoiceQuestionsMapping choiceQuestionsMapping = (ChoiceQuestionsMapping) itemId;
			return I18N.MATCHINGMAPPING_CAPTION_CHOICEQUESTIONS.msg(
					getName(choiceQuestionsMapping.getSourceQuestion()),
					getName(choiceQuestionsMapping.getTargetQuestion()));
		}
		
		if (itemId instanceof AffinityMapping)
		{
			final AffinityMapping affinityMapping = (AffinityMapping) itemId;
			return affinityMapping.getQuestions().stream().map(this::getName).collect(Collectors.joining(", "));
		}
		
		if (itemId instanceof NumberQuestionsMapping)
		{
			final NumberQuestionsMapping numberQuestionsMapping = (NumberQuestionsMapping) itemId;
			return I18N.MATCHINGMAPPING_CAPTION_NUMBERQUESTIONS.msg(
					getName(numberQuestionsMapping.getFactQuestion()),
					getName(numberQuestionsMapping.getMinQuestion()),
					getName(numberQuestionsMapping.getMaxQuestion()));
		}
		
		if (itemId instanceof AgeQuestionMapping)
		{
			final AgeQuestionMapping ageQuestionMapping = (AgeQuestionMapping) itemId;
			return I18N.MATCHINGMAPPING_CAPTION_AGEQUESTIONS.msg(
					getName(ageQuestionMapping.getMinAgeQuestion()),
					getName(ageQuestionMapping.getMaxAgeQuestion()));
		}
		
		if (itemId instanceof AvatarQuestionMapping)
		{
			final AvatarQuestionMapping avatarQuestionMapping = (AvatarQuestionMapping) itemId;
			return I18N.MATCHINGMAPPING_CAPTION_AVATARQUESTIONS.msg(
					getName(avatarQuestionMapping.getAvatarQuestion()),
					avatarQuestionMapping.getTrueChoice().getName());
		}
		
		return "";
	}
	
	private LazyBeanTable<MatchingMatrix> createMatrixTable()
	{
		final LazyBeanTable<MatchingMatrix> table = new LazyBeanTable<>();
		table.setSelectable(true);
		table.setSizeFull();
		
		table.addContainerProperty(I18N.MATCHINGMATRIX_HEADER_NAME.msg(), MatchingMatrix_.name);
		table.addContainerProperty(I18N.TABLE_HEADER_CREATEDATE.msg(), MatchingMatrix_.createDate);
		table.addContainerProperty(I18N.TABLE_HEADER_CHANGEDATE.msg(), MatchingMatrix_.changeDate);
		
		return table;
	}
	
	private LazyBeanTable<Match> createMatchTable()
	{
		final LazyBeanTable<Match> table = new LazyBeanTable<>();
		table.setSizeFull();
		
		table.addGeneratedColumn(I18N.MATCHTABLE_HEADER_SOURCEUSER.msg(), (match) -> aliasGetter != null ? aliasGetter.getAlias(match.getSourceUserId()) : null);
		table.addGeneratedColumn(I18N.MATCHTABLE_HEADER_TARGETUSER.msg(), (match) -> aliasGetter != null ? aliasGetter.getAlias(match.getTargetUserId()) : null);
		table.addContainerProperty(I18N.MATCHTABLE_HEADER_STRICTNESS.msg(), Match_.strictness);
		table.addContainerProperty(I18N.MATCHTABLE_HEADER_COUNT.msg(), Match_.number);
		table.addContainerProperty(I18N.MATCHTABLE_HEADER_CATEGORY.msg(), Match_.category);
		table.addContainerProperty(I18N.MATCHTABLE_HEADER_UPDATEDATE.msg(), BaseEntity_.changeDate);
		
		return table;
	}
	
	private LazyBeanPagingComponent<MatchStatistic> createMatchStatisticTable()
	{
		final LazyBeanPagingComponent<MatchStatistic> table = new LazyBeanPagingComponent<>();
		table.setSizeFull();
		table.setSelectable(true);
		table.setSortPropertyId(false, MatchStatistic_.createDate);
		table.addStyleName("__matiching-statistic");
		
		table.addGeneratedColumn(I18N.MATCHSTATISTICTABLE_HEADER_MATCHINGSCOPE.msg(), MatchStatistic::getMatchingScope);
		table.addGeneratedColumn(I18N.MATCHSTATISTICTABLE_HEADER_STARTDATE.msg(), MatchStatistic::getStartDate);
		table.addGeneratedColumn(I18N.MATCHSTATISTICTABLE_HEADER_ENDDATE.msg(), MatchStatistic::getEndDate);
		table.addGeneratedColumn(I18N.MATCHSTATISTICTABLE_HEADER_DURATION.msg(), MatchStatistic::getDuration);
		table.addGeneratedColumn(I18N.MATCHSTATISTICTABLE_HEADER_COUNTRELATIONSHIPS.msg(), itemId -> getListener().getCountRelationships(itemId));
		table.addGeneratedColumn(I18N.MATCHSTATISTICTABLE_HEADER_COUNTAFFECTEDUSERS.msg(), itemId -> getListener().getCountAffectedUsers(itemId));
		
		return table;
	}
	
	private Component createActivatorAdministration()
	{
		final TableControl<Activator> tableControl = new TableControl<>(activatorTable);
		tableControl.setMargin(true);
		
		tableControl.setButtonCaptions(I18N.MATCHING_ACTION_NEWACTIVATOR.msg(), I18N.MATCHING_ACTION_EDITACTIVATOR.msg(), I18N.MATCHING_ACTION_DELETEACTIVATOR.msg());
		tableControl.setNewCallback(() -> fireEvent(MatchingViewListener::newActivator));
		tableControl.setEditCallback(item -> fireEvent(eventAction -> eventAction.editActivator(item)));
		tableControl.setDeleteCallback(item -> fireEvent(eventAction -> eventAction.deleteActivator(item)));
		
		return tableControl;
	}
	
	private Component createQuestionMapper()
	{
		final TableControl<AbstractQuestionsMapping> tableControl = new TableControl<>(mappingTable);
		tableControl.setMargin(true);
		
		tableControl.setButtonCaptions(I18N.MATCHING_ACTION_NEWMAPPER.msg(), null, I18N.MATCHING_ACTION_DELETEMAPPER.msg());
		tableControl.setNewCallback(() -> fireEvent(MatchingViewListener::newQuestionMapping));
		tableControl.setDeleteCallback(item -> fireEvent(eventAction -> eventAction.deleteQuestionMapping(item)));
		tableControl.setEditCallback(item -> fireEvent(eventAction -> eventAction.editQuestionMapping(item)));
		
		return tableControl;
	}
	
	private Component createMatrixAdministration()
	{
		final TableControl<MatchingMatrix> tableControl = new TableControl<>(matrixTable);
		tableControl.setMargin(true);
		
		tableControl.setButtonCaptions(I18N.MATCHING_ACTION_NEWMATRIX.msg(), I18N.MATCHING_ACTION_EDITMATRIX.msg(), I18N.MATCHING_ACTION_DELETEMATRIX.msg());
		tableControl.setNewCallback(() -> fireEvent(MatchingViewListener::newMatrix));
		tableControl.setEditCallback(item -> fireEvent(eventAction -> eventAction.editMatrix(item)));
		tableControl.setDeleteCallback(item -> fireEvent(eventAction -> eventAction.deleteMatrix(item)));
		
		return tableControl;
	}
	
	private Component createMatchAdministration()
	{
		final VerticalLayout layout = new VerticalLayout();
		layout.setMargin(true);
		layout.setSpacing(true);
		
		final Component categoryFilterComponent = createCategoryFilterComponent(matchTable, new MatchCategoryFilter());
		
		layout.addComponents(startMatchingButton, generateSuggestionButton, waitingComponent, categoryFilterComponent, matchFilterComponent, matchTable);
		
		return layout;
	}
	
	private <T extends BaseEntity> Component createCategoryFilterComponent(LazyBeanTable<T> table, AbstractCategoryFilter<T> filter)
	{
		Objects.requireNonNull(table);
		Objects.requireNonNull(filter);
		
		final HorizontalLayout layout = new HorizontalLayout();
		layout.setSpacing(true);
		
		table.addFilter(filter);
		
		for (RecommendationCategory category : RecommendationCategory.values())
		{
			final CheckBox checkBox = ComponentFactory.getInstance().createField(CheckBox.class, category.toString());
			checkBox.setValue(filter.isInitialActivated());
			checkBox.addValueChangeListener(event ->
			{
				filter.setCategoryEnabled(category, checkBox.getValue());
				table.refresh();
			});
			
			layout.addComponent(checkBox);
		}
		
		return layout;
	}
	
	private Component createMatchStatistic()
	{
		final VerticalLayout layout = new VerticalLayout();
		layout.setMargin(true);
		layout.setSpacing(true);
		
		final TableControl<MatchStatistic> tableControl = new TableControl<>(matchStatisticTable);
		tableControl.addButton("Details", item -> getListener().openMatchStatistic(item));
		
		layout.addComponents(tableControl);
		
		return layout;
	}
	
	private Button createMatchingButton()
	{
		final Button startButton = new Button(I18N.MATCHING_ACTION_STARTMATCHING.msg());
		startButton.addClickListener(event -> fireEvent(MatchingViewListener::startMatching));
		return startButton;
	}
	
	private Button createSuggestionButton()
	{
		final Button generateSuggestionButton = new Button(I18N.MATCHING_ACTION_GENERATESUGGESTION.msg());
		generateSuggestionButton.addClickListener(event -> fireEvent(MatchingViewListener::generateSuggestion));
		return generateSuggestionButton;
	}
	
	@Override
	public void setMatchFilterHandlerAndBuilder(DefaultFilterControlHandler matchFilterControlHandler,
			FilterSpecificationBuilder filterSpecificationBuilder)
	{
		matchFilter.setValue(null);
		matchFilterComponent.setComponent(null);
		
		if (matchFilterControlHandler != null)
		{
			final FilterControlComponent filterControlComponent = new FilterControlComponent(null,
					matchFilterControlHandler, filterSpecificationBuilder);
			filterControlComponent.addFilterChangedListener(matchFilter::setValue);
			matchFilterComponent.setComponent(filterControlComponent);
		}
	}
	
	@Override
	public void setMatricesHandler(LazyBeanItemsHandler<MatchingMatrix> handler)
	{
		matrixTable.setHandler(handler);
	}
	
	@Override
	public void setQuestionMappingHandler(LazyBeanItemsHandler<AbstractQuestionsMapping> handler)
	{
		mappingTable.setHandler(handler);
	}
	
	@Override
	public void setActivatorHandler(LazyBeanItemsHandler<Activator> handler)
	{
		activatorTable.setHandler(handler);
	}
	
	@Override
	public void setMatchHandler(LazyBeanFilteredItemsHandler<Match> handler)
	{
		matchTable.setHandler(handler);
	}
	
	@Override
	public void setMatchStatisticHandler(LazyBeanFilteredItemsHandler<MatchStatistic> handler)
	{
		matchStatisticTable.setHandler(handler);
	}
	
	@Override
	public void disableMatching()
	{
		waitingComponent.setVisible(true);
		startMatchingButton.setEnabled(false);
	}
	
	@Override
	public void enableMatching()
	{
		waitingComponent.setVisible(false);
		startMatchingButton.setEnabled(true);
	}
	
	@Override
	public void updateWaitingInfo(String text)
	{
		waitingComponent.setVisible(true);
		waitingLabel.setValue(text);
	}
	
	@Override
	public MatchingTab getSelectedMatchingTab()
	{
		return tabs.inverse().get(tabSheet.getTab(tabSheet.getSelectedTab()));
	}
	
	@Override
	public void setAutoMatchingEnabled(boolean autoMatchingEnabled)
	{
		startMatchingButton.setVisible(!autoMatchingEnabled);
		generateSuggestionButton.setVisible(!autoMatchingEnabled);
	}
}


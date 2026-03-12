package de.binaerebauten.gleichklang.adminweb.view.popup;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.AbstractSelect.ItemCaptionMode;
import com.vaadin.ui.*;
import de.binaerebauten.gleichklang.adminweb.view.util.RecommendationCategoryVisitor;
import de.binaerebauten.gleichklang.core.model.matching.*;
import de.binaerebauten.gleichklang.core.model.matching.AbstractQuestionsMapping.MatcherType;
import de.binaerebauten.gleichklang.core.model.questionnaire.*;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.service.validator.ValidationException;
import de.binaerebauten.gleichklang.core.utils.DefaultI18N;
import de.binaerebauten.gleichklang.core.utils.PropertyPathBuilder;
import de.binaerebauten.gleichklang.core.view.component.ComponentFactory;
import de.binaerebauten.gleichklang.core.view.component.ComponentGroup;
import de.binaerebauten.gleichklang.core.view.component.Popup;
import de.binaerebauten.gleichklang.core.view.component.validator.SaveHelper;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class QuestionMappingPopup extends Popup
{
	public interface SaveCallback
	{
		void save(AbstractQuestionsMapping questionsMapping) throws ValidationException;
	}
	
	private final ComponentGroup<ChoiceQuestionsMapping> choiceQuestionsMappingComponentGroup;
	private final ComponentGroup<AffinityMapping> affinityMappingComponentGroup;
	private final ComponentGroup<NumberQuestionsMapping> numberQuestionsMappingComponentGroup;
	private final ComponentGroup<AgeQuestionMapping> ageQuestionMappingComponentGroup;
	private final ComponentGroup<AvatarQuestionMapping> avatarQuestionMappingComponentGroup;
	
	private final ComboBox mappingTypeComboBox;
	
	private final SaveCallback saveCallback;
	
	public QuestionMappingPopup(AbstractQuestionsMapping questionsMapping, Collection<Question> questions, Collection<MatchingMatrix> matchingMatrices, SaveCallback saveCallback)
	{
		this(Collections.singleton(questionsMapping), questions, matchingMatrices, saveCallback);
	}
	
	public QuestionMappingPopup(Collection<AbstractQuestionsMapping> questionMappings, Collection<Question> questions, Collection<MatchingMatrix> matchingMatrices, SaveCallback saveCallback)
	{
		Objects.requireNonNull(saveCallback);
		this.saveCallback = saveCallback;
		
		setCaption(I18N.QUESTIONMAPPING_CAPTION_TITLE.msg());
		
		choiceQuestionsMappingComponentGroup = new ComponentGroup<>(ChoiceQuestionsMapping.class);
		affinityMappingComponentGroup = new ComponentGroup<>(AffinityMapping.class);
		numberQuestionsMappingComponentGroup = new ComponentGroup<>(NumberQuestionsMapping.class);
		ageQuestionMappingComponentGroup = new ComponentGroup<>(AgeQuestionMapping.class);
		avatarQuestionMappingComponentGroup = new ComponentGroup<>(AvatarQuestionMapping.class);
		
		final VerticalLayout layout = new VerticalLayout();
		layout.setSpacing(true);
		layout.setMargin(true);
		
		mappingTypeComboBox = ComponentFactory.getInstance().createField(ComboBox.class);
		mappingTypeComboBox.setRequired(true);
		
		final Component choiceQuestionComponent = createChoiceQuestionComponent(questions, matchingMatrices);
		final Component affinityComponent = createAffinityComponent(questions);
		final Component numberQuestionComponent = createNumberQuestionComponent(questions);
		final Component ageQuestionComponent = createAgeQuestionComponent(questions);
		final Component avatarQuestionComponent = createAvatarQuestionComponent(questions);
		
		mappingTypeComboBox.addValueChangeListener(event ->
		{
			final MatcherType matcherType = (MatcherType) mappingTypeComboBox.getValue();
			
			choiceQuestionComponent.setVisible(MatcherType.MATRIX.equals(matcherType));
			affinityComponent.setVisible(MatcherType.AFFINITY.equals(matcherType));
			numberQuestionComponent.setVisible(MatcherType.NUMBER.equals(matcherType));
			ageQuestionComponent.setVisible(MatcherType.AGE.equals(matcherType));
			avatarQuestionComponent.setVisible(MatcherType.AVATAR.equals(matcherType));
			
			center();
		});
		
		final SaveHelper saveHelper = new SaveHelper(this::save);
		saveHelper.setShowUnsavedNotification(false);
		
		layout.addComponents(saveHelper.getValidationComponent(), mappingTypeComboBox, choiceQuestionComponent, affinityComponent, numberQuestionComponent, ageQuestionComponent, avatarQuestionComponent, saveHelper.getSaveButton());
		setContent(layout);
		
		questionMappings.forEach(this::load);
		
		saveHelper.addFields(mappingTypeComboBox);
		saveHelper.addFields(choiceQuestionsMappingComponentGroup);
		saveHelper.addFields(affinityMappingComponentGroup);
		saveHelper.addFields(numberQuestionsMappingComponentGroup);
		saveHelper.addFields(ageQuestionMappingComponentGroup);
		saveHelper.addFields(avatarQuestionMappingComponentGroup);
	}
	
	private void load(AbstractQuestionsMapping abstractQuestionsMapping)
	{
		MatcherType matcherType = null;
		
		if (abstractQuestionsMapping instanceof AffinityMapping)
		{
			matcherType = MatcherType.AFFINITY;
			affinityMappingComponentGroup.setItemDataSource((AffinityMapping) abstractQuestionsMapping);
		}
		else if (abstractQuestionsMapping instanceof AgeQuestionMapping)
		{
			matcherType = MatcherType.AGE;
			ageQuestionMappingComponentGroup.setItemDataSource((AgeQuestionMapping) abstractQuestionsMapping);
		}
		else if (abstractQuestionsMapping instanceof AvatarQuestionMapping)
		{
			matcherType = MatcherType.AVATAR;
			avatarQuestionMappingComponentGroup.setItemDataSource((AvatarQuestionMapping) abstractQuestionsMapping);
		}
		else if (abstractQuestionsMapping instanceof ChoiceQuestionsMapping)
		{
			matcherType = MatcherType.MATRIX;
			choiceQuestionsMappingComponentGroup.setItemDataSource((ChoiceQuestionsMapping) abstractQuestionsMapping);
		}
		else if (abstractQuestionsMapping instanceof NumberQuestionsMapping)
		{
			matcherType = MatcherType.NUMBER;
			numberQuestionsMappingComponentGroup.setItemDataSource((NumberQuestionsMapping) abstractQuestionsMapping);
		}
		
		mappingTypeComboBox.addItem(matcherType);
		if (mappingTypeComboBox.size() == 1)
		{
			mappingTypeComboBox.setValue(matcherType);
			mappingTypeComboBox.setVisible(false);
		}
		else
		{
			mappingTypeComboBox.setValue(null);
			mappingTypeComboBox.setVisible(true);
		}
		abstractQuestionsMapping.setChangeDate(LocalDateTime.now());

	}
	
	private Component createQuestionMappingComponent(ComponentGroup<? extends AbstractQuestionsMapping> componentGroup, boolean withDefaultEmptyStrictness)
	{
		final VerticalLayout layout = new VerticalLayout();
		layout.setSpacing(true);
		
		layout.addComponent(componentGroup.buildAndBind(true, I18N.QUESTIONMAPPING_CAPTION_NATURALKEY.msg(), AbstractQuestionsMapping_.naturalKey));
		if (withDefaultEmptyStrictness)
			layout.addComponent(componentGroup.buildAndBind(true, I18N.QUESTIONMAPPING_CAPTION_DEFAULTEMPTYSTRICTNESS.msg(), AbstractQuestionsMapping_.defaultEmptyStrictness));
		
		return layout;
	}
	
	private Component createChoiceQuestionComponent(Collection<Question> questions, Collection<MatchingMatrix> matchingMatrices)
	{
		final BeanItemContainer<MatchingMatrix> matchingMatricesContainer = new BeanItemContainer<>(MatchingMatrix.class, matchingMatrices);
		final BeanItemContainer<ChoiceQuestion> sourceQuestionsContainer = new BeanItemContainer<>(ChoiceQuestion.class);
		final BeanItemContainer<ChoiceQuestion> targetQuestionsContainer = new BeanItemContainer<>(ChoiceQuestion.class);
		final List<ChoiceQuestion> choiceQuestions = questions.stream()
				.filter(question -> question instanceof ChoiceQuestion)
				.map(choiceQuestion -> (ChoiceQuestion) choiceQuestion)
				.collect(Collectors.toList());
		
		final VerticalLayout layout = new VerticalLayout();
		layout.setVisible(false);
		layout.setSpacing(true);
		
		final ComboBox matchingMatrixComboBox = choiceQuestionsMappingComponentGroup.buildAndBind(true, I18N.QUESTIONMAPPING_CAPTION_MATRIX.msg(), ComboBox.class, ChoiceQuestionsMapping_.matrix);
		matchingMatrixComboBox.setItemCaptionMode(ItemCaptionMode.PROPERTY);
		matchingMatrixComboBox.setItemCaptionPropertyId(PropertyPathBuilder.getFieldName(MatchingMatrix_.name));
		matchingMatrixComboBox.setContainerDataSource(matchingMatricesContainer);
		
		final ComboBox sourceQuestionComboBox = choiceQuestionsMappingComponentGroup.buildAndBind(true, I18N.QUESTIONMAPPING_CAPTION_SOURCEQUESTION.msg(), ComboBox.class, ChoiceQuestionsMapping_.sourceQuestion);
		sourceQuestionComboBox.setItemCaptionPropertyId(PropertyPathBuilder.getFieldName(Question_.i18nKey));
		sourceQuestionComboBox.setItemCaptionMode(ItemCaptionMode.PROPERTY);
		sourceQuestionComboBox.setContainerDataSource(sourceQuestionsContainer);
		
		final ComboBox targetQuestionComboBox = choiceQuestionsMappingComponentGroup.buildAndBind(true, I18N.QUESTIONMAPPING_CAPTION_TARGETQUESTION.msg(), ComboBox.class, ChoiceQuestionsMapping_.targetQuestion);
		targetQuestionComboBox.setItemCaptionPropertyId(PropertyPathBuilder.getFieldName(Question_.i18nKey));
		targetQuestionComboBox.setItemCaptionMode(ItemCaptionMode.PROPERTY);
		targetQuestionComboBox.setContainerDataSource(targetQuestionsContainer);
		
		matchingMatrixComboBox.addValueChangeListener(event ->
		{
			sourceQuestionsContainer.removeAllItems();
			targetQuestionsContainer.removeAllItems();
			sourceQuestionComboBox.setValue(null);
			targetQuestionComboBox.setValue(null);
			
			if (matchingMatrixComboBox.getValue() == null) return;
			
			final MatchingMatrix selectedMatrix = (MatchingMatrix) matchingMatrixComboBox.getValue();
			final ChoiceGroup sourceValueGroup = selectedMatrix.getSourceChoiceGroup();
			final ChoiceGroup targetValueGroup = selectedMatrix.getTargetChoiceGroup();
			
			for (ChoiceQuestion choiceQuestion : choiceQuestions)
			{
				if (sourceValueGroup.equals(choiceQuestion.getChoiceGroup()))
					sourceQuestionsContainer.addItem(choiceQuestion);
				if (targetValueGroup.equals(choiceQuestion.getChoiceGroup()))
					targetQuestionsContainer.addItem(choiceQuestion);
			}
		});
		
		layout.addComponent(createQuestionMappingComponent(choiceQuestionsMappingComponentGroup, true));
		layout.addComponents(matchingMatrixComboBox, sourceQuestionComboBox, targetQuestionComboBox);
		
		return layout;
	}
	
	private Component createAffinityComponent(Collection<Question> questions)
	{
		final BeanItemContainer<ChoiceQuestion> questionsContainer = new BeanItemContainer<>(ChoiceQuestion.class);
		questions.stream().filter(question -> question instanceof ChoiceQuestion).forEach(questionsContainer::addItem);
		
		final VerticalLayout layout = new VerticalLayout();
		layout.setVisible(false);
		layout.setSpacing(true);
		
		final Component distanceTextField = affinityMappingComponentGroup.buildAndBind(true, I18N.QUESTIONMAPPING_CAPTION_AFFINITYDISTANCE.msg(), AffinityMapping_.maxDistance);
		
		final Label choiceValueMinLabel = new Label();
		choiceValueMinLabel.setCaption(I18N.QUESTIONMAPPING_CAPTION_AFFINITYMIN.msg());
		
		final Label choiceValueMaxLabel = new Label();
		choiceValueMaxLabel.setCaption(I18N.QUESTIONMAPPING_CAPTION_AFFINITYMAX.msg());
		
		final TwinColSelect questionsTwinColSelect = affinityMappingComponentGroup.buildAndBind(true, I18N.QUESTIONMAPPING_CAPTION_AFFINITYQUESTION.msg(), TwinColSelect.class, AffinityMapping_.questions);
		questionsTwinColSelect.setContainerDataSource(questionsContainer);
		questionsTwinColSelect.setItemCaptionMode(ItemCaptionMode.PROPERTY);
		questionsTwinColSelect.setItemCaptionPropertyId(PropertyPathBuilder.getFieldName(Question_.i18nKey));
		questionsTwinColSelect.addValueChangeListener(event ->
		{
			@SuppressWarnings("unchecked")
			Set<ChoiceQuestion> selectedQuestions = (Set<ChoiceQuestion>) questionsTwinColSelect.getValue();
			if (selectedQuestions == null) selectedQuestions = new HashSet<>();
			choiceValueMaxLabel.setValue(selectedQuestions.stream().collect(Collectors.summingInt(choiceQuestion -> choiceQuestion.getChoiceGroup().getChoices().size())).toString());
			choiceValueMinLabel.setValue(selectedQuestions.size() + "");
		});
		
		layout.addComponent(createQuestionMappingComponent(affinityMappingComponentGroup, false));
		layout.addComponents(distanceTextField, choiceValueMinLabel, choiceValueMaxLabel, questionsTwinColSelect);
		
		return layout;
	}
	
	private BeanItemContainer<NumberQuestion> createNumberQuestionContainer(Collection<Question> questions)
	{
		final BeanItemContainer<NumberQuestion> questionsContainer = new BeanItemContainer<>(NumberQuestion.class);
		questions.stream().filter(question -> question instanceof NumberQuestion).forEach(questionsContainer::addItem);
		return questionsContainer;
	}
	
	private Component createNumberQuestionComponent(Collection<Question> questions)
	{
		final VerticalLayout layout = new VerticalLayout();
		layout.setVisible(false);
		layout.setSpacing(true);
		
		final ComboBox factQuestionComboBox = numberQuestionsMappingComponentGroup.buildAndBind(true, I18N.QUESTIONMAPPING_CAPTION_FACTQUESTION.msg(), ComboBox.class, NumberQuestionsMapping_.factQuestion);
		factQuestionComboBox.setItemCaptionPropertyId(PropertyPathBuilder.getFieldName(Question_.i18nKey));
		factQuestionComboBox.setItemCaptionMode(ItemCaptionMode.PROPERTY);
		factQuestionComboBox.setContainerDataSource(createNumberQuestionContainer(questions));
		
		final ComboBox minQuestionComboBox = numberQuestionsMappingComponentGroup.buildAndBind(true, I18N.QUESTIONMAPPING_CAPTION_MINQUESTION.msg(), ComboBox.class, NumberQuestionsMapping_.minQuestion);
		minQuestionComboBox.setContainerDataSource(createNumberQuestionContainer(questions));
		minQuestionComboBox.setItemCaptionPropertyId(PropertyPathBuilder.getFieldName(Question_.i18nKey));
		minQuestionComboBox.setItemCaptionMode(ItemCaptionMode.PROPERTY);
		
		final ComboBox maxQuestionComboBox = numberQuestionsMappingComponentGroup.buildAndBind(true, I18N.QUESTIONMAPPING_CAPTION_MAXQUESTION.msg(), ComboBox.class, NumberQuestionsMapping_.maxQuestion);
		maxQuestionComboBox.setContainerDataSource(createNumberQuestionContainer(questions));
		maxQuestionComboBox.setItemCaptionPropertyId(PropertyPathBuilder.getFieldName(Question_.i18nKey));
		maxQuestionComboBox.setItemCaptionMode(ItemCaptionMode.PROPERTY);
		
		layout.addComponent(createQuestionMappingComponent(numberQuestionsMappingComponentGroup, true));
		layout.addComponents(factQuestionComboBox, minQuestionComboBox, maxQuestionComboBox);
		
		return layout;
	}
	
	private Component createAgeQuestionComponent(Collection<Question> questions)
	{
		final VerticalLayout layout = new VerticalLayout();
		layout.setVisible(false);
		layout.setSpacing(true);
		
		final ComboBox minQuestionComboBox = ageQuestionMappingComponentGroup.buildAndBind(true, I18N.QUESTIONMAPPING_CAPTION_MINAGEQUESTION.msg(), ComboBox.class, AgeQuestionMapping_.minAgeQuestion);
		minQuestionComboBox.setContainerDataSource(createNumberQuestionContainer(questions));
		minQuestionComboBox.setItemCaptionPropertyId(PropertyPathBuilder.getFieldName(Question_.i18nKey));
		minQuestionComboBox.setItemCaptionMode(ItemCaptionMode.PROPERTY);
		
		final ComboBox maxQuestionComboBox = ageQuestionMappingComponentGroup.buildAndBind(true, I18N.QUESTIONMAPPING_CAPTION_MAXAGEQUESTION.msg(), ComboBox.class, AgeQuestionMapping_.maxAgeQuestion);
		maxQuestionComboBox.setContainerDataSource(createNumberQuestionContainer(questions));
		maxQuestionComboBox.setItemCaptionPropertyId(PropertyPathBuilder.getFieldName(Question_.i18nKey));
		maxQuestionComboBox.setItemCaptionMode(ItemCaptionMode.PROPERTY);
		
		layout.addComponent(createQuestionMappingComponent(ageQuestionMappingComponentGroup, false));
		layout.addComponents(minQuestionComboBox, maxQuestionComboBox);
		
		return layout;
	}
	
	private Component createAvatarQuestionComponent(Collection<Question> questions)
	{
		final BeanItemContainer<ChoiceQuestion> avatarQuestionBeanItemContainer = new BeanItemContainer<>(ChoiceQuestion.class);
		final BeanItemContainer<Choice> trueChoiceBeanItemContainer = new BeanItemContainer<>(Choice.class);
		questions.stream().filter(question -> question instanceof ChoiceQuestion).forEach(avatarQuestionBeanItemContainer::addItem);
		
		final VerticalLayout layout = new VerticalLayout();
		layout.setVisible(false);
		layout.setSpacing(true);
		
		final ComboBox avatarQuestionComboBox = avatarQuestionMappingComponentGroup.buildAndBind(true, I18N.QUESTIONMAPPING_CAPTION_AVATARQUESTION.msg(), ComboBox.class, AvatarQuestionMapping_.avatarQuestion);
		avatarQuestionComboBox.setContainerDataSource(avatarQuestionBeanItemContainer);
		avatarQuestionComboBox.setItemCaptionMode(ItemCaptionMode.PROPERTY);
		avatarQuestionComboBox.setItemCaptionPropertyId(PropertyPathBuilder.getFieldName(Question_.i18nKey));
		
		final ComboBox trueChoiceComboBox = avatarQuestionMappingComponentGroup.buildAndBind(true, I18N.QUESTIONMAPPING_CAPTION_TRUECHOICE.msg(), ComboBox.class, AvatarQuestionMapping_.trueChoice);
		trueChoiceComboBox.setContainerDataSource(trueChoiceBeanItemContainer);
		trueChoiceComboBox.setItemCaptionMode(ItemCaptionMode.PROPERTY);
		trueChoiceComboBox.setItemCaptionPropertyId(DefaultI18N.NAME);
		
		avatarQuestionComboBox.addValueChangeListener(event ->
		{
			trueChoiceBeanItemContainer.removeAllItems();
			trueChoiceComboBox.setValue(null);
			
			if (avatarQuestionComboBox.getValue() == null) return;
			
			final ChoiceQuestion choiceQuestion = (ChoiceQuestion) avatarQuestionComboBox.getValue();
			trueChoiceBeanItemContainer.addAll(choiceQuestion.getChoiceGroup().getChoices());
		});
		
		layout.addComponent(createQuestionMappingComponent(avatarQuestionMappingComponentGroup, false));
		layout.addComponents(avatarQuestionComboBox, trueChoiceComboBox);
		
		return layout;
	}
	
	private void save() throws ValidationException
	{
		switch ((MatcherType) mappingTypeComboBox.getValue())
		{
			case MATRIX:
				checkAndSave(choiceQuestionsMappingComponentGroup.getItemDataSource().getBean());
				break;
			case NUMBER:
				checkAndSave(numberQuestionsMappingComponentGroup.getItemDataSource().getBean());
				break;
			case AFFINITY:
				checkAndSave(affinityMappingComponentGroup.getItemDataSource().getBean());
				break;
			case AGE:
				checkAndSave(ageQuestionMappingComponentGroup.getItemDataSource().getBean());
				break;
			case AVATAR:
				checkAndSave(avatarQuestionMappingComponentGroup.getItemDataSource().getBean());
				break;
		}
		
		close();
	}
	
	private void checkAndSave(AbstractQuestionsMapping questionsMapping) throws ValidationException
	{
		checkRecommendationCategory(questionsMapping);
		saveCallback.save(questionsMapping);
	}
	
	private void checkRecommendationCategory(AbstractQuestionsMapping questionsMapping) throws ValidationException
	{
		final EnumSet<RecommendationCategory> recommendationCategories = questionsMapping.accept(new RecommendationCategoryVisitor());
		if (recommendationCategories == null)
			throw new ValidationException(I18N.QUESTIONMAPPING_NOTIFICATION_WRONGRECOMMENDATIONCATEGORY.msg());
	}
}

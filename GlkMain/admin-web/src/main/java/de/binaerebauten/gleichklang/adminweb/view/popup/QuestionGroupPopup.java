package de.binaerebauten.gleichklang.adminweb.view.popup;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.*;
import de.binaerebauten.gleichklang.core.model.I18NEntity;
import de.binaerebauten.gleichklang.core.model.I18NEntity.BaseName;
import de.binaerebauten.gleichklang.core.model.questionnaire.QuestionGroup;
import de.binaerebauten.gleichklang.core.model.questionnaire.QuestionGroup_;
import de.binaerebauten.gleichklang.core.model.questionnaire.Questionnaire;
import de.binaerebauten.gleichklang.core.service.validator.ValidationException;
import de.binaerebauten.gleichklang.core.utils.DefaultI18N;
import de.binaerebauten.gleichklang.core.view.component.ComponentGroup;
import de.binaerebauten.gleichklang.core.view.component.Popup;
import de.binaerebauten.gleichklang.core.view.component.TranslationComponent;
import de.binaerebauten.gleichklang.core.view.component.validator.SaveHelper;

import java.util.Collection;

public class QuestionGroupPopup extends Popup
{
	public interface SaveCallback
	{
		void save(QuestionGroup questionGroup, Collection<I18NEntity> i18nNames, Collection<I18NEntity> i18nDescriptions) throws ValidationException;
	}
	
	private final SaveHelper saveHelper;
	private final SaveCallback saveCallback;
	private final ComponentGroup<QuestionGroup> questionGroupComponentGroup;
	private final BeanItemContainer<Questionnaire> questionnaireBeanItemContainer;
	private final TranslationComponent nameTranslationComponent;
	private final TranslationComponent descriptionTranslationComponent;
	
	public QuestionGroupPopup(QuestionGroup questionGroup, Collection<I18NEntity> i18nNames, Collection<I18NEntity> i18nDescriptions, Collection<Questionnaire> questionnaires, SaveCallback saveCallback)
	{
		super(I18N.QUESTIONGROUPPOPUP_CAPTION_TITLE.msg());
		
		this.saveCallback = saveCallback;
		this.nameTranslationComponent = new TranslationComponent(I18N.QUESTIONGROUPPOPUP_CAPTION_NAME.msg(), i18nNames, BaseName.QUESTION_GROUP_NAME);
		this.descriptionTranslationComponent = new TranslationComponent(I18N.QUESTIONGROUPPOPUP_CAPTION_DESCRIPTION.msg(), i18nDescriptions, BaseName.QUESTION_GROUP_DESCRIPTION, RichTextArea.class, true);
		this.questionnaireBeanItemContainer = new BeanItemContainer<>(Questionnaire.class, questionnaires);
		this.saveHelper = new SaveHelper(this::save);
		this.saveHelper.setShowUnsavedNotification(false);
		
		questionGroupComponentGroup = new ComponentGroup<>(QuestionGroup.class);
		
		final VerticalLayout layout = new VerticalLayout();
		layout.setMargin(true);
		layout.setSpacing(true);
		
		layout.addComponents(saveHelper.getValidationComponent(), createQuestionGroupComponent(), createControlButtons());
		
		questionGroupComponentGroup.setItemDataSource(questionGroup);
		
		setContent(layout);
	}
	
	private Component createQuestionGroupComponent()
	{
		final VerticalLayout layout = new VerticalLayout();
		layout.setSpacing(true);
		
		final ComboBox questionnaireComboBox = questionGroupComponentGroup.buildAndBind(true, I18N.QUESTIONGROUPPOPUP_CAPTION_QUESTIONNAIRE.msg(), ComboBox.class, QuestionGroup_.questionnaire);
		questionnaireComboBox.setContainerDataSource(questionnaireBeanItemContainer);
		
		questionnaireComboBox.setItemCaptionMode(AbstractSelect.ItemCaptionMode.ITEM);
		questionnaireComboBox.setItemCaptionPropertyId(DefaultI18N.NAME);
		
		layout.addComponent(questionnaireComboBox);
		layout.addComponent(questionGroupComponentGroup.buildAndBind(true, I18N.QUESTIONGROUPPOPUP_CAPTION_KEY.msg(), QuestionGroup_.i18nKey));
		layout.addComponent(nameTranslationComponent);
		layout.addComponent(descriptionTranslationComponent);
		
		saveHelper.addFields(questionGroupComponentGroup);
		saveHelper.addFields(nameTranslationComponent.getFields());
		saveHelper.addFields(descriptionTranslationComponent.getFields());
		
		return layout;
	}
	
	private Component createControlButtons()
	{
		final HorizontalLayout layout = new HorizontalLayout();
		layout.setSpacing(true);
		
		layout.addComponents(saveHelper.getSaveButton());
		
		return layout;
	}
	
	private void save() throws ValidationException
	{
		final QuestionGroup questionGroup = questionGroupComponentGroup.getItemDataSource().getBean();
		
		nameTranslationComponent.setI18nKey(questionGroup.getI18nKey());
		descriptionTranslationComponent.setI18nKey(questionGroup.getI18nKey());
		
		saveCallback.save(questionGroup, nameTranslationComponent.getI18NEntities(), descriptionTranslationComponent.getI18NEntities());
		close();
	}
}

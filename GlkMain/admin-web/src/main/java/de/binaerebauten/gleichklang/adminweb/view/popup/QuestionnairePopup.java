package de.binaerebauten.gleichklang.adminweb.view.popup;

import com.vaadin.ui.*;
import de.binaerebauten.gleichklang.core.model.I18NEntity;
import de.binaerebauten.gleichklang.core.model.I18NEntity.BaseName;
import de.binaerebauten.gleichklang.core.model.questionnaire.Questionnaire;
import de.binaerebauten.gleichklang.core.model.questionnaire.Questionnaire_;
import de.binaerebauten.gleichklang.core.service.validator.ValidationException;
import de.binaerebauten.gleichklang.core.view.component.ComponentGroup;
import de.binaerebauten.gleichklang.core.view.component.Popup;
import de.binaerebauten.gleichklang.core.view.component.TranslationComponent;
import de.binaerebauten.gleichklang.core.view.component.validator.SaveHelper;

import java.util.Collection;

public class QuestionnairePopup extends Popup
{
	public interface SaveCallback
	{
		void save(Questionnaire questionnaire, Collection<I18NEntity> i18nNames, Collection<I18NEntity> i18nDescriptions) throws ValidationException;
	}

	private final SaveHelper saveHelper;
	private final SaveCallback saveCallback;
	private final ComponentGroup<Questionnaire> questionnaireComponentGroup;
	private final TranslationComponent nameTranslationComponent;
	private final TranslationComponent descriptionTranslationComponent;

	public QuestionnairePopup(SaveCallback saveCallback)
	{
		this(null, null, null, saveCallback);
	}

	public QuestionnairePopup(Questionnaire questionnaire, Collection<I18NEntity> i18nNames, Collection<I18NEntity> i18nDescriptions, SaveCallback saveCallback)
	{
		super(I18N.QUESTIONNAIREPOPUP_CAPTION_TITLE.msg());

		this.saveCallback = saveCallback;
		this.nameTranslationComponent = new TranslationComponent(I18N.QUESTIONNAIREPOPUP_CAPTION_NAME.msg(), i18nNames, BaseName.QUESTIONNAIRE_NAME);
		this.descriptionTranslationComponent = new TranslationComponent(I18N.QUESTIONNAIREPOPUP_CAPTION_DESCRIPTION.msg(), i18nDescriptions, BaseName.QUESTIONNAIRE_DESCRIPTION, RichTextArea.class, true);
		this.saveHelper = new SaveHelper(this::save);
		this.saveHelper.setShowUnsavedNotification(false);
		
		if (questionnaire == null) questionnaire = new Questionnaire();

		questionnaireComponentGroup = new ComponentGroup<>(Questionnaire.class, questionnaire);

		final VerticalLayout layout = new VerticalLayout();
		layout.setMargin(true);
		layout.setSpacing(true);

		layout.addComponents(saveHelper.getValidationComponent(), createQuestionnaireComponent(), createControlButtons());

		setContent(layout);
	}
	
	private Component createQuestionnaireComponent()
	{
		final VerticalLayout layout = new VerticalLayout();
		layout.setSpacing(true);

		final ComboBox categoryComboBox = questionnaireComponentGroup.buildAndBind(I18N.QUESTIONNAIREPOPUP_CAPTION_CATEGORY.msg(), ComboBox.class, Questionnaire_.recommendationCategory);
		categoryComboBox.setNullSelectionAllowed(true);
		
		final ComboBox iconComboBox = questionnaireComponentGroup.buildAndBind(I18N.QUESTIONNAIREPOPUP_CAPTION_ICON.msg(), ComboBox.class, Questionnaire_.icon);
		iconComboBox.setNullSelectionAllowed(true);

		layout.addComponent(categoryComboBox);
		layout.addComponent(questionnaireComponentGroup.buildAndBind(true, I18N.QUESTIONNAIREPOPUP_CAPTION_TRANSLATIONKEY.msg(), Questionnaire_.i18nKey));
		layout.addComponent(nameTranslationComponent);
		layout.addComponent(descriptionTranslationComponent);
		layout.addComponent(iconComboBox);
		
		saveHelper.addFields(questionnaireComponentGroup);
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
		final Questionnaire questionnaire = questionnaireComponentGroup.getItemDataSource().getBean();
		
		nameTranslationComponent.setI18nKey(questionnaire.getI18nKey());
		descriptionTranslationComponent.setI18nKey(questionnaire.getI18nKey());
		
		saveCallback.save(questionnaire, nameTranslationComponent.getI18NEntities(), descriptionTranslationComponent.getI18NEntities());
		close();
	}
}

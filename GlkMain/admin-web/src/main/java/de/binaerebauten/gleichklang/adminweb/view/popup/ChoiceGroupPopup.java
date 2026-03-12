package de.binaerebauten.gleichklang.adminweb.view.popup;

import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.ui.*;
import com.vaadin.ui.Notification.Type;
import de.binaerebauten.gleichklang.core.model.I18NEntity;
import de.binaerebauten.gleichklang.core.model.I18NEntity.BaseName;
import de.binaerebauten.gleichklang.core.model.questionnaire.Choice;
import de.binaerebauten.gleichklang.core.model.questionnaire.ChoiceGroup;
import de.binaerebauten.gleichklang.core.model.questionnaire.ChoiceGroup_;
import de.binaerebauten.gleichklang.core.model.questionnaire.Choice_;
import de.binaerebauten.gleichklang.core.service.validator.UniqueValidationException;
import de.binaerebauten.gleichklang.core.service.validator.ValidationException;
import de.binaerebauten.gleichklang.core.view.component.ComponentFactory;
import de.binaerebauten.gleichklang.core.view.component.ComponentGroup;
import de.binaerebauten.gleichklang.core.view.component.Popup;
import de.binaerebauten.gleichklang.core.view.component.TranslationComponent;

import java.util.*;

public class ChoiceGroupPopup extends Popup
{
	public interface SaveCallback
	{
		void save(ChoiceGroup choiceGroup, Collection<I18NEntity> i18nChoiceGroups, Collection<I18NEntity> i18NChoices) throws ValidationException;
	}

	private final SaveCallback saveCallback;
	private final ComponentGroup<ChoiceGroup> choiceGroupComponentGroup;
	private final TranslationComponent choiceGroupTranslationComponent;
	private final List<TranslationComponent> translationComponents = new ArrayList<>();
	private final List<ComponentGroup<Choice>> choiceComponentGroups = new ArrayList<>();

	public ChoiceGroupPopup(ChoiceGroup choiceGroup, Collection<I18NEntity> i18nChoiceGroups, Map<Choice, Collection<I18NEntity>> i18nMap, SaveCallback saveCallback)
	{
		super(I18N.CHOICEGROUPPOPUP_CAPTION_TITLE.msg());

		this.saveCallback = saveCallback;
		this.choiceGroupTranslationComponent = new TranslationComponent(I18N.CHOICEGROUPPOPUP_CAPTION_NAME.msg(), i18nChoiceGroups, BaseName.CHOICE_GROUP);

		choiceGroupComponentGroup = new ComponentGroup<>(ChoiceGroup.class, choiceGroup);

		final VerticalLayout layout = new VerticalLayout();
		layout.setMargin(true);
		layout.setSpacing(true);

		layout.addComponents(createChoiceGroupComponent(i18nMap), createControlButtons());

		setContent(layout);
	}

	private Component createChoiceComponent(Choice choice, Collection<I18NEntity> i18NEntities, TextField autoKeyTextField, Button removeButton)
	{
		final HorizontalLayout layout = new HorizontalLayout();
		layout.setSizeFull();
		layout.setSpacing(true);

		final int nr = choiceComponentGroups.size();

		final ComponentGroup<Choice> choiceComponentGroup = new ComponentGroup<>(Choice.class, choice);
		final TranslationComponent translationComponent = new TranslationComponent(I18N.CHOICEGROUPPOPUP_CAPTION_CHOICE.msg(nr), i18NEntities, BaseName.CHOICE_VALUE);
		final TextField i18nKeyTextField = choiceComponentGroup.buildAndBind(true, I18N.CHOICEGROUPPOPUP_CAPTION_CHOICEKEY.msg(nr), TextField.class, Choice_.i18nKey);

		translationComponents.add(translationComponent);
		choiceComponentGroups.add(choiceComponentGroup);

		autoKeyTextField.addTextChangeListener(event -> i18nKeyTextField.setValue(event.getText() + "_" + nr));
		if (i18nKeyTextField.getValue() == null)
			i18nKeyTextField.setValue(autoKeyTextField.getValue() + "_" + nr);

		layout.addComponent(i18nKeyTextField);
		layout.addComponent(translationComponent);

		if (removeButton != null)
		{
			layout.addComponent(removeButton);
			layout.setComponentAlignment(removeButton, Alignment.BOTTOM_RIGHT);
			layout.setExpandRatio(i18nKeyTextField, 1f);
			layout.setExpandRatio(translationComponent, 1f);

			removeButton.addClickListener(event ->
			{
				translationComponents.remove(translationComponent);
				choiceComponentGroups.remove(choiceComponentGroup);
			});
		}

		return layout;
	}

	private Component createChoiceGroupComponent(Map<Choice, Collection<I18NEntity>> i18nMap)
	{
		final VerticalLayout layout = new VerticalLayout();
		layout.setSpacing(true);

		final VerticalLayout choiceLayout = new VerticalLayout();
		choiceLayout.setSpacing(true);

		final TextField i18nKeyTextField = ComponentFactory.getInstance().createField(TextField.class, I18N.CHOICEGROUPPOPUP_CAPTION_AUTOKEY.msg());
		final Button addChoiceButton = new Button(I18N.CHOICEGROUPPOPUP_ACTION_ADD.msg());

		layout.addComponent(choiceGroupComponentGroup.buildAndBind(true, I18N.CHOICEGROUPPOPUP_CAPTION_CHOICEGROUPKEY.msg(), ChoiceGroup_.i18nKey));
		layout.addComponent(choiceGroupTranslationComponent);
		layout.addComponent(i18nKeyTextField);

		for (Choice choice : choiceGroupComponentGroup.getItemDataSource().getBean().getChoices())
		{
			choiceLayout.addComponent(createChoiceComponent(choice, i18nMap.get(choice), i18nKeyTextField, null));
		}

		layout.addComponent(choiceLayout);
		layout.addComponent(addChoiceButton);

		addChoiceButton.addClickListener(event -> addNewChoiceComponent(choiceLayout, i18nKeyTextField));

		return layout;
	}

	private void addNewChoiceComponent(AbstractOrderedLayout layout, TextField autoKeyTextField)
	{
		final ChoiceGroup choiceGroup = choiceGroupComponentGroup.getItemDataSource().getBean();

		final Choice choice = new Choice();
		choice.setChoiceGroup(choiceGroup);
		choiceGroup.getChoices().add(choice);

		final Button removeButton = new Button("-");

		final Component newChoiceComponent = createChoiceComponent(choice, null, autoKeyTextField, removeButton);
		layout.addComponent(newChoiceComponent);

		removeButton.addClickListener(event1 ->
		{
			choiceGroup.getChoices().remove(choice);
			layout.removeComponent(newChoiceComponent);
			center();
		});

		center();
	}

	private Component createControlButtons()
	{
		final HorizontalLayout layout = new HorizontalLayout();
		layout.setSpacing(true);

		final Button saveButton = new Button(I18N.CHOICEGROUPPOPUP_ACTION_SAVE.msg());
		saveButton.addClickListener(event ->
		{
			try
			{
				if (choiceComponentGroups.isEmpty())
					throw new CommitException();

				choiceGroupComponentGroup.commit();
				
				final ChoiceGroup choiceGroup = choiceGroupComponentGroup.getItemDataSource().getBean();
				
				final HashSet<I18NEntity> i18NChoices = new HashSet<>();
				final HashSet<I18NEntity> i18NChoiceGroups = choiceGroupTranslationComponent.commit(choiceGroup.getI18nKey());
				
				for (int i = 0; i < choiceComponentGroups.size(); i++)
				{
					final ComponentGroup<Choice> choiceComponentGroup = choiceComponentGroups.get(i);
					final TranslationComponent translationComponent = translationComponents.get(i);
					
					choiceComponentGroup.commit();
					i18NChoices.addAll(translationComponent.commit(choiceComponentGroup.getItemDataSource().getBean().getI18nKey()));
				}


				saveCallback.save(choiceGroup, i18NChoiceGroups, i18NChoices);
				close();
			}
			catch (CommitException e)
			{
				Notification.show(I18N.CHOICEGROUPPOPUP_NOTIFICATION_INVALIDENTRIES.msg(), Type.ERROR_MESSAGE);
			}
			catch (UniqueValidationException e)
			{
				Notification.show(I18N.CHOICEGROUPPOPUP_NOTIFICATION_NOTUNIQUE.msg(), Type.ERROR_MESSAGE);
			}
			catch (ValidationException e)
			{
				Notification.show(e.getLocalizedMessage(), Type.ERROR_MESSAGE);
			}
		});

		layout.addComponents(saveButton);

		return layout;
	}
}

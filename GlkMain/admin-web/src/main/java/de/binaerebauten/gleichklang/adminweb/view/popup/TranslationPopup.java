package de.binaerebauten.gleichklang.adminweb.view.popup;

import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.ui.*;
import com.vaadin.ui.Notification.Type;
import de.binaerebauten.gleichklang.core.model.I18NEntity;
import de.binaerebauten.gleichklang.core.model.I18NEntity.Language;
import de.binaerebauten.gleichklang.core.model.I18NEntity_;
import de.binaerebauten.gleichklang.core.view.component.ComponentGroup;
import de.binaerebauten.gleichklang.core.view.component.Popup;

import java.util.ArrayList;
import java.util.Collection;

public class TranslationPopup extends Popup
{
	public interface SaveCallback
	{
		void save(Collection<I18NEntity> i18NEntities);
	}

	private final SaveCallback saveCallback;
	private final Collection<ComponentGroup<I18NEntity>> translationComponentGroups = new ArrayList<>();

	public TranslationPopup(Collection<I18NEntity> i18nEntities, SaveCallback saveCallback)
	{
		super(I18N.TRANSLATIONPOPUP_CAPTION_TITLE.msg());

		this.saveCallback = saveCallback;

		for (I18NEntity entity : i18nEntities)
		{
			translationComponentGroups.add(new ComponentGroup<>(I18NEntity.class, entity));
		}

		final VerticalLayout layout = new VerticalLayout();
		layout.setMargin(true);
		layout.setSpacing(true);

		layout.addComponents(createTranslationComponent(), createControlButtons());

		setContent(layout);
	}

	private Component createTranslationComponent()
	{
		final VerticalLayout layout = new VerticalLayout();
		layout.setSpacing(true);

		for (ComponentGroup<I18NEntity> componentGroup : translationComponentGroups)
		{
			final Language language = componentGroup.getItemDataSource().getBean().getLanguage();
			final boolean required = Language.DE.equals(language);
			layout.addComponent(componentGroup.buildAndBind(required, language.toString(), RichTextArea.class, I18NEntity_.value));
		}

		return layout;
	}

	private Component createControlButtons()
	{
		final HorizontalLayout layout = new HorizontalLayout();
		layout.setSpacing(true);

		final Button saveButton = new Button(I18N.TRANSLATIONPOPUP_ACTION_SAVE.msg());
		saveButton.addClickListener(event ->
		{
			try
			{
				Collection<I18NEntity> result = new ArrayList<>();
				for (ComponentGroup<I18NEntity> componentGroup : translationComponentGroups)
				{
					componentGroup.commit();
					result.add(componentGroup.getItemDataSource().getBean());
				}

				saveCallback.save(result);
				close();
			}
			catch (CommitException e)
			{
				Notification.show(I18N.TRANSLATIONPOPUP_NOTIFICATION_INVALIDENTRIES.msg(), Type.ERROR_MESSAGE);
			}
		});

		layout.addComponents(saveButton);

		return layout;
	}
}

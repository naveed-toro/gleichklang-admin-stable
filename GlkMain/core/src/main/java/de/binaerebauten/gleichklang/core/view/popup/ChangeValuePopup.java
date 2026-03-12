package de.binaerebauten.gleichklang.core.view.popup;

import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Button;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import de.binaerebauten.gleichklang.core.service.validator.ValidationException;
import de.binaerebauten.gleichklang.core.view.component.ComponentFactory;
import de.binaerebauten.gleichklang.core.view.component.FormPanel;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;

import java.util.Objects;

public class ChangeValuePopup extends GenericPopup
{
	public interface SaveListener
	{
		void save(String value) throws ValidationException;
	}

	private final SaveListener saveListener;

	public ChangeValuePopup(String caption, String title, String description, String value, SaveListener saveListener)
	{
		super(caption);

		Objects.requireNonNull(saveListener);

		this.saveListener = saveListener;

		final VerticalLayout layout = new VerticalLayout();
		final FormPanel formPanel = new FormPanel();
		formPanel.setStyleName(CssStyle.GK_PANEL.getStyleName());
		formPanel.setCaption(title);
		formPanel.setDescription(description);

		layout.setSpacing(true);

		final TextField textField = ComponentFactory.getInstance().createField(TextField.class);
		textField.setValue(value);
		formPanel.addComponent(textField);
		layout.addComponent(formPanel);

		createControls(textField);

		setPopupContent(layout);

		addStyleName(CssStyle.POPUP_TYPE_GREEN.getStyleName());
        setBoxSize(BoxSize.WIDE);
	}

	private void createControls(TextField textField) {
		final Button saveButton = new Button(I18N.CHANGEVALUEPOPUP_ACTION_SAVE.msg(), event -> saveValue(textField));
		saveButton.setIcon(FontAwesome.SAVE);

		final Button backButton = new Button(I18N.CHANGEVALUEPOPUP_ACTION_BACK.msg(), event -> close());
        backButton.setIcon(FontAwesome.CHEVRON_LEFT);

		addFooterComponent(saveButton, FooterPosition.RIGHT);
		addFooterComponent(backButton, FooterPosition.LEFT);
	}

	private void saveValue(TextField textField)
	{
		try
		{
			saveListener.save(textField.getValue());
			close();
		}
		catch (ValidationException e)
		{
			Notification.show(e.getMessage(), Type.WARNING_MESSAGE);
		}
	}
}

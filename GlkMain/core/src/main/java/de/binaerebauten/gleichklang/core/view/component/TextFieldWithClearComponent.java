package de.binaerebauten.gleichklang.core.view.component;

import com.google.common.base.Strings;
import com.vaadin.annotations.StyleSheet;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;

import javax.persistence.OrderBy;
import java.util.Objects;

@SuppressWarnings("serial")
public class TextFieldWithClearComponent extends CustomComponent
{
	private final TextField textField;
	private final Label textFieldCaption;
	
	public TextFieldWithClearComponent(TextField textField)
	{
		Objects.requireNonNull(textField);
		
		this.textField = textField;
		this.textFieldCaption = new Label();
		setCaption(textField.getCaption());
		textField.setCaption(null);


		
		final HorizontalLayout layout = new HorizontalLayout();
		layout.setSpacing(true);

		final HorizontalLayout textfieldWrapper = new HorizontalLayout();
		textfieldWrapper.setStyleName(CssStyle.TEXTFIELD_CLEARABLE_WRAPPER.getStyleName());

		final Button clearButton = new Button();
		clearButton.setIcon(FontAwesome.TIMES_CIRCLE);
		clearButton.addStyleName(ValoTheme.BUTTON_ICON_ONLY);
		clearButton.addClickListener(event ->
		{
			textField.clear();
			fireTextChangeEvent(textField);
		});
        textField.addTextChangeListener(event -> {
            if (Strings.isNullOrEmpty(event.getText())) {
                addStyleName(CssStyle.TEXTFIELD_CLEARABLE_EMPTY.getStyleName());
            } else {
                removeStyleName(CssStyle.TEXTFIELD_CLEARABLE_EMPTY.getStyleName());
            }
        });

		textfieldWrapper.addComponents(textField, clearButton);

		layout.addComponents(textFieldCaption, textfieldWrapper);
		layout.setComponentAlignment(textFieldCaption, Alignment.MIDDLE_LEFT);
		layout.setComponentAlignment(textfieldWrapper, Alignment.MIDDLE_LEFT);
		setPrimaryStyleName(CssStyle.TEXTFIELD_CLEARABLE.getStyleName());
		addStyleName(CssStyle.TEXTFIELD_CLEARABLE_EMPTY.getStyleName());
		setCompositionRoot(layout);
	}
	
	private void fireTextChangeEvent(final TextField textField)
	{
		for (Object listener : textField.getListeners(TextChangeEvent.class))
		{
			((TextChangeListener) listener).textChange(new TextChangeEvent(textField)
			{
				@Override
				public String getText()
				{
					return "";
				}
				
				@Override
				public int getCursorPosition()
				{
					return 0;
				}
			});
		}
	}
	
	public TextField getTextField()
	{
		return textField;
	}

	@Override
	public void setCaption(String caption) {
	    textFieldCaption.setValue(caption);
	    textFieldCaption.setVisible(!Strings.isNullOrEmpty(caption));
    }

    @Override
    public String getCaption() {
	    return textFieldCaption.getValue();
    }
}

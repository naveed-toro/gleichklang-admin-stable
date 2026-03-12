package de.binaerebauten.gleichklang.adminweb.view.popup;

import com.vaadin.ui.*;
import de.binaerebauten.gleichklang.core.view.component.Popup;

import javax.validation.constraints.Null;

public class PaymentReceivedPopup extends Popup
{
	public interface SaveCallback
	{
		void save(String comment);
	}

	private final SaveCallback saveCallback;
	
	private final TextField commentField;

	public String getExistingComments() {
		return existingComments;
	}

	private final String existingComments;

	public PaymentReceivedPopup(SaveCallback saveCallback, String existingComments)
	{
		super(I18N.PAYMENTRECEIVEDPOPUP_CAPTION_TITLE.msg());
		setWidth(45, Unit.PERCENTAGE);
		setHeight(45, Unit.PERCENTAGE);

		this.saveCallback = saveCallback;

		this.existingComments = existingComments;

		final VerticalLayout layout = new VerticalLayout();
		layout.setMargin(true);
		layout.setSpacing(true);
		// TODO Task 1 changes
		commentField = new TextField();
		if(existingComments != null) {
			commentField.setValue(existingComments);
		}
		commentField.setWidth(100, Unit.PERCENTAGE);
		commentField.setInputPrompt(I18N.PAYMENT_COMMENT.msg());
		
		layout.addComponents(commentField, createControlButtons());

		setContent(layout);
	}

	private Component createControlButtons()
	{
		final HorizontalLayout layout = new HorizontalLayout();
		layout.setSpacing(true);

		final Button saveButton = new Button(I18N.PAYMENTRECEIVEDPOPUP_ACTION_SAVE.msg());
		saveButton.addClickListener(event ->
		{
			saveCallback.save(commentField.getValue());
			close();
		});

		layout.addComponents(saveButton);

		return layout;
	}
}

package de.binaerebauten.gleichklang.core.view.component;

import com.vaadin.server.FontAwesome;
import com.vaadin.server.Resource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import de.binaerebauten.gleichklang.core.utils.DefaultEnumI18N;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;
import de.binaerebauten.gleichklang.core.view.popup.GenericPopup;

@SuppressWarnings("serial")
public class MessageBox extends GenericPopup
{
	public interface DialogResultListener
	{
		void onClose(DialogResult dialogResult);
	}
	
	public enum MessageBoxButtons
	{
		OK(MessageBoxButton.OK),
		OK_CANCEL(MessageBoxButton.OK, MessageBoxButton.CANCEL),
		YES_NO(MessageBoxButton.YES, MessageBoxButton.NO),
		YES_NO_CANCEL(MessageBoxButton.YES, MessageBoxButton.NO, MessageBoxButton.CANCEL);
		
		private final MessageBoxButton[] btns;
		
		MessageBoxButtons(MessageBoxButton... btns)
		{
			this.btns = btns;
		}
	}
	
	public enum MessageBoxStyle
	{
		QUESTION("question"),
		ATTENTION("attention"),
		NONE("none");
		
		private final String stylename;
		
		MessageBoxStyle(String stylename)
		{
			this.stylename = stylename;
		}
		
		public String getStylename()
		{
			return this.stylename;
		}
	}
	
	/* Only package visible for tests, otherwise could be private */
	enum MessageBoxButton implements DefaultEnumI18N
	{
		OK(DialogResult.OK),
		CANCEL(DialogResult.CANCEL),
		YES(DialogResult.YES),
		NO(DialogResult.NO);
		
		private final DialogResult dialogResult;
		
		MessageBoxButton(DialogResult dialogResult)
		{
			this.dialogResult = dialogResult;
		}
		
		@Override
		public String toString()
		{
			return msg();
		}
	}
	
	public enum DialogResult
	{
		NONE,
		OK,
		CANCEL,
		YES,
		NO
	}
	
	private DialogResult dialogResult = DialogResult.NONE;
	private MessageBoxStyle messageBoxStyle = MessageBoxStyle.NONE;
	
	private MessageBox(String text, String reason, String action, MessageBoxButtons buttons, MessageBoxStyle messageBoxStyle, DialogResultListener dialogResultListener)
	{
		buildLayout(text, reason, action, buttons, messageBoxStyle, dialogResultListener);
		setModal(true);
		setResizable(false);

		setClosable(false);
		addStyleName(CssStyle.MESSAGE_BOX.getStyleName());
	}
	
	public static void show(String text)
	{
		show(text, null, MessageBoxButtons.OK, MessageBoxStyle.NONE, null);
	}
	
	public static void show(String text, final DialogResultListener dialogResultListener)
	{
		show(text, null, MessageBoxButtons.OK, MessageBoxStyle.NONE, dialogResultListener);
	}
	
	public static void show(String text, MessageBoxButtons buttons, final DialogResultListener dialogResultListener)
	{
		show(text, null, buttons, MessageBoxStyle.NONE, dialogResultListener);
	}
	
	public static void show(String text, MessageBoxButtons buttons, MessageBoxStyle messageBoxStyle, final DialogResultListener dialogResultListener)
	{
		show(text, null, buttons, messageBoxStyle, dialogResultListener);
	}
	
	public static void show(String text, String reason, final DialogResultListener dialogResultListener)
	{
		show(text, reason, MessageBoxButtons.OK, MessageBoxStyle.NONE, dialogResultListener);
	}
	
	public static void show(String text, String reason, MessageBoxStyle messageBoxStyle, final DialogResultListener dialogResultListener)
	{
		show(text, reason, MessageBoxButtons.OK, messageBoxStyle, dialogResultListener);
	}
	
	public static void show(String text, String reason, MessageBoxButtons buttons, MessageBoxStyle messageBoxStyle, final DialogResultListener dialogResultListener)
	{
		show(text, reason, null, buttons, messageBoxStyle, dialogResultListener);
	}
	
	/**
	 * Shows a Message Box.
	 *
	 * @param text                 the shown text
	 * @param reason               the reason for the event
	 * @param action               action to avoid the event
	 * @param buttons              visible buttons
	 * @param messageBoxStyle      style of the message box
	 * @param dialogResultListener action which runs after message box is closed
	 */
	public static void show(String text, String reason, String action, MessageBoxButtons buttons, MessageBoxStyle messageBoxStyle, final DialogResultListener dialogResultListener)
	{
		UI.getCurrent().addWindow(new MessageBox(text, reason, action, buttons, messageBoxStyle, dialogResultListener));
	}
	
	private void buildLayout(String text, String reason, String action, MessageBoxButtons buttons, MessageBoxStyle messageBoxStyle, final DialogResultListener dialogResultListener)
	{
		this.messageBoxStyle = messageBoxStyle;
		setMessageStyle(messageBoxStyle);


		final VerticalLayout layout = new VerticalLayout();
		layout.setMargin(false);
		layout.setSpacing(false);

		layout.addComponent(createMessage(text, reason, action));

		// set control buttons
		setControlButtons(buttons);
		
		addCloseListener(dialogResultListener);

		setPopupContent(layout);
	}
	
	private Component createContent(String text)
	{
		final Label label = new Label();
		label.setContentMode(ContentMode.HTML);
		
		if (text.indexOf('\n') >= 0)
		{
			// Multiline
			label.setValue(text.replaceAll("\n", "<br/>"));
		}
		else
		{
			label.setValue(text);
		}
		
		return label;
	}
	
	private Component createMessage(String text, String reason, String action)
	{
		final VerticalLayout layout = new VerticalLayout();
		layout.setWidth(100, Unit.PERCENTAGE);
		
		Component message = createContent(text);
		message.addStyleName("message");
		layout.addComponent(message);
		
		if (reason != null)
		{
			Component reasonLabel = createContent(reason);
			reasonLabel.addStyleName("reason");
			layout.addComponent(reasonLabel);
		}
		
		if (action != null)
		{
			Component actionLabel = createContent(action);
			actionLabel.addStyleName("action");
			actionLabel.addStyleName(messageBoxStyle.getStylename());
			layout.addComponent(actionLabel);
		}

		return layout;
	}
	
	private void addCloseListener(final DialogResultListener dialogResultListener)
	{
		this.addCloseListener((CloseListener) event ->
		{
			if (dialogResultListener != null)
				dialogResultListener.onClose(dialogResult);
		});
	}

	private void setControlButtons(MessageBoxButtons buttons) {
		for (MessageBoxButton button : buttons.btns) {
			addFooterComponent(createButton(button));
		}
	}
	
	private Button createButton(MessageBoxButton buttonType)
	{
		final Button button = new Button(buttonType.toString());
		button.addClickListener(event ->
		{
			dialogResult = buttonType.dialogResult;
			close();
		});
		return button;
	}

	private void setMessageStyle(MessageBoxStyle style) {

		switch (style) {
			case QUESTION:
				addStyleName(style.getStylename());
				setIcon(FontAwesome.QUESTION_CIRCLE);
				break;

			case ATTENTION:
				addStyleName(style.getStylename());
				setIcon(FontAwesome.EXCLAMATION_CIRCLE);
		}
	}
}

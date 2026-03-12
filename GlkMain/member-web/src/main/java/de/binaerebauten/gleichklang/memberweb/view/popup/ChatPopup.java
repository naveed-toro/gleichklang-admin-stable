package de.binaerebauten.gleichklang.memberweb.view.popup;

import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.ui.*;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.view.component.ComponentFactory;
import de.binaerebauten.gleichklang.core.view.component.Popup;
import de.binaerebauten.gleichklang.memberweb.view.I18N;

public class ChatPopup extends Popup
{
	public interface SendCallback
	{
		void sendMessage(User targetUser, String msg);
	}

	private final User sourceUser;
	private final User targetUser;
	private final ListSelect chatView;

	public ChatPopup(User sourceUser, User targetUser, SendCallback sendCallback)
	{
		this.sourceUser = sourceUser;
		this.targetUser = targetUser;

		chatView = ComponentFactory.getInstance().createField(ListSelect.class);

		setCaption(I18N.CHAT_POPUP_CAPTION.msg(targetUser.getAlias()));
		setModal(false);

		final VerticalLayout layout = new VerticalLayout();
		layout.setSpacing(true);
		layout.setMargin(true);

		layout.addComponent(createChatComponent(sendCallback));

		setContent(layout);
	}

	private Component createChatComponent(SendCallback sendCallback)
	{
		final VerticalLayout layout = new VerticalLayout();
		layout.setSpacing(true);

		final HorizontalLayout inputLayout = new HorizontalLayout();
		inputLayout.setSpacing(true);

		final TextField inputText = ComponentFactory.getInstance().createField(TextField.class);
		final Button sendButton = new Button(I18N.CHAT_POPUP_SENDBUTTON.msg());
		sendButton.setClickShortcut(KeyCode.ENTER);
		sendButton.addClickListener(event -> sendMessage(sendCallback, inputText));

		inputLayout.addComponents(inputText, sendButton);
		layout.addComponents(chatView, inputLayout);

		return layout;
	}

	private void sendMessage(SendCallback sendCallback, TextField inputText)
	{
		sendCallback.sendMessage(targetUser, inputText.getValue());
		inputText.clear();
	}

	public void addText(User user, String msg)
	{
		chatView.addItem(user.getAlias() + ": " + msg);
	}
}

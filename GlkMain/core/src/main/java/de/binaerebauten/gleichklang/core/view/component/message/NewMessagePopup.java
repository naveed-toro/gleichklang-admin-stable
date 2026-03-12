package de.binaerebauten.gleichklang.core.view.component.message;

import com.vaadin.server.ThemeResource;
import de.binaerebauten.gleichklang.core.model.message.Message;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.service.file.MessageUploadFile;
import de.binaerebauten.gleichklang.core.view.component.message.MessageDialog.NewUploadCallback;
import de.binaerebauten.gleichklang.core.view.component.message.MessageDialog.SaveCallback;
import de.binaerebauten.gleichklang.core.view.component.message.MessageDialog.SendCallback;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;
import de.binaerebauten.gleichklang.core.view.popup.GenericPopup;

import java.util.List;
import java.util.Objects;

public class NewMessagePopup extends GenericPopup
{
	private final MessageDialog messageDialog;
	
	public NewMessagePopup(boolean isMessageToGleichlang, Message message, List<MessageUploadFile> messageUploadFiles)
	{
		Objects.requireNonNull(message);
		
		messageDialog = new MessageDialog(isMessageToGleichlang, message);
		messageDialog.setCancelCallback(this::close);
		messageDialog.setMessage(message, messageUploadFiles);
		
		setPopupContent(messageDialog);
		this.addStyleName(CssStyle.MESSAGE_DIALOG_POPUP.getStyleName());
		this.addStyleName(CssStyle.MESSAGE_DIALOG_POPUP_NORMAL.getStyleName());

		setCaption(I18N.NEW_MESSAGE_POPUP_HEADER.msg());
		setIcon(new ThemeResource("img/mail-normal.svg"));

		addFooterComponent(messageDialog.getControlComponents());
	}
	
	public NewMessagePopup(boolean isMessageToGleichlang, Message message)
	{
		this(isMessageToGleichlang, message, null);
	}
	
	/**
	 * New message
	 *
	 */
	public NewMessagePopup(Message message)
	{
		this(false, message, null);
	}
	
	/**
	 * Edit a message (Edit Draft-Message)
	 *
	 * @param message
	 * @param messageUploadFiles
	 */
	public NewMessagePopup(Message message, List<MessageUploadFile> messageUploadFiles)
	{
		this(false, message, messageUploadFiles);
	}
	
	public void setReceiverList(List<User> receiverList)
	{
		messageDialog.setReceiverContainer(receiverList);
	}
	
	public void setReceiver(User receiver)
	{
		messageDialog.setReceiverContainer(receiver);
	}
	
	public void setSaveCallback(SaveCallback saveCallback)
	{
		Objects.requireNonNull(saveCallback);
		messageDialog.setSaveCallback((message, messageUploadFiles) ->
		{
			message.setBody(messageDialog.bodyText.getValue());
			saveCallback.saveMessage(message, messageUploadFiles);
			close();
		});
	}
	
	public void setSendCallback(SendCallback sendCallback)
	{
		Objects.requireNonNull(sendCallback);
		messageDialog.setSendCallback((message, messageUploadFiles) ->
		{
		    message.setBody(messageDialog.bodyText.getValue());
			sendCallback.sendMessage(message, messageUploadFiles);
			close();
		});
	}
	
	public void setNewUploadCallback(NewUploadCallback newUploadCallback)
	{
		Objects.requireNonNull(newUploadCallback);
		messageDialog.setNewUploadCallback(newUploadCallback);
	}
	
	public void setSubjectEnabled(boolean enabled)
	{
		messageDialog.setSubjectEnabled(enabled);
	}

	public void setOldMessage(String message)
	{
		messageDialog.setOldMessage(message);
	}
}

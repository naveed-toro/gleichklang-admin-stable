package de.binaerebauten.gleichklang.memberweb.view.popup;

import com.vaadin.server.ThemeResource;
import com.vaadin.ui.VerticalLayout;
import de.binaerebauten.gleichklang.core.model.message.Message;
import de.binaerebauten.gleichklang.core.model.user.ClientInformation.Device;
import de.binaerebauten.gleichklang.core.view.component.LazyBeanItemContainer.LazyBeanItemsHandler;
import de.binaerebauten.gleichklang.core.view.component.message.MessageTable;
import de.binaerebauten.gleichklang.core.view.component.message.MessageTable.Directory;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;
import de.binaerebauten.gleichklang.core.view.popup.GenericPopup;

import static de.binaerebauten.gleichklang.memberweb.view.I18N.SHOWMESSAGE_POPUP_MESSAGEHEADER;

public class MessageListPopup extends GenericPopup
{
	public interface MessageListPopupListener
	{
		void showMessage(Message message, float width, float height);
	}
	
	private final MessageTable messageTable;
	
	public MessageListPopup(MessageListPopupListener listener, LazyBeanItemsHandler<Message> messageLazyBeanItemsHandler, Device device)
	{
		final VerticalLayout panel = new VerticalLayout();
		panel.setStyleName(CssStyle.MESSAGE_TABLE_WRAPPER.getStyleName());
		
		messageTable = new MessageTable(Directory.INCOMING, false, device,null);
		messageTable.setMessagesHandler(messageLazyBeanItemsHandler);
		messageTable.setOpenButtonClickListener(event -> listener.showMessage(event, getWidth(), getHeight()));
		
		setCaption(SHOWMESSAGE_POPUP_MESSAGEHEADER.msg());
		addStyleName(CssStyle.MESSAGE_DIALOG_POPUP.getStyleName());
		setIcon(new ThemeResource("img/mail-normal.svg"));
		
		panel.addComponent(messageTable);
		
		setPopupContent(panel);
	}
	
	@Override
	public void onDeviceChanged(Device device)
	{
		super.onDeviceChanged(device);
		messageTable.onDeviceChanged(device);
	}
}

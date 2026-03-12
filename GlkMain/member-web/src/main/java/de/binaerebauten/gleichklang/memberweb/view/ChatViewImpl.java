package de.binaerebauten.gleichklang.memberweb.view;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.AbstractSelect.ItemCaptionMode;
import com.vaadin.ui.*;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.model.user.User_;
import de.binaerebauten.gleichklang.core.utils.PropertyPathBuilder;
import de.binaerebauten.gleichklang.core.view.AbstractNavigateView;
import de.binaerebauten.gleichklang.core.view.component.ComponentFactory;
import de.binaerebauten.gleichklang.memberweb.view.ChatView.ChatViewListener;
import de.binaerebauten.gleichklang.memberweb.view.I18N;


import java.util.List;

public class ChatViewImpl extends AbstractNavigateView<ChatViewListener> implements ChatView
{
	private final BeanItemContainer<User> userContainer;

	public ChatViewImpl()
	{
		userContainer = new BeanItemContainer<>(User.class);

		final VerticalLayout layout = new VerticalLayout();
		layout.setSizeFull();
		layout.setSpacing(true);

		layout.addComponent(createChatControl());

		setCompositionRoot(layout);
	}

	private Component createChatControl()
	{
		final VerticalLayout layout = new VerticalLayout();
		layout.setSpacing(true);

		final ComboBox receiverComboBox = ComponentFactory.getInstance().createField(ComboBox.class, I18N.CHAT_COMBOBOX_RECEIVER.msg());
		receiverComboBox.setContainerDataSource(userContainer);
		receiverComboBox.setItemCaptionMode(ItemCaptionMode.PROPERTY);
		receiverComboBox.setItemCaptionPropertyId(PropertyPathBuilder.getFieldName(User_.alias));
		receiverComboBox.setRequired(true);

		Button startButton = new Button(I18N.CHAT_COMBOBOX_STARTBUTTON.msg());
		startButton.addClickListener(event -> startChat(receiverComboBox));

		layout.addComponents(receiverComboBox, startButton);

		return layout;
	}

	private void startChat(ComboBox receiverComboBox)
	{
		if(!receiverComboBox.isValid()) return;
		fireEvent(action -> action.startChat((User) receiverComboBox.getValue()));
	}

	@Override
	public void setReceiverList(List<User> userList)
	{
		userContainer.removeAllItems();
		if (userList != null) userContainer.addAll(userList);
	}
}

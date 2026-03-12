package de.binaerebauten.gleichklang.core.view.component.message;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.AbstractSelect.ItemCaptionMode;
import com.vaadin.ui.*;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.Notification.Type;
import de.binaerebauten.gleichklang.core.model.message.Envelope_;
import de.binaerebauten.gleichklang.core.model.message.Message;
import de.binaerebauten.gleichklang.core.model.message.Message_;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.model.user.User_;
import de.binaerebauten.gleichklang.core.service.file.MessageUploadFile;
import de.binaerebauten.gleichklang.core.service.validator.ValidationException;
import de.binaerebauten.gleichklang.core.utils.PropertyPathBuilder;
import de.binaerebauten.gleichklang.core.utils.StringUtils;
import de.binaerebauten.gleichklang.core.view.component.ComponentGroup;
import de.binaerebauten.gleichklang.core.view.component.Emoji;
import de.binaerebauten.gleichklang.core.view.component.UploadComponent;
import de.binaerebauten.gleichklang.core.view.component.validator.SaveHelper;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;

import javax.xml.soap.Text;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("serial")
public class MessageDialog extends CustomComponent
{
	public interface NewUploadCallback
	{
		MessageUploadFile newUpload(Message message, Collection<MessageUploadFile> messageUploadFiles) throws ValidationException;
	}
	
	public interface SaveCallback
	{
		void saveMessage(Message message, List<MessageUploadFile> messageUploadFiles) throws ValidationException;
	}
	
	public interface SendCallback
	{
		void sendMessage(Message message, List<MessageUploadFile> messageUploadFiles) throws ValidationException;
	}
	
	public interface CancelCallback
	{
		void cancelMessage();
	}
	
	private final UploadComponent<MessageUploadFile> uploadComponent = new UploadComponent<>(I18N.MESSAGEDIALOG_ATTACHMENT.msg(), I18N.MESSAGEDIALOG_ATTACHMENT_BUTTON_CAPTION.msg());
	private final Label tooManyAttachments = new Label(I18N.MESSAGEDIALOG_MAX_ATTACHMENTS.msg());
	private final BeanItemContainer<User> userContainer;
	private final BeanItemContainer<MessageUploadFile> messageUploadFiles;
	private final ComponentGroup<Message> messageFieldGroup;
	private final ComboBox receiverComboBox;
	private final TextField subjectTextField;
	public final RichTextArea bodyText;
	private final Button cancelButton;
	private final Component controlComponents;
	private final Component emojiBar;
	private final Label oldMessageLabel;
	private final VerticalLayout oldMessageLayout;
	private final SaveHelper saveHelper;
	private final SaveHelper sendHelper;
	
	private Message message = null;
	private NewUploadCallback newUploadCallback = null;
	private SaveCallback saveCallback = null;
	private SendCallback sendCallback = null;
	private CancelCallback cancelCallback = null;
	private Message messages;
	
	public MessageDialog(boolean isMessageToGleichklang, Message messages) {
		this.messages = messages;
		saveHelper = createSaveHelper();
		sendHelper = createSendHelper();
		sendHelper.setShowValidationNotification(false);

		userContainer = new BeanItemContainer<>(User.class);
		messageUploadFiles = new BeanItemContainer<>(MessageUploadFile.class);
		messageFieldGroup = new ComponentGroup<>(Message.class);

		if (!isMessageToGleichklang) {
			receiverComboBox = createReceiverComboBox();
		} else {
			receiverComboBox = createAdminReceiverComboBox();
			hideSaveButton();
		}

		subjectTextField = createSubjectTextField();
		if(messages==null || messages.getBody()==null || messages.getBody().isEmpty()){
			bodyText = createBodyRichTextArea();
		}
		else{
			bodyText =createBodyRichTextAreas();
		}

		cancelButton = createCancelButton();
		emojiBar = createEmojiBar();
		
		final VerticalLayout layout = new VerticalLayout();
		layout.setSizeFull();
		
		oldMessageLabel = createOldMessageLabel();
		oldMessageLayout = createOldMessageLayout();
		
		controlComponents = createControlButtons();
		
		layout.addComponents(saveHelper.getValidationComponent(), sendHelper.getValidationComponent(), createDialogPanel());
		
		setCompositionRoot(layout);
		
		saveHelper.addFields(messageFieldGroup);
		sendHelper.addFields(messageFieldGroup);
	}
	
	private Label createOldMessageLabel()
	{
		final Label label = new Label();
		label.setContentMode(ContentMode.HTML);
		label.setStyleName(CssStyle.COLLAPSED.getStyleName());
		
		return label;
	}
	
	private VerticalLayout createOldMessageLayout()
	{
		final VerticalLayout layout = new VerticalLayout();
		layout.setStyleName(CssStyle.OLD_MESSAGE_WRAPPER.getStyleName());
		layout.setVisible(false);
		
		final Button btnExpand = new Button();
		btnExpand.setIcon(FontAwesome.ANGLE_DOUBLE_DOWN);
		
		final Button btnCollapse = new Button();
		btnCollapse.setVisible(false);
		btnCollapse.setIcon(FontAwesome.ANGLE_DOUBLE_UP);
		
		btnExpand.addClickListener(event ->
		{
			oldMessageLabel.setStyleName(CssStyle.EXPAND.getStyleName());
			oldMessageLabel.removeStyleName(CssStyle.COLLAPSED.getStyleName());
			btnExpand.setVisible(false);
			btnCollapse.setVisible(true);
		});
		
		btnCollapse.addClickListener(event ->
		{
			oldMessageLabel.removeStyleName(CssStyle.EXPAND.getStyleName());
			oldMessageLabel.setStyleName(CssStyle.COLLAPSED.getStyleName());
			btnExpand.setVisible(true);
			btnCollapse.setVisible(false);
		});
		
		layout.addComponents(oldMessageLabel, btnExpand, btnCollapse);
		
		return layout;
	}
	
	private Button createCancelButton()
	{
		final Button button = new Button(I18N.MESSAGEDIALOG_ACTION_CANCEL.msg(), (event) -> cancelCallback.cancelMessage());
		button.setVisible(false);
		button.setIcon(FontAwesome.TIMES);
		
		return button;
	}
	
	private SaveHelper createSendHelper()
	{
		final SaveHelper sendHelper = new SaveHelper(this::sendMessage);
		
		sendHelper.setSuccessMessage(I18N.MESSAGEDIALOG_NOTIFICATION_MESSAGESEND.msg());
		sendHelper.setFailMessage(I18N.MESSAGEDIALOG_NOTIFICATION_INVALIDENTRIES.msg());
		sendHelper.getSaveButton().addClickListener(event -> saveHelper.resetValidationResult());
		sendHelper.setShowUnsavedNotification(false);
		sendHelper.getSaveButton().setCaption(I18N.MESSAGEDIALOG_ACTION_SEND.msg());
		sendHelper.getSaveButton().setIcon(FontAwesome.SEND);
		sendHelper.getSaveButton().setVisible(false);
		
		return sendHelper;
	}
	
	private SaveHelper createSaveHelper()
	{
		final SaveHelper saveHelper = new SaveHelper(this::saveMessage);
		
		saveHelper.setSuccessMessage(I18N.MESSAGEDIALOG_NOTIFICATION_MESSAGESAVED.msg());
		saveHelper.setFailMessage(I18N.MESSAGEDIALOG_NOTIFICATION_INVALIDENTRIES.msg());
		saveHelper.getSaveButton().addClickListener(event -> sendHelper.resetValidationResult());
		saveHelper.setShowUnsavedNotification(false);
		saveHelper.getSaveButton().setCaption(I18N.MESSAGEDIALOG_ACTION_SAVE.msg());
		saveHelper.getSaveButton().setIcon(FontAwesome.SAVE);
		saveHelper.getSaveButton().setVisible(false);
		
		return saveHelper;
	}
	
	private TextField createSubjectTextField()
	{
		final TextField textField = messageFieldGroup.buildAndBind(true, I18N.MESSAGEDIALOG_LABEL_SUBJECT.msg(), TextField.class, Message_.subject);
		textField.addStyleName(CssStyle.MARGIN_BOTTOM.getStyleName());
		textField.setMaxLength(150);
		
		return textField;
	}
	
	private ComboBox createReceiverComboBox()
	{
		final ComboBox comboBox = messageFieldGroup.buildAndBind(true, I18N.MESSAGEDIALOG_LABEL_RECEIVER.msg(), ComboBox.class, Message_.receiverEnvelope, Envelope_.user);
		
		comboBox.setContainerDataSource(userContainer);
		comboBox.setItemCaptionMode(ItemCaptionMode.PROPERTY);
		comboBox.setItemCaptionPropertyId(PropertyPathBuilder.getFieldName(User_.alias));
		comboBox.setCaptionAsHtml(true);
		comboBox.addStyleName(CssStyle.MARGIN_BOTTOM.getStyleName());
		
		return comboBox;
	}
	
	private ComboBox createAdminReceiverComboBox()
	{
		final ComboBox placeHolder = new ComboBox(I18N.MESSAGEDIALOG_LABEL_RECEIVER.msg());
		
		placeHolder.addItem(I18N.MESSAGEDIALOG_TEAM_GLEICHKLANG.msg());
		placeHolder.setValue(I18N.MESSAGEDIALOG_TEAM_GLEICHKLANG.msg());
		placeHolder.select(0);
		placeHolder.setEnabled(true);
		placeHolder.addStyleName(CssStyle.ANSWERED.getStyleName());
		placeHolder.addStyleName(CssStyle.MARGIN_BOTTOM.getStyleName());
		placeHolder.setWidth("100%");
		
		return placeHolder;
	}
	
	private Component createControlButtons()
	{
		final HorizontalLayout buttonLayout = new HorizontalLayout();
		buttonLayout.setStyleName(CssStyle.MESSAGE_DIALOG_FOOTER_BUTTONS.getStyleName());
		
		buttonLayout.addComponent(saveHelper.getSaveButton());
		buttonLayout.addComponent(sendHelper.getSaveButton());
		buttonLayout.addComponent(cancelButton);
		
		return buttonLayout;
	}
	
	public Component getControlComponents()
	{
		return controlComponents;
	}
	
	private Panel createDialogPanel()
	{
		final Panel panel = new Panel();
		panel.setSizeFull();
		
		final VerticalLayout wrapper = new VerticalLayout();
		wrapper.setSizeFull();
		
		final VerticalLayout content = new VerticalLayout();
		content.setStyleName(CssStyle.MESSAGE_DIALOG_WRAPPER.getStyleName());
		
		content.addComponent(receiverComboBox);
		content.addComponent(subjectTextField);
		
		content.addComponent(bodyText);
		content.addComponent(emojiBar);
		
		content.addComponent(oldMessageLayout);
		
		final Component attachment = createAttachmentComponent();
		content.addComponent(attachment);
		
		wrapper.addComponent(content);
		wrapper.setComponentAlignment(content, Alignment.MIDDLE_CENTER);
		
		panel.setContent(wrapper);
		
		return panel;
	}
	
	private RichTextArea createBodyRichTextArea()
	{
		//final RichTextArea textArea = messageFieldGroup.buildAndBind(true, I18N.MESSAGEDIALOG_LABEL_BODY.msg(), RichTextArea.class, Message_.body);
		 RichTextArea textArea = new RichTextArea("Message text");
         textArea.setValue("Hallo");
         textArea.setWidth(100, Unit.PERCENTAGE);
		 textArea.setStyleName(CssStyle.MESSAGE_BODY.getStyleName());
		 return textArea;
	}

	private RichTextArea createBodyRichTextAreas()
	{
		final RichTextArea textArea = messageFieldGroup.buildAndBind(true, I18N.MESSAGEDIALOG_LABEL_BODY.msg(), RichTextArea.class, Message_.body);
		textArea.setStyleName(CssStyle.MESSAGE_BODY.getStyleName());

		return textArea;
	}
	
	private Component createEmojiBar()
	{
		final VerticalLayout layout = new VerticalLayout();
		layout.setMargin(new MarginInfo(true, true, true, false));
		
		final MenuBar emojiBar = new MenuBar();
		emojiBar.setStyleName(CssStyle.EMOJI_BAR.getStyleName(), true);
		
		final MenuBar.MenuItem smileys = emojiBar.addItem("", null);
		smileys.setIcon(FontAwesome.SMILE_O);
		smileys.setStyleName(CssStyle.EMOJI_MENU.getStyleName());
		
		final MenuBar.MenuItem relation = emojiBar.addItem("", null);
		relation.setIcon(FontAwesome.HEART_O);
		relation.setStyleName(CssStyle.EMOJI_MENU.getStyleName());
		
		final MenuBar.MenuItem nature = emojiBar.addItem("", null);
		nature.setIcon(FontAwesome.TREE);
		nature.setStyleName(CssStyle.EMOJI_MENU.getStyleName());
		
		final MenuBar.MenuItem things = emojiBar.addItem("", null);
		things.setIcon(FontAwesome.CAR);
		things.setStyleName(CssStyle.EMOJI_MENU.getStyleName());
		
		final Command menuSelected = this::menuSelected;
		
		for (Emoji e : Emoji.values())
		{
			switch (e.getCategory())
			{
				case SMILEYS:
					smileys.addItem("", new ThemeResource(e.getPath()), menuSelected).setDescription(e.getDescription());
					break;
				case RELATIONSHIP:
					relation.addItem("", new ThemeResource(e.getPath()), menuSelected).setDescription(e.getDescription());
					break;
				case NATURE:
					nature.addItem("", new ThemeResource(e.getPath()), menuSelected).setDescription(e.getDescription());
					break;
				case THINGS:
					things.addItem("", new ThemeResource(e.getPath()), menuSelected).setDescription(e.getDescription());
					break;
			}
		}
		
		layout.addComponent(emojiBar);
		
		return layout;
	}
	
	private void menuSelected(MenuItem selectedItem)
	{
		String t = bodyText.getValue();
		String emojiPath = "";
		
		for (Emoji e : Emoji.values())
		{
			if (Objects.equals(e.getDescription(), selectedItem.getDescription()))
				emojiPath = e.getPath();
		}
		
		final String imageUrl = "<img  src=\"VAADIN/themes/gk_theme/" + emojiPath + "\" class=\"v-icon-emoji\" style=\"height: 24px; vertical-align: middle;\">";

			/*
			 cut off mozillas <br> tag and  replaces chromes <div><br></div> tag
			 with &nbsp; at the end of string to avoid empty lines (chrome) and
			 linebreaks (firefox) when inserting emojis
			 */
		if (t == null)
			bodyText.setValue(imageUrl.concat("&nbsp;"));
		else
		{
			if (t.endsWith("<br>")) //firefox
				t = t.substring(0, t.length() - 4).concat("&nbsp;");
			else if (t.endsWith("<div><br></div>"))// chrome
				t = t.replace("<div><br></div>", "&nbsp;");
			bodyText.setValue(t.concat(imageUrl).concat("&nbsp;"));
		}
	}
	
	private Component createAttachmentComponent()
	{
		final VerticalLayout layout = new VerticalLayout();
		layout.setCaption(I18N.MESSAGEDIALOG_LABEL_ATTACHMENT.msg());
		
		final VerticalLayout fileWrapper = new VerticalLayout();
		fileWrapper.setSizeFull();
		fileWrapper.setStyleName(CssStyle.MESSAGE_DIALOG_ATTACHMENT.getStyleName());
		
		tooManyAttachments.setVisible(false);
		
		uploadComponent.setStyleName(CssStyle.FILEUPLOAD.getStyleName());
		uploadComponent.setUploadFileChangedListener((changedUploadFile, uploadResult) ->
		{
			switch (uploadResult)
			{
				case VIRUS:
					Notification.show(I18N.MESSAGEDIALOG_VIRUS.msg(), Type.ERROR_MESSAGE);
					break;
				case FAILED:
					Notification.show(I18N.MESSAGEDIALOG_FAILED.msg(), Type.ERROR_MESSAGE);
					break;
				case SUCCESS:
					messageUploadFiles.addBean(uploadComponent.getUploadFile());
					generateNewUploadFile();
					final HorizontalLayout file = createFileLayout(messageUploadFiles.getItem(messageUploadFiles.lastItemId()).getBean(), fileWrapper);
					fileWrapper.addComponent(file);
					break;
			}
		});
		
		layout.addComponent(tooManyAttachments);
		
		final HorizontalLayout uploadLayout = new HorizontalLayout();
		uploadLayout.addComponent(uploadComponent);
		uploadLayout.setComponentAlignment(uploadComponent, Alignment.BOTTOM_LEFT);
		
		layout.addComponents(uploadComponent.createDragAndDrop(uploadLayout), fileWrapper);
		
		return layout;
	}
	
	private HorizontalLayout createFileLayout(MessageUploadFile file, VerticalLayout fileWrapper)
	{
		final HorizontalLayout wrapper = new HorizontalLayout();
		wrapper.setSizeFull();
		wrapper.setStyleName(CssStyle.MESSAGE_DIALOG_ATTACHMENT.getStyleName());
		
		final Button fileButton = new Button();
		fileButton.setCaption(file.getFilename());
		fileButton.setIcon(FontAwesome.PAPERCLIP);
		fileButton.setEnabled(false);
		
		final Button delete = new Button();
		delete.setIcon(FontAwesome.TRASH_O);
		delete.addClickListener(event -> deleteFile(file, wrapper, fileWrapper));
		
		wrapper.addComponents(fileButton, delete);
		wrapper.setExpandRatio(fileButton, 0.7f);
		wrapper.setExpandRatio(delete, 0.3f);
		wrapper.setComponentAlignment(delete, Alignment.MIDDLE_RIGHT);
		
		return wrapper;
	}
	
	private void deleteFile(MessageUploadFile file, HorizontalLayout wrapper, VerticalLayout fileWrapper)
	{
		messageUploadFiles.removeItem(file);
		wrapper.removeAllComponents();
		wrapper.setVisible(false);
		
		if (fileWrapper.getComponentCount() == 0)
			fileWrapper.setVisible(false);
		
	}
	
	private void generateNewUploadFile()
	{
		uploadComponent.setVisible(true);
		tooManyAttachments.setVisible(false);
		
		try
		{
			uploadComponent.setUploadFile(newUploadCallback != null ? newUploadCallback.newUpload(message, messageUploadFiles.getItemIds()) : null);
		}
		catch (ValidationException e)
		{
			uploadComponent.setVisible(false);
			tooManyAttachments.setVisible(true);
		}
	}
	
	private void hideSaveButton()
	{
		saveHelper.getSaveButton().setEnabled(false);
		saveHelper.getSaveButton().setVisible(false);
	}
	
	public void setSubject(String subject)
	{
		subjectTextField.setValue(subject);
	}
	
	public void setSubjectEnabled(boolean enabled)
	{
		subjectTextField.setEnabled(enabled);
	}
	
	public void setReceiverContainer(User receiver)
	{
		userContainer.removeAllItems();
		if (receiver != null)
		{
			userContainer.addItem(receiver);
			receiverComboBox.addItem(receiver.getAlias());
			receiverComboBox.setValue(receiver.getAlias());
			receiverComboBox.select(0);
			receiverComboBox.addStyleName(CssStyle.ANSWERED.getStyleName());
		}
		receiverComboBox.setEnabled(true);
	}
	
	public void setReceiverContainer(List<User> receiverList)
	{
		userContainer.removeAllItems();
		if (receiverList != null)
		{
			userContainer.addAll(receiverList);
		}
		receiverComboBox.setEnabled(receiverList != null);
	}
	
	private void sendMessage() throws ValidationException
	{
		if (message == null) return;
		
		sendCallback.sendMessage(message, messageUploadFiles.getItemIds());
	}
	
	private void saveMessage() throws ValidationException
	{
		if (message == null) return;
		
		saveCallback.saveMessage(message, messageUploadFiles.getItemIds());
	}
	
	public void setMessage(Message message, List<MessageUploadFile> messageAttachments)
	{
		this.message = message;
		message.setSubject(StringUtils.deSanitizeSpecialCharacters(message.getSubject()));
		messageFieldGroup.setItemDataSource(message);
		
		messageUploadFiles.removeAllItems();
		if (messageAttachments != null)
		{
			messageUploadFiles.addAll(messageAttachments);
		}
		
		generateNewUploadFile();
		
		saveHelper.resetValidationResult();
		sendHelper.resetValidationResult();
	}
	
	public void setSaveCallback(SaveCallback saveCallback)
	{
		this.saveCallback = saveCallback;
		saveHelper.getSaveButton().setVisible(saveCallback != null);
	}
	
	public void setSendCallback(SendCallback sendCallback)
	{
		this.sendCallback = sendCallback;
		sendHelper.getSaveButton().setVisible(sendCallback != null);
	}
	
	public void setCancelCallback(CancelCallback cancelCallback)
	{
		this.cancelCallback = cancelCallback;
		cancelButton.setVisible(cancelCallback != null);
	}
	
	public void setNewUploadCallback(NewUploadCallback newUploadCallback)
	{
		this.newUploadCallback = newUploadCallback;
		
		generateNewUploadFile();
	}
	
	public void setOldMessage(String oldMessage)
	{
		oldMessageLabel.setValue(oldMessage);
		oldMessageLayout.setVisible(true);
	}
}

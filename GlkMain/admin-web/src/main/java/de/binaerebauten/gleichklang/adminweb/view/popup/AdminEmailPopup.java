package de.binaerebauten.gleichklang.adminweb.view.popup;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import de.binaerebauten.gleichklang.core.model.I18NEntity;
import de.binaerebauten.gleichklang.core.model.user.MemberStatus;
import de.binaerebauten.gleichklang.core.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.server.FileResource;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.Notification;
import com.vaadin.ui.RichTextArea;
import com.vaadin.ui.Upload;
import com.vaadin.ui.VerticalLayout;

import de.binaerebauten.gleichklang.core.model.mail.AdminEmail;
import de.binaerebauten.gleichklang.core.model.user.Admin;
import de.binaerebauten.gleichklang.core.service.MessageService;
import de.binaerebauten.gleichklang.core.service.mail.MailReceiveService;
import de.binaerebauten.gleichklang.core.service.validator.ValidationException;
import de.binaerebauten.gleichklang.core.view.component.AdminEmailListComponent;
import de.binaerebauten.gleichklang.core.view.component.Popup;
import de.binaerebauten.gleichklang.core.view.component.message.I18N;
import de.binaerebauten.gleichklang.core.view.component.validator.SaveHelper;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;


public class AdminEmailPopup extends Popup {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8891200457828936692L;
	private static final Logger LOG = LoggerFactory.getLogger(AdminEmailPopup.class);
	private VerticalLayout parentLayout, dataTextLayout, replyLayout;
	private HorizontalLayout buttonLayout;
	protected SaveHelper saveHelper;
	private RichTextArea emailText, replyTextField;
	private Button markDeleted, markRead, reply;
	Admin currentAdmin;
	AdminEmail email;
	private Upload upload= new Upload();
	//private final BeanItemContainer<MessageUploadFile> messageUploadFiles = new BeanItemContainer<>(MessageUploadFile.class);;
	private final Label tooManyAttachments = new Label(I18N.MESSAGEDIALOG_MAX_ATTACHMENTS.msg());
	private static final int MAX_ATTACHMENTS = 4;
	final VerticalLayout fileWrapper = new VerticalLayout();
	public List<File> attachmentList = new ArrayList();

	private final MailReceiveService mailReceiveService;
    private final MessageService messageService;

	public AdminEmailPopup(MailReceiveService mailReceiveService, MessageService messageService, AdminEmailListComponent.Operation operation, AdminEmail email, Admin currentAdmin) {

		super();
		this.currentAdmin = currentAdmin;
		this.email = email;
		setStyleName(CssStyle.ADMIN_MESSAGE_POPUP.getStyleName());
		parentLayout = new VerticalLayout();
		dataTextLayout = new VerticalLayout();
		replyLayout = new VerticalLayout();
		buttonLayout = new HorizontalLayout();
		parentLayout.setMargin(true);
		parentLayout.setSpacing(true);

		replyTextField = new RichTextArea("Email Reply");
		replyTextField.setWidth("700px");
		replyLayout.addComponent(replyTextField);
		replyTextField.setRequired(true);
		replyLayout.setMargin(true);
		replyLayout.setComponentAlignment(replyTextField, Alignment.MIDDLE_LEFT);
		final Component attachment = createAttachmentComponent();
		replyLayout.addComponent(attachment);

		replyLayout.setVisible(false);
		this.mailReceiveService = mailReceiveService;
		this.messageService = messageService;
		this.setWidth("1200px");
		this.setHeight("500px");
		setStyleName(CssStyle.ADMIN_MESSAGE_POPUP.getStyleName());
		parentLayout.setStyleName(CssStyle.ADMIN_LIST_MESSAGES_WRAPPER.getStyleName());
		switch (operation) {
			case READ:
			case REPLY:
				setCaption("Reply Mail");
				performView(email);
				break;
		}
	}

	private boolean performDelete() {
		mailReceiveService.markDeleted(email);
		Notification.show("Email Marked Deleted", Notification.Type.TRAY_NOTIFICATION);
		close();
		return true;
	}

	private boolean performRead() {
		mailReceiveService.markRead(email);
		Notification.show("Email Marked Read", Notification.Type.TRAY_NOTIFICATION);
		close();
		return true;
	}

	private boolean performReply() {
		replyLayout.setVisible(true);
		reply.setCaption("Send Email");
		reply.addClickListener((event) -> {
			checkAndSendReply();
		});
		return true;
	}

	private String prepareReply() {
		StringBuilder replySb = new StringBuilder();
        String footer = "";

        replySb.append("<BR>");
		replySb.append("<BR>");
		replySb.append("<B>Ursprüngliche Nachricht gesendet am: " + email.getSentDate()+"</B>");
		replySb.append("<BR>");
		replySb.append("Absender der Nachricht: " + email.getSender());
		replySb.append("<BR>");
		replySb.append(email.getProcessedMailData());
		replySb.append("<BR>");
		replySb.append("--");
        replySb.append("<BR>");

        try {
            footer = messageService.createEmailFooter(currentAdmin);
        }catch (Exception ex)
        {
            LOG.error("prepareReply", ex);
        }
        replySb.append(footer);
		replySb.append("<BR>");
		replySb.append("<BR>");

		String reply = replySb.toString();



		if(reply!=null)
		{
			reply = reply.replaceAll("> >","<BR>");
			reply = reply.replaceAll("\n","<BR>");

		}






		return reply;

	}

	private void performView(AdminEmail email) {

		dataTextLayout.setWidth("900px");
		dataTextLayout.setStyleName(CssStyle.FIRST_MESSAGE.getStyleName());
		dataTextLayout.setSpacing(true);
		dataTextLayout.setMargin(true);
		markDeleted = new Button("Mark Deleted");
		markRead = new Button("Mark Read");
		reply = new Button("Reply");

		markDeleted.addClickListener((event) -> {
			performDelete();
		});
		markRead.addClickListener((event) -> {
			performRead();
		});
		reply.addClickListener((event) -> {
			performReply();
		});
		buttonLayout.addComponent(markDeleted);
		buttonLayout.addComponent(markRead);
		buttonLayout.addComponent(reply);
		parentLayout.addComponents(buttonLayout);
		parentLayout.addComponent(replyLayout);
		parentLayout.setComponentAlignment(replyLayout, Alignment.MIDDLE_CENTER);
		parentLayout.addComponent(dataTextLayout);
		parentLayout.setComponentAlignment(buttonLayout, Alignment.TOP_LEFT);
		parentLayout.setComponentAlignment(dataTextLayout, Alignment.BOTTOM_CENTER);
		parentLayout.setSpacing(true);
		parentLayout.setMargin(true);
		try {

			String data = mailReceiveService.processEmailData(email);
			StringBuilder resultSb = new StringBuilder();
			resultSb.append("\n");
			resultSb.append(email.getSender());
			resultSb.append("\n");
			resultSb.append("------------------------------------------------------------------------------------");
			resultSb.append("\n");
			resultSb.append(data);


			if (email.isMailDataHTML() || isHTML(data)) {
				Label emaildataLbl = new Label(resultSb.toString());
				emaildataLbl.setContentMode(ContentMode.HTML);
				dataTextLayout.addComponent(emaildataLbl);
			} else {

				Label emaildataLbl = new Label(resultSb.toString());
				emaildataLbl.setContentMode(ContentMode.TEXT);
				//TODO: get this label changed to white usins css
				dataTextLayout.addComponent(emaildataLbl);
			}
			showAttachments(email);

		} catch (Exception ex) {
			LOG.error("performReply", ex);
		}

		setContent(parentLayout);
	}

	private boolean isHTML(String data) {

		if (data != null && (data.contains("HTML") || data.contains("div") || data.contains("table") || data.contains("http://") || data.contains("https://"))) {
			return true;
		}
		return false;
	}

	private void checkAndSendReply() {
		if (replyTextField != null) {


			String val = replyTextField.getValue();


			if (val != null && !val.trim().isEmpty()) {
				AdminEmail reply = new AdminEmail();

				String mailContent = prepareReply();

				val = val + mailContent;
				reply.setText(val);
				//	reply.setReceiver(email.getSender());
				reply.setSubject("RE: " + email.getSubject());
				reply.setReceiver(email.getSender());
				reply.setAttachments(attachmentList);
				mailReceiveService.sendReply(reply);
				replyLayout.setVisible(false);
				replyTextField.setValue("");
				this.reply.setCaption("Reply");
				this.reply.addClickListener((event) -> {
					performReply();
				});

				Notification.show("Reply Sent to " + reply.getReceiver(), Notification.Type.TRAY_NOTIFICATION);
			}

		}
	}

	private void showAttachments(AdminEmail email) {
		if (email.getProcessingState().equals(AdminEmail.ProcessingState.ATTACHMENTPENDING)) {
			dataTextLayout.addComponent(new Label("Attachment download in progress, please try again after some time"));
			return;
		}

		if (email != null && email.getAttachments() != null && !email.getAttachments().isEmpty()) {

			Link link;
			for (File f : email.getAttachments()) {

				link = new Link(f.getName(), new FileResource(f));
				link.setTargetName("_blank");
				link.setIcon(FontAwesome.PAPERCLIP);
				parentLayout.addComponent(link);
				parentLayout.setComponentAlignment(link, Alignment.BOTTOM_LEFT);
			}
		} else {
			LOG.debug("Attachments not found!");
		}

	}


	private Component createAttachmentComponent()
	{
		final VerticalLayout layout = new VerticalLayout();
		layout.setCaption(I18N.MESSAGEDIALOG_LABEL_ATTACHMENT.msg());

		AttachmentUploader uploader = new AttachmentUploader();
		//fileWrapper.setSizeFull();
		fileWrapper.setStyleName(CssStyle.MESSAGE_DIALOG_ATTACHMENT.getStyleName());
		tooManyAttachments.setVisible(false);

		upload.setStyleName(CssStyle.FILEUPLOAD.getStyleName());

		upload.setButtonCaption("Add Attachment");
		//label.setStyleName();
		upload.setReceiver(uploader);
		upload.addSucceededListener(uploader);
		upload.setImmediate(true);
		upload.setHeight("50px");
		upload.setEnabled(true);
		layout.addComponent(tooManyAttachments);
		upload.setVisible(true);
		tooManyAttachments.setVisible(false);

		final HorizontalLayout uploadLayout = new HorizontalLayout();
		uploadLayout.addComponent(upload);
		uploadLayout.setComponentAlignment(upload, Alignment.BOTTOM_LEFT);

		layout.addComponents(upload, fileWrapper);

		return layout;
	}


	private HorizontalLayout createFileLayout(File file)
	{
		final HorizontalLayout wrapper = new HorizontalLayout();
		wrapper.setSizeFull();
		wrapper.setStyleName(CssStyle.MESSAGE_DIALOG_ATTACHMENT.getStyleName());

		final Button fileButton = new Button();
		fileButton.setCaption(file.getName());
		fileButton.setIcon(FontAwesome.PAPERCLIP);
		fileButton.setEnabled(false);

		final Button delete = new Button();
		delete.setIcon(FontAwesome.TRASH_O);
		delete.addClickListener(event -> deleteFile(file, wrapper));

		wrapper.addComponents(fileButton, delete);
		wrapper.setExpandRatio(fileButton, 0.7f);
		wrapper.setExpandRatio(delete, 0.3f);
		wrapper.setComponentAlignment(delete, Alignment.MIDDLE_RIGHT);

		return wrapper;
	}

	private void deleteFile(File file, HorizontalLayout wrapper)
	{
		attachmentList.remove(file);
		wrapper.removeAllComponents();
		wrapper.setVisible(false);

		if (fileWrapper.getComponentCount() == 0)
			fileWrapper.setVisible(false);

	}


	class AttachmentUploader implements Upload.Receiver, Upload.SucceededListener
	{

		private String fileName;
		private  Button saveButton = new Button("save");

		private Double maxFileSize = 1e+7;
		private ByteArrayOutputStream fileData = null;
		ByteArrayOutputStream out = null;

		@Override
		public OutputStream receiveUpload(String filename, String mimeType) {
				this.fileName = filename;
				fileData = new ByteArrayOutputStream();
				return fileData;
			}


		@Override
		public void uploadSucceeded(Upload.SucceededEvent event) {


			try
			{

				if (attachmentList.size() > MAX_ATTACHMENTS)
				{
					throw new ValidationException("Maximale Anhänge überschritten");
				}

			}
			catch (ValidationException e)
			{
				upload.setVisible(false);
				tooManyAttachments.setVisible(true);
			}

			try {
				byte[] fileBytes = fileData.toByteArray();
				File f = mailReceiveService.createAttachmentFile(fileBytes,fileName);
				if(f != null)
				{
					attachmentList.add(f);
					fileWrapper.addComponent(createFileLayout(f));
				}

			}catch (Exception ex)
			{
				LOG.error("uploadSucceded", ex);
			}


		}
	}

}



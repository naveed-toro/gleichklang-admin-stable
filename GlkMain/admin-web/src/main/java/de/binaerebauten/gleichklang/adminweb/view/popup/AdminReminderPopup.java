package de.binaerebauten.gleichklang.adminweb.view.popup;


import com.vaadin.server.FontAwesome;
import com.vaadin.ui.*;


import de.binaerebauten.gleichklang.adminweb.presenter.handler.UserControlHandler;
import de.binaerebauten.gleichklang.core.initializer.AppUI;
import de.binaerebauten.gleichklang.core.model.reminder.AdminReminder;

import de.binaerebauten.gleichklang.core.model.user.Admin;
import de.binaerebauten.gleichklang.core.model.user.User_;
import de.binaerebauten.gleichklang.core.service.ReminderService;
import de.binaerebauten.gleichklang.core.service.UserService;
import de.binaerebauten.gleichklang.core.view.component.*;
import de.binaerebauten.gleichklang.core.view.component.validator.SaveHelper;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


public class AdminReminderPopup extends Popup {
	private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	private String textWidth="500px",popupHeight="600px",popupWidth="600px";
	 private VerticalLayout parentLayout, dataTextLayout;
	 private HorizontalLayout buttonLayout, recurranceLayout ;
	 private VerticalLayout customErrorSpace = new VerticalLayout();
	protected  SaveHelper saveHelper;
	private TextField titleTxtField;
	private  TextArea reminderTxtArea;
	public TextField userNameOrAlias;
	DateField dateField;
	UserControlHandler userControlHandler;
	CheckBox onlyMe, markAsDone;

	Admin currentAdmin;
	AdminReminder currentReminder;

	private  final ReminderService reminderService;

	public enum Operation
	{
		ADD,
		DELETE,
		VIEW,
		EDIT;
	}

	public AdminReminderPopup(ReminderService service, Operation operation, AdminReminder reminder, Admin currentAdmin, UserControlHandler userControlHandler) {

		super();
		this.currentAdmin = currentAdmin;
		currentReminder = reminder;
		this.userControlHandler = userControlHandler;
		setStyleName(CssStyle.ADMIN_MESSAGE_POPUP.getStyleName());
		parentLayout = new VerticalLayout();
		dataTextLayout = new VerticalLayout();
		buttonLayout = new HorizontalLayout();
		parentLayout.setMargin(true);
		parentLayout.setSpacing(true);

		parentLayout.addComponents(customErrorSpace);
		dataTextLayout.setWidth("500px");

		reminderService = service;


		switch (operation)
		{
			case DELETE:
				this.setWidth(popupWidth);
				this.setHeight("200px");
				setCaption("Delete Reminder");
				performDelete(reminder);
				return;
			case ADD:
				this.setWidth(popupWidth);
				this.setHeight(popupHeight);
				setCaption("Add New Reminder");
				performAddReminder();
				break;
			case EDIT:
				this.setWidth(popupWidth);
				this.setHeight(popupHeight);
				setCaption("Edit Reminder");
				performEdit(reminder);
				break;
			case VIEW:
				this.setWidth(popupWidth);
				this.setHeight("500px");
				setCaption("View Reminder");
				performView();
				break;
		}
	}

	private  boolean performAddReminder()
	{
		recurranceLayout = new HorizontalLayout();
		dateField = new DateField("Remind On Date");
		titleTxtField = new TextField("Reminder Title");
		reminderTxtArea = new TextArea("Reminder Text");
		userNameOrAlias = new TextField("Name Or Alias");

		titleTxtField.setMaxLength(50);
		onlyMe = new CheckBox("Show Only To Me");
		recurranceLayout.addComponent(dateField);

		dataTextLayout.addComponent(recurranceLayout);
		dataTextLayout.addComponent(titleTxtField);
		dataTextLayout.addComponent(reminderTxtArea);
		dataTextLayout.addComponent(userNameOrAlias);
		//dataTextLayout.addComponent(onlyMe);
		titleTxtField.setWidth("400px");
		titleTxtField.setRequired(true);
		reminderTxtArea.setRequired(true);
		dateField.setRequired(true);
		reminderTxtArea.setWidth("400px");
		userNameOrAlias.setWidth("400px");
		dataTextLayout.setSpacing(true);
		dataTextLayout.setMargin(true);
		dataTextLayout.setComponentAlignment(recurranceLayout, Alignment.MIDDLE_LEFT);
		dataTextLayout.setComponentAlignment(titleTxtField, Alignment.MIDDLE_CENTER);
		dataTextLayout.setComponentAlignment(reminderTxtArea, Alignment.MIDDLE_CENTER);
		setContent(parentLayout);
		Button saveButton = new Button("Save"), cancelButton = new Button("Cancel");
		cancelButton.addClickListener(event -> close());
		saveButton.addClickListener(event -> saveReminder(reminderService,true));
		buttonLayout.addComponents(saveButton);
		buttonLayout.addComponents(cancelButton);
		parentLayout.addComponent(dataTextLayout);
		parentLayout.addComponent(buttonLayout);
		parentLayout.setComponentAlignment(buttonLayout, Alignment.MIDDLE_CENTER);
		return true;
	}

	private void saveReminder(ReminderService reminderService, boolean isAdd)
	{

		String title = titleTxtField.getValue();
		String text = reminderTxtArea.getValue();
		Date date = dateField.getValue();
		String nameOrAlias=null;
		if(userNameOrAlias.getValue()!=null && !userNameOrAlias.getValue().isEmpty()) {
			nameOrAlias = userNameOrAlias.getValue().trim();
		}

		AdminReminder reminder;

		if((title == null || title.isEmpty()) || (text == null || text.isEmpty()) || (date == null))
		{
			showCustomError(true,customErrorSpace,"Please enter all the required details ");
			return;
		}
		else {
			showCustomError(false,customErrorSpace,"");
		}


		if(isAdd) {
			reminder = new AdminReminder();
			reminder.setAdmin(currentAdmin);
		}
		else
		{// edit reminder
			reminder = currentReminder;
			if(markAsDone != null && markAsDone.getValue())
				reminder.setReminderStatus(AdminReminder.ReminderStatus.DONE);
			else
				reminder.setReminderStatus(AdminReminder.ReminderStatus.READ);
		}

		reminder.setReminderTitle(titleTxtField.getValue());
		reminder.setReminderText(reminderTxtArea.getValue());
		/*if(onlyMe.getValue())
		{
			reminder.setReminderRecurrence(AdminReminder.ReminderRecurrence.ONLYME);
		}*/
		reminder.setDueDate(new java.sql.Timestamp(
				dateField.getValue().getTime()).toLocalDateTime());
		if(reminderService.save(reminder, nameOrAlias))
		{
			Notification.show("Reminder Saved", Notification.Type.TRAY_NOTIFICATION);
			close();
		}
	}


	void showCustomError(boolean showError, Layout layout , String message)
	{
		if(showError)
		{
			layout.removeAllComponents();
			layout.addComponent(new Label(message));
			layout.setStyleName(EmailTemplateConstants.failure);
			layout.addStyleName(CssStyle.FORM_PART_EMPTY.getStyleName());
			layout.setVisible(true);

		}
		else
		{
			layout.removeAllComponents();
			layout.setVisible(false);
		}
	}




	private  boolean performDelete(AdminReminder reminder)
	{
		Label label = new Label(de.binaerebauten.gleichklang.adminweb.view.popup.I18N.DELETEEMAILPMAPPINGPOPUP_CAPTION_AREYOUSURE.msg(reminder.getReminderTitle()));
		dataTextLayout.addComponent(label);
		Button deleteButton = new Button(de.binaerebauten.gleichklang.adminweb.view.popup.I18N.DELETEEMAILPMAPPINGPOPUP_ACTION_YES.msg()), cancelButton = new Button("Cancel");
		deleteButton.addClickListener(event -> { deleteReminder(reminder.getId(),reminderService);close(); });
		cancelButton.addClickListener(event -> {close();});
		buttonLayout.addComponent(deleteButton);
		buttonLayout.addComponent(cancelButton);
		parentLayout.addComponent(dataTextLayout);
		parentLayout.addComponent(buttonLayout);
		parentLayout.setComponentAlignment(buttonLayout, Alignment.MIDDLE_CENTER);
		setContent(parentLayout);
	return true;
	}

	private void deleteReminder(Long id, ReminderService reminderService)
	{
		try
		{
			reminderService.delete(id);
			Notification.show("Reminder Deleted", Notification.Type.TRAY_NOTIFICATION);

		}
		catch (Exception e)
		{
			Notification.show(de.binaerebauten.gleichklang.adminweb.view.popup.I18N.DELETION_ERROR_MESSAGE.msg(), e.getMessage(), Notification.Type.ERROR_MESSAGE);
		}
	}

	private  boolean performEdit(AdminReminder reminder)
	{
		recurranceLayout = new HorizontalLayout();
		dateField = new DateField("Remind On Date");
		titleTxtField = new TextField("Reminder Title");
		reminderTxtArea = new TextArea("Reminder Text");
		userNameOrAlias = new TextField("Reminder User");
		titleTxtField.setMaxLength(50);
		userNameOrAlias.setMaxLength(50);
		onlyMe = new CheckBox("Show Only To Me");
		markAsDone = new CheckBox(("Mak As Done"));
		recurranceLayout.addComponent(dateField);
		dataTextLayout.addComponent(recurranceLayout);
		dataTextLayout.addComponent(titleTxtField);
		dataTextLayout.addComponent(reminderTxtArea);
		dataTextLayout.addComponent(userNameOrAlias);
		//dataTextLayout.addComponent(onlyMe);
		dataTextLayout.addComponent(markAsDone);
		titleTxtField.setWidth("400px");
		titleTxtField.setRequired(true);
		reminderTxtArea.setRequired(true);
		userNameOrAlias.setWidth("400px");
		dateField.setRequired(true);
		dateField.setValue(java.sql.Timestamp.valueOf(reminder.getDueDate()));
		titleTxtField.setValue(reminder.getReminderTitle());
		reminderTxtArea.setValue(reminder.getReminderText());
		userNameOrAlias.setValue(reminder.getUser()!=null?reminder.getUser().getAlias():"");
		if(reminder.getReminderStatus().equals(AdminReminder.ReminderStatus.DONE))
		{markAsDone.setValue(true);}
		reminderTxtArea.setWidth("400px");
		dataTextLayout.setSpacing(true);
		dataTextLayout.setMargin(true);
		dataTextLayout.setComponentAlignment(recurranceLayout, Alignment.MIDDLE_LEFT);
		dataTextLayout.setComponentAlignment(titleTxtField, Alignment.MIDDLE_CENTER);
		dataTextLayout.setComponentAlignment(reminderTxtArea, Alignment.MIDDLE_CENTER);
		setContent(parentLayout);
		Button saveButton = new Button("Save"), cancelButton = new Button("Cancel");
		cancelButton.addClickListener(event -> close());
		saveButton.addClickListener(event -> saveReminder(reminderService,false));
		buttonLayout.addComponents(saveButton);
		buttonLayout.addComponents(cancelButton);
		parentLayout.addComponent(dataTextLayout);
		parentLayout.addComponent(buttonLayout);
		parentLayout.setComponentAlignment(buttonLayout, Alignment.MIDDLE_CENTER);
		return true;
	}


//return itemId -> itemId.getReplyToMessage() == null ?
//			new Label() : new Label(FontAwesome.ARROW_LEFT.getHtml(), ContentMode.HTML);

	private void performView()
	{

		dataTextLayout.setWidth("570px");
		TableControl<AdminReminder> tableControl;
		final LazyBeanTable<AdminReminder> table = new LazyBeanTable<>();
		table.setSelectable(true);
		table.setMultiSelect(false);
		table.setSizeFull();
		table.addGeneratedColumn("Title", (source, itemId, columnId) -> itemId.getReminderTitle() );
		table.addGeneratedColumn("Due Date", (source, itemId, columnId) -> formatDate(itemId.getDueDate()));
		table.addGeneratedColumn("Status", (source, itemId, columnId) -> itemId.getReminderStatus());
		table.addGeneratedColumn("Added By", (source, itemId, columnId) -> itemId.getAdmin().getAlias());
		table.addGeneratedColumn("User", (source, itemId, columnId) -> itemId.getUser()!=null?itemId.getUser().getAlias():"");
		table.setCellStyleGenerator((source, itemId, columnId) ->
		{

			if (itemId.getReminderStatus() != AdminReminder.ReminderStatus.DONE &&  LocalDateTime.now().isAfter(itemId.getDueDate()))
				return CssStyle.DANGER;
		return null;
		});
		table.setHandler(reminderService.createAdminRemindersHandler(currentAdmin));
		tableControl = new TableControl<>(table);
		tableControl.setSizeFull();


		tableControl.setEditCallback(item ->
		{
			System.out.println(""+item.getReminderStatus());
			item.setReminderStatus(AdminReminder.ReminderStatus.DONE);
			reminderService.save(item,null);
			table.refresh();
		});


		tableControl.setDeleteCallback(item -> {
			reminderService.delete(item.getId());
			Notification.show("Reminder Deleted", Notification.Type.TRAY_NOTIFICATION);
			table.refresh();
		});


		tableControl.setButtonCaptions("","Mark As Done","Delete Reminder");

		tableControl.addButton("Show", (item) -> userControlHandler.openUser(item.getUser()!=null?item.getUser():null));
		//tableControl.addButton("Show", (item) -> userControlHandler.openUser(null));

		int size = table.getSize();


		if (size == 0) {
			dataTextLayout.addComponent(new Label("No Active Reminders found for user " + currentAdmin.getAlias()));

		}
		else
		{
			dataTextLayout.addComponent(tableControl);
			dataTextLayout.setComponentAlignment(tableControl, Alignment.MIDDLE_CENTER); }

		parentLayout.addComponent(dataTextLayout);

		setContent(parentLayout);
	}

	String formatDate(LocalDateTime dateI)
	{
		try
		{
			return dateI.format(formatter);
		}
		catch (Exception e)
		{
			// formatting exception return as is
		}
		return dateI.toString();
	}

}

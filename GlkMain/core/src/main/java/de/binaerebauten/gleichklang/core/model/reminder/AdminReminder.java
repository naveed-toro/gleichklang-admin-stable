package de.binaerebauten.gleichklang.core.model.reminder;

import de.binaerebauten.gleichklang.core.model.BaseEntity;
import de.binaerebauten.gleichklang.core.model.user.Admin;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.security.SanitizeContent;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
@Table(name = "admin_reminder")
public class AdminReminder extends BaseEntity
{


	public enum ReminderStatus
	{
		NEW,
		OPEN,
		DONE,
		IGNORED,
		READ
		
	}



	public enum ReminderRecurrence
	{
		EVERYONE,
		ONLYME
	}

	@ManyToOne
	@JoinColumn(name = "admin_id")
	private Admin admin;
	

	
	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "reminder_status", nullable = false)
	private ReminderStatus reminderStatus = ReminderStatus.NEW;


	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "reminder_recurrence", nullable = false)
	private ReminderRecurrence reminderRecurrence = ReminderRecurrence.EVERYONE;



	@NotBlank(message = "Bitte einen Betreff eingeben")
	@Size(min = 1, max = 255)
	@Column(name = "reminder_text", nullable = false)
	private String reminderText;

	@ManyToOne
	private User user;

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public ReminderRecurrence getReminderRecurrence() {
		return reminderRecurrence;
	}

	public void setReminderRecurrence(ReminderRecurrence reminderRecurrence) {
		this.reminderRecurrence = reminderRecurrence;
	}

	public String getReminderText() {
		return reminderText;
	}

	public void setReminderText(String reminderText) {
		this.reminderText = reminderText;
	}

	public String getReminderTitle() {
		return reminderTitle;
	}

	public void setReminderTitle(String reminderTitle) {
		this.reminderTitle = reminderTitle;
	}

	public LocalDateTime getDueDate() {
		return dueDate;
	}

	public void setDueDate(LocalDateTime dueDate) {
		this.dueDate = dueDate;
	}


	@NotBlank(message = "Bitte einen Betreff eingeben")

	@Size(min = 1, max = 255)
	@Column(name = "reminder_title", nullable = false)
	private String reminderTitle;


	@Column(name = "due_date")
	private LocalDateTime dueDate;

	public Admin getAdmin()
	{
		return admin;
	}
	
	public void setAdmin(Admin admin)
	{
		this.admin = admin;
	}

	public ReminderStatus getReminderStatus()
	{
		return reminderStatus;
	}
	
	public void setReminderStatus(ReminderStatus workItemStatus)
	{
		this.reminderStatus = workItemStatus;
	}
}
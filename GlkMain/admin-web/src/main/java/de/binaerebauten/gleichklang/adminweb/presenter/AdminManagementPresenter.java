package de.binaerebauten.gleichklang.adminweb.presenter;

import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import de.binaerebauten.gleichklang.adminweb.view.AdminManagementView;
import de.binaerebauten.gleichklang.adminweb.view.popup.AdminPopup;
import de.binaerebauten.gleichklang.core.model.user.Admin;
import de.binaerebauten.gleichklang.core.model.user.AdminRole;
import de.binaerebauten.gleichklang.core.presenter.NavigatePresenter;
import de.binaerebauten.gleichklang.core.repository.user.AdminRepository;
import de.binaerebauten.gleichklang.core.service.AdminService;
import de.binaerebauten.gleichklang.core.service.mail.MailQueueService;
import de.binaerebauten.gleichklang.core.service.validator.ValidationException;
import de.binaerebauten.gleichklang.core.view.component.MessageBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.mail.MailException;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

public class AdminManagementPresenter extends NavigatePresenter implements AdminManagementView.AdminManagementListener
{
	private static final Logger LOG = LoggerFactory.getLogger(AdminManagementPresenter.class);

	private final AdminManagementView view;
	private final AdminRepository adminRepository;
	private final AdminService adminService;
	private final MailQueueService mailQueueService;


	public AdminManagementPresenter(ApplicationContext ctx, AdminManagementView view)
	{
		super(view);
		
		this.view = view;

		this.adminService = ctx.getBean(AdminService.class);
		this.adminRepository = ctx.getBean(AdminRepository.class);
		this.mailQueueService = ctx.getBean(MailQueueService.class);



		view.setListener(this);
	}

	@Override
	public void newAdmin()
	{
		final Admin admin = new Admin();
		admin.setRoles(new HashSet<>(Arrays.asList(AdminRole.values())));
		final AdminPopup popup = new AdminPopup(admin, this::onSaveAdmin, this::onResetPassword);
		popup.addCloseListener(e -> refreshView());
		tryOpenPopup(popup);
	}

	@Override
	public void editAdmin(Admin admin)
	{
		final AdminPopup popup = new AdminPopup(admin, this::onSaveAdmin, this::onResetPassword);
		popup.addCloseListener(e -> refreshView());
		tryOpenPopup(popup);
	}


	private void onResetPassword(Admin admin)
			throws ValidationException, MailException
	{
		adminService.save(admin, true);
	}

	private void onSaveAdmin(Admin admin) throws ValidationException, MailException
	{
		boolean resetPassword = admin.getPassword() == null;

		adminService.save(admin, resetPassword);
	}

	@Override
	public void deleteAdmin(Admin admin)
	{
		try
		{
			adminService.delete(admin);
			refreshView();
		}
		catch (ValidationException e)
		{
			Notification.show(e.getMessage(), Notification.Type.WARNING_MESSAGE);
		}
	}

	@Override
	public void removeExcedingMails() {
		MessageBox.show("Clear Pending Mails", MessageBox.MessageBoxButtons.OK, dialogResult ->
		{
			if (dialogResult.equals(MessageBox.DialogResult.OK))
                mailQueueService.removeExcedingMails();
				UI.getCurrent().getPage().reload();
		});
	}

	@Override
	public int countPendingMails() {
		return mailQueueService.countPendingMails();
	}

	@Override
	public void enter(String parameters)
	{
		refreshView();
	}

	private void refreshView()
	{
		view.setAdminHandler(adminRepository::findAll);
	}
}

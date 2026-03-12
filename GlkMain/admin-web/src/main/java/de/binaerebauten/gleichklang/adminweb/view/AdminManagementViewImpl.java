package de.binaerebauten.gleichklang.adminweb.view;

import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import de.binaerebauten.gleichklang.adminweb.view.component.SubscriptionTable;
import de.binaerebauten.gleichklang.core.initializer.AppUI;
import de.binaerebauten.gleichklang.core.model.user.Admin;
import de.binaerebauten.gleichklang.core.model.user.AdminRole;
import de.binaerebauten.gleichklang.core.model.user.Admin_;
import de.binaerebauten.gleichklang.core.repository.AudioRepository;
import de.binaerebauten.gleichklang.core.repository.mail.MailQueueEntryRepository;
import de.binaerebauten.gleichklang.core.service.mail.MailQueueService;
import de.binaerebauten.gleichklang.core.view.*;
import de.binaerebauten.gleichklang.core.view.component.LazyBeanItemContainer;
import de.binaerebauten.gleichklang.core.view.component.LazyBeanTable;
import de.binaerebauten.gleichklang.core.view.component.TableControl;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.stream.Collectors;

public class AdminManagementViewImpl extends AbstractNavigateView<AdminManagementView.AdminManagementListener>
		implements AdminManagementView
{
	private final AbstractOrderedLayout layout;
	private final LazyBeanTable<Admin> table;
	private final TableControl<Admin> adminControl;
    @Autowired
    private MailQueueService mailQueueService;

	Button removeExcedingMails;

	public AdminManagementViewImpl()
	{
		layout = new VerticalLayout();
		layout.setSpacing(true);
		layout.setWidth(100, Unit.PERCENTAGE);
        mailQueueService = AppUI.getApplicationContext().getBean(MailQueueService.class);
		table = createAdminTable();
		adminControl = createAdminControl();
		if(mailQueueService.countPendingMails()>1000) {
			removeExcedingMails = deleteExcedingMailsButton();
			layout.addComponent(deleteExcedingMailsButton());
		}
        addAttachListener(c->getListener().countPendingMails());
		layout.addComponent(adminControl);
		setCompositionRoot(layout);

	}

	private TableControl<Admin> createAdminControl()
	{
		final TableControl<Admin> tableControl = new TableControl<>(createAdminTable());
		tableControl.setMargin(true);

		tableControl.setNewCallback(() -> fireEvent(AdminManagementListener::newAdmin));
		tableControl.setEditCallback(item -> fireEvent(eventAction -> eventAction.editAdmin(item)));
		tableControl.setDeleteCallback(item -> fireEvent(eventAction -> eventAction.deleteAdmin(item)));


		return tableControl;
	}

	public Button deleteExcedingMailsButton(){
		Button button = new Button();
		button.setCaption("Remove Pending Mails");
		button.addClickListener(clickEvent -> getListener().removeExcedingMails());
		return button;
	}

	private LazyBeanTable<Admin> createAdminTable()
	{
		final LazyBeanTable<Admin> table = new LazyBeanTable<>();
		table.setSelectable(true);
		table.setSizeFull();

		table.addContainerProperty(I18N.ADMINMANAGEMENTVIEW_TABLE_ALIAS.msg(), Admin_.alias);
		table.addContainerProperty(I18N.ADMINMANAGEMENTVIEW_TABLE_LASTNAME.msg(), Admin_.lastName);
		table.addContainerProperty(I18N.ADMINMANAGEMENTVIEW_TABLE_FIRSTNAME.msg(), Admin_.firstName);
		table.addContainerProperty(I18N.ADMINMANAGEMENTVIEW_TABLE_EMAIL.msg(), Admin_.email);
		table.addGeneratedColumn(I18N.ADMINMANAGEMENTVIEW_TABLE_ROLES.msg(), (source, itemId, columnId) -> itemId.getRoles().stream().map
				(AdminRole::name).collect(Collectors.joining(", ")));
		return table;
	}

	@Override
	public void setAdminHandler(LazyBeanItemContainer.LazyBeanFilteredItemsHandler<Admin> handler) {
		adminControl.getTable().setHandler(handler);
	}

}

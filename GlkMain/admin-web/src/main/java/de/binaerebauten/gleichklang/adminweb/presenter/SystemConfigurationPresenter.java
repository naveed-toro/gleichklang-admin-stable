package de.binaerebauten.gleichklang.adminweb.presenter;

import de.binaerebauten.gleichklang.adminweb.view.SystemConfigurationView;
import de.binaerebauten.gleichklang.adminweb.view.popup.systemconfig.*;
import de.binaerebauten.gleichklang.core.model.systemconfig.EmailDomainMapping;
import de.binaerebauten.gleichklang.core.model.systemconfig.EmailTemplateMapping;
import de.binaerebauten.gleichklang.core.model.user.Admin;
import de.binaerebauten.gleichklang.core.presenter.NavigatePresenter;
import de.binaerebauten.gleichklang.core.repository.systemconfig.EmailDomainMappingRepository;
import de.binaerebauten.gleichklang.core.repository.systemconfig.EmailTemplateRepository;
import de.binaerebauten.gleichklang.core.service.AdminService;
import de.binaerebauten.gleichklang.core.service.DynamicContentTemplateService;
import de.binaerebauten.gleichklang.core.service.mail.MailSendService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.util.*;

public class SystemConfigurationPresenter extends NavigatePresenter implements SystemConfigurationView.SystemConfigurationViewListener
{


	private static final Logger LOG = LoggerFactory.getLogger(SystemConfigurationPresenter.class);

	private final SystemConfigurationView view;

	private final EmailDomainMappingRepository emailDomainMappingRepository;

	private final EmailTemplateRepository emailTemplateRepository;

	private final DynamicContentTemplateService templateService;

	private final MailSendService mailSendService;
	final Admin currentAdmin;



	public SystemConfigurationPresenter(ApplicationContext ctx, SystemConfigurationView view)
	{
		super(view);

		this.view = view;
		emailDomainMappingRepository = ctx.getBean(EmailDomainMappingRepository.class);
		emailTemplateRepository = ctx.getBean(EmailTemplateRepository.class);
		templateService = ctx.getBean(DynamicContentTemplateService.class);
		mailSendService = ctx.getBean(MailSendService.class);
		final AdminService adminService = ctx.getBean(AdminService.class);
		 currentAdmin = adminService.getCurrentUser();
		view.setListener(this);
	}

	@Override
	public void enter(String parameters)
	{
		refreshView();
	}

	private void refreshView()
	{

		view.setEmailDomainMappingsHandler(emailDomainMappingRepository::findAll);
		view.setEmailTemplateMappingsHandler(emailTemplateRepository::findAll);
	}

	@Override
	public void addNewEmailDomainMapping() {
		final AddEmailDomainMappingPopup popup = new AddEmailDomainMappingPopup(emailDomainMappingRepository);
		popup.addCloseListener(e -> refreshView());
		tryOpenPopup(popup);
	}

	@Override
	public void editEmailDomainMapping(EmailDomainMapping mapping) {
        final EditEmailDomainMappingPopup popup = new EditEmailDomainMappingPopup(mapping,emailDomainMappingRepository);
        popup.addCloseListener(e -> refreshView());
        tryOpenPopup(popup);
	}

	@Override
	public void deleteEmailDomainMapping(EmailDomainMapping mapping) {
		final ConfirmDeletePopup popup = new ConfirmDeletePopup(mapping,emailDomainMappingRepository);
		popup.addCloseListener(e -> refreshView());
		tryOpenPopup(popup);
	}

	@Override
	public void activeInactiveEmailDomainMapping(EmailDomainMapping mapping)
	{
		final ConfirmActiveInactivePopup popup = new ConfirmActiveInactivePopup(mapping,emailDomainMappingRepository);
		popup.addCloseListener(e -> refreshView());
		tryOpenPopup(popup);
	}

	@Override
	public void deleteEmailTemplate(EmailTemplateMapping mapping) {

	}

	@Override
	public void activeInactiveEmailTemplateMapping(EmailTemplateMapping mapping) {

		final ConfirmActiveInactiveEmailTemplatePopup popup = new ConfirmActiveInactiveEmailTemplatePopup(mapping,emailTemplateRepository);
		popup.addCloseListener(e -> refreshView());
		tryOpenPopup(popup);

	}

	@Override
	public void editEmailTemplate(EmailTemplateMapping mapping) {

		final EditMailTemplatePopup popup = new EditMailTemplatePopup(mapping,emailTemplateRepository,templateService, mailSendService, currentAdmin);
		popup.addCloseListener(e -> refreshView());
		tryOpenPopup(popup);

	}
}

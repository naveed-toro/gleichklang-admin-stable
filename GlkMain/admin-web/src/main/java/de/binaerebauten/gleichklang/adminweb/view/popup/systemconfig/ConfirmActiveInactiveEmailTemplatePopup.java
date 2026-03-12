package de.binaerebauten.gleichklang.adminweb.view.popup.systemconfig;

import com.vaadin.ui.*;
import de.binaerebauten.gleichklang.adminweb.view.popup.EmailTemplateConstants;
import de.binaerebauten.gleichklang.adminweb.view.popup.I18N;
import de.binaerebauten.gleichklang.core.model.systemconfig.EmailDomainMapping;
import de.binaerebauten.gleichklang.core.model.systemconfig.EmailTemplateMapping;
import de.binaerebauten.gleichklang.core.repository.systemconfig.EmailDomainMappingRepository;
import de.binaerebauten.gleichklang.core.repository.systemconfig.EmailTemplateRepository;
import de.binaerebauten.gleichklang.core.view.component.Popup;
import org.springframework.beans.factory.annotation.Value;

import java.util.Objects;

/**
 * This popup just shows a confirmation to delete email domain mapping.
 */
public class ConfirmActiveInactiveEmailTemplatePopup extends Popup
{

	private  final EmailTemplateRepository repository;
	public ConfirmActiveInactiveEmailTemplatePopup(EmailTemplateMapping mapping, EmailTemplateRepository repository)
	{
		super(EmailTemplateConstants.activeDeactiveMailTemp);
		this.setWidth(EmailTemplateConstants.fiveHundredPX);
		this.setHeight(EmailTemplateConstants.twoHundredPX);

		Objects.requireNonNull(mapping, EmailTemplateConstants.mapping1);
		Objects.requireNonNull(repository, EmailTemplateConstants.repoNull);
		this.repository = repository;

		final VerticalLayout layout = new VerticalLayout();
		layout.setSpacing(true);
		layout.setMargin(true);

		final Label label = new Label();

		String operation;

		if(mapping.isActive())
			operation = I18N.ACTIVEINACTIVEEMAILPMAPPINGPOPUP_ACTION_DEACTIVATE.msg();
		else
			operation = I18N.ACTIVEINACTIVEEMAILPMAPPINGPOPUP_ACTION_ACTIVATE.msg();

		label.setCaption(I18N.ACTIVEINACTIVEEMAILPMAPPINGPOPUP_CAPTION_AREYOUSURE.msg(operation));
		layout.addComponent(label);

		final HorizontalLayout buttonPanel = new HorizontalLayout();
		buttonPanel.setSpacing(true);
		buttonPanel.setMargin(false);

		layout.addComponent(buttonPanel);


		final Button activeInactiveButton = new Button(I18N.DELETEEMAILPMAPPINGPOPUP_ACTION_YES.msg());

		activeInactiveButton.addClickListener(event -> activeInactiveMapping(mapping));

		buttonPanel.addComponent(activeInactiveButton);



		setContent(layout);
	}

	private void activeInactiveMapping(EmailTemplateMapping mapping)
	{
		try
		{
			repository.updateActiveEmailTemplate(!mapping.isActive(),mapping.getId());
			close();
		}
		catch (Exception e)
		{
			Notification.show(I18N.ACTIVE_INACTIVE_ERROR_MESSAGE.msg(), e.getMessage(), Notification.Type.ERROR_MESSAGE);
		}
	}
}

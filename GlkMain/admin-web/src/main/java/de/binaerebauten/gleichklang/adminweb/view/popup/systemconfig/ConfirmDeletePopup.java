package de.binaerebauten.gleichklang.adminweb.view.popup.systemconfig;

import com.vaadin.ui.*;
import de.binaerebauten.gleichklang.adminweb.view.popup.EmailTemplateConstants;
import de.binaerebauten.gleichklang.adminweb.view.popup.I18N;
import de.binaerebauten.gleichklang.core.model.systemconfig.EmailDomainMapping;
import de.binaerebauten.gleichklang.core.repository.systemconfig.EmailDomainMappingRepository;
import de.binaerebauten.gleichklang.core.view.component.Popup;

import java.util.Objects;

/**
 * This popup just shows a confirmation to delete email domain mapping.
 */
public class ConfirmDeletePopup extends Popup
{



	private  final EmailDomainMappingRepository repository;
	public ConfirmDeletePopup(EmailDomainMapping mapping,EmailDomainMappingRepository repository)
	{
		super(I18N.DELETEEMAILPMAPPINGPOPUP_CAPTION_TITLE.msg());

		this.setWidth(EmailTemplateConstants.fiveHundredPX);
		this.setHeight(EmailTemplateConstants.twoHundredPX);

		Objects.requireNonNull(mapping, EmailTemplateConstants.mapping);
		Objects.requireNonNull(repository, EmailTemplateConstants.repoNull);
		this.repository = repository;

		final VerticalLayout layout = new VerticalLayout();
		layout.setSpacing(true);
		layout.setMargin(true);

		final Label label = new Label(I18N.DELETEEMAILPMAPPINGPOPUP_CAPTION_AREYOUSURE.msg(mapping.getMappingName()));
		layout.addComponent(label);

		final HorizontalLayout buttonPanel = new HorizontalLayout();
		buttonPanel.setSpacing(true);
		buttonPanel.setMargin(false);

		layout.addComponent(buttonPanel);

		final Button deleteButton = new Button(I18N.DELETEEMAILPMAPPINGPOPUP_ACTION_YES.msg());
		deleteButton.addClickListener(event -> deleteMapping(mapping));

		buttonPanel.addComponent(deleteButton);

		setContent(layout);
	}

	private void deleteMapping(EmailDomainMapping mapping)
	{
		try
		{
			repository.deleteById(mapping.getId());
			close();
		}
		catch (Exception e)
		{
			Notification.show(I18N.DELETION_ERROR_MESSAGE.msg(), e.getMessage(), Notification.Type.ERROR_MESSAGE);
		}
	}
}

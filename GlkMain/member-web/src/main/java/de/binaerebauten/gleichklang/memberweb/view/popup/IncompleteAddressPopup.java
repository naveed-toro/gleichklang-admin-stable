package de.binaerebauten.gleichklang.memberweb.view.popup;

import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.VerticalLayout;
import de.binaerebauten.gleichklang.core.model.user.Address;
import de.binaerebauten.gleichklang.core.model.user.Address_;
import de.binaerebauten.gleichklang.core.view.component.AddressPanel;
import de.binaerebauten.gleichklang.core.view.component.ComponentGroup;
import de.binaerebauten.gleichklang.core.view.component.FormPanel;
import de.binaerebauten.gleichklang.core.view.component.Popup;
import de.binaerebauten.gleichklang.core.view.component.validator.ValidationComponent;
import de.binaerebauten.gleichklang.core.view.component.validator.ValidationResult;
import de.binaerebauten.gleichklang.memberweb.view.I18N;

public class IncompleteAddressPopup extends Popup
{
	private final FormPanel rootPanel;
	private final ComponentGroup<Address> addressComponentGroup;
	private final AddressPanel addressPanel;
	private final ValidationComponent validationComponent;

	public IncompleteAddressPopup(AddressPanel addressPanel)
	{
		VerticalLayout rootLayout = new VerticalLayout();

		validationComponent = new ValidationComponent();

		this.addressPanel = addressPanel;
		rootPanel = new FormPanel();
		rootPanel.setMargin(true);
		rootPanel.setDescription(I18N.INCOMPLETE_QUESTIONS_ADDRESS_CHECK_DESCRIPTION.msg());

		addressComponentGroup = new ComponentGroup<>(Address.class);
		addressComponentGroup.setItemDataSource(addressPanel.getAddress());

		validationComponent.addFields(addressPanel.getFields());

		addressPanel.switchZipSelection(false);

		CheckBox checkBox = addressComponentGroup.buildAndBind(true, I18N.INCOMPLETE_QUESTIONS_ADDRESS_CORRECT_LABEL.msg(), CheckBox.class, Address_.checked);

		validationComponent.addFields(checkBox);

		rootPanel.addComponent(addressPanel);
		rootPanel.addFormElement(checkBox);

		rootLayout.addComponents(validationComponent, rootPanel);

		setDraggable(false);
		setContent(rootLayout);
	}


	public void setSaveListener(ClickListener listener){
		rootPanel.setSaveListener(listener);
	}

	public void commit(ValidationResult validationResult)
	{
		try
		{
			addressComponentGroup.commit();
			addressPanel.commit();
		}
		catch (CommitException e)
		{
			validationResult.addInvalidFields(e.getInvalidFields());
		}
		validationComponent.showValidationResult(validationResult);
	}
	
	public ValidationComponent getValidationComponent()
	{
		return validationComponent;
	}
}

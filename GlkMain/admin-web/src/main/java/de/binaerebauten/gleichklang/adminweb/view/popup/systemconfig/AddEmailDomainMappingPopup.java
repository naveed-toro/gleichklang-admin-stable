package de.binaerebauten.gleichklang.adminweb.view.popup.systemconfig;

import com.vaadin.ui.*;
import de.binaerebauten.gleichklang.adminweb.view.popup.EmailTemplateConstants;
import de.binaerebauten.gleichklang.adminweb.view.popup.I18N;

import de.binaerebauten.gleichklang.core.model.systemconfig.EmailDomainMapping;
import de.binaerebauten.gleichklang.core.model.systemconfig.EmailDomainMapping_;
import de.binaerebauten.gleichklang.core.repository.systemconfig.EmailDomainMappingRepository;
import de.binaerebauten.gleichklang.core.service.validator.ValidationException;
import org.springframework.beans.factory.annotation.Value;


import java.util.ArrayList;
import java.util.List;

public class AddEmailDomainMappingPopup extends EmailDomainMappingPopups
{


	public interface SaveCallback
	{
		void save(EmailDomainMapping mapping) throws ValidationException;
	}

	public AddEmailDomainMappingPopup(EmailDomainMappingRepository repository)
	{
		this(new EmailDomainMapping(), repository);
	}

	public AddEmailDomainMappingPopup(EmailDomainMapping mapping, EmailDomainMappingRepository repository)
	{
		super(mapping,repository);
		//this.saveCallback = saveCallback;

		addMoreButton.addClickListener((clickEvent)->{addMore();});
		nameTextField.addValueChangeListener((clickEvent)->{checkIfNameExists();});
		parentLayout.addComponents(saveHelper.getValidationComponent(), customErrorSpace, createMappingNameSection(), createMappingValuesSection(), createControlButtons());
		setContent(parentLayout);
	}


	private Component createMappingNameSection()
	{
		nameLayout.setMargin(true);
		nameLayout.setSpacing(true);
		nameLayout.addComponent(nameTextField);
		nameLayout.addComponent(customErrorSpace);

		saveHelper.addFields(nameTextField);

		return nameLayout;
	}

	private Component createMappingValuesSection()
	{
		this.valueTextField.add(componentGroup.buildAndBind(true,I18N.EMAIL_DOMAIN_MAPPING_POPUP_MAPPING_VALUE.msg(),TextField.class,EmailDomainMapping_.mappingValue));
		this.valueTextField.add(new TextField());
		valueTextField.get(0).setWidth(textWidth);
		valueTextField.get(1).setWidth(textWidth);
		dataTextLayout.setMargin(true);
		valueTextField.get(0).setRequired(true);
		valueTextField.get(1).setRequired(true);
		dataTextLayout.addComponent(valueTextField.get(0));
		dataTextLayout.addComponent(valueTextField.get(1));
		saveHelper.addFields(valueTextField.get(0));
		saveHelper.addFields(valueTextField.get(0));

		return dataTextLayout;
	}

	private Component createControlButtons()
	{
		buttonLayout.setMargin(true);
		buttonLayout.addComponent(addMoreButton);
		buttonLayout.addComponents(saveHelper.getSaveButton());
		return buttonLayout;
	}

	void checkIfNameExists()
	{
		customErrorSpace.setVisible(false);
		String enteredMappingName = nameTextField.getValue().toString();
		List<EmailDomainMapping> existingMappingWithSameValues = repository.findByMappingName(enteredMappingName);
		if(existingMappingWithSameValues != null && existingMappingWithSameValues.size() >0) {
			//throw new ValidationException("mapping name already exists");
			showCustomError(true, customErrorSpace, I18N.NAME_ALREADY_EXIST_ERROR_MESSAGE.msg());
		}
	}
	protected void save() throws ValidationException
	{
		customErrorSpace.setVisible(false);
		if(valuesEnteredCorrectAndNonDuplicate()) {
			formEnabled(false); // disable form elements
			repository.saveAndFlush(constructMappingForSave());
		}

	}


	private boolean valuesEnteredCorrectAndNonDuplicate()  throws ValidationException
	{
		String enteredMappingValue, enteredMappingName;
		List<EmailDomainMapping> existingMappingWithSameValues=null;
		List<String> namesOfExistingMappingWithSameValues = new ArrayList<>();

		if(checkGenericValidations()) {
			// check if mapping name exits

			enteredMappingName = nameTextField.getValue().toString();
			existingMappingWithSameValues = repository.findByMappingName(enteredMappingName);
			if (existingMappingWithSameValues != null && existingMappingWithSameValues.size() > 0) {
				//alright = false;
				//showCustomError(true, customErrorSpace, "This name already exists");
				throw new ValidationException(I18N.NAME_ALREADY_EXIST_ERROR_MESSAGE.msg());

			}


			// check if existing mapping value exists
			//get a list of mappings entered by user and check if they exist in the database already; also disable text fields in process
			for (Field f : valueTextField) {
				enteredMappingValue = f.getValue().toString();
				if (enteredMappingValue != null && !enteredMappingValue.isEmpty()) {
					existingMappingWithSameValues = repository.findByMappingValueContaining(enteredMappingValue);
					if (existingMappingWithSameValues != null && existingMappingWithSameValues.size() > 0) {
						for (EmailDomainMapping m : existingMappingWithSameValues) {
							namesOfExistingMappingWithSameValues.add(enteredMappingValue + " under the " + m.getMappingName() + " mapping");
						}
					}
				}

			}
			//
			if (namesOfExistingMappingWithSameValues != null && namesOfExistingMappingWithSameValues.size() >= 1) {
				throw new ValidationException(I18N.MAPPING_VALUE_ALREADY_EXIST_ERROR_MESSAGE.msg() + namesOfExistingMappingWithSameValues.toString());
				//alright = false;
			}
		}

		return true;
	}

}

package de.binaerebauten.gleichklang.adminweb.view.popup.systemconfig;

import com.vaadin.ui.*;
import de.binaerebauten.gleichklang.adminweb.view.popup.I18N;
import de.binaerebauten.gleichklang.core.model.systemconfig.EmailDomainMapping;
import de.binaerebauten.gleichklang.core.model.systemconfig.EmailDomainMapping_;
import de.binaerebauten.gleichklang.core.repository.systemconfig.EmailDomainMappingRepository;
import de.binaerebauten.gleichklang.core.service.validator.ValidationException;
import de.binaerebauten.gleichklang.core.view.component.ComponentGroup;
import de.binaerebauten.gleichklang.core.view.component.Popup;
import de.binaerebauten.gleichklang.core.view.component.validator.SaveHelper;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EditEmailDomainMappingPopup extends EmailDomainMappingPopups
{
	public interface SaveCallback
	{
		void save(EmailDomainMapping mapping) throws ValidationException;
	}


	public EditEmailDomainMappingPopup(EmailDomainMapping mapping, EmailDomainMappingRepository repository)
	{
		super(mapping, repository);
		parentLayout.addComponents(saveHelper.getValidationComponent(), customErrorSpace, createMappingNameSection(), createMappingValuesSection(), createControlButtons());
		setContent(parentLayout);
	}

	private Component createMappingNameSection()
	{
		nameTextField.setWidth(textWidth);
		nameTextField.setValue(mapping.getMappingName());
		nameLayout.setMargin(true);
		nameLayout.setSpacing(true);
		nameLayout.addComponent(nameTextField);
		nameLayout.addComponent(customErrorSpace);
		saveHelper.addFields(nameTextField);
		nameTextField.addValueChangeListener((clickEvent)->{checkIfNameExists();});

		return nameLayout;
	}

	private Component createMappingValuesSection()
	{

		dataTextLayout.setMargin(true);
		this.valueTextField.add(componentGroup.buildAndBind(true,I18N.EMAIL_DOMAIN_MAPPING_POPUP_MAPPING_VALUE.msg(),TextField.class,EmailDomainMapping_.mappingValue));
		this.valueTextField.add(new TextField());
		valueTextField.get(0).setRequired(true);
		valueTextField.get(1).setRequired(true);
		valueTextField.get(0).setWidth(textWidth);
		valueTextField.get(1).setWidth(textWidth);
		dataTextLayout.addComponent(valueTextField.get(0));
		dataTextLayout.addComponent(valueTextField.get(1));
		saveHelper.addFields(valueTextField.get(0));
		saveHelper.addFields(valueTextField.get(1));


		// Now pre populate data
		String mappingValuesAsCSV = mapping.getMappingValue();


		mappingValuesAsCSV.replace("[","");
		mappingValuesAsCSV.replace("]","");

		List<String> mappingValuesList = new ArrayList<String>(Arrays.asList(mappingValuesAsCSV.split(",")));
		TextField t;
		String s;

		if(mappingValuesList != null && mappingValuesList.size() >=2)
		{
			if(mappingValuesList.get(0) != null)
				valueTextField.get(0).setValue(mappingValuesList.get(0).trim());
			if(mappingValuesList.get(1) != null)
				valueTextField.get(1).setValue(mappingValuesList.get(1).trim());

			mappingValuesList.remove(0); // remove 0th
			mappingValuesList.remove(0); // remove 1st which is now 0th
		}
		// add rest of the values
		if(mappingValuesList != null)
		{
			for(String mappingVal : mappingValuesList)
			{
				t = new TextField();
				if(mappingVal != null)
					t.setValue(mappingVal.trim());
//			t.setRequired(true);
				dataTextLayout.addComponent(t);
				saveHelper.addFields(t);
				valueTextField.add(t);
			}}
		// Only 2 mappings are required

		return dataTextLayout;
	}

	private Component createControlButtons()
	{

		addMoreButton.setCaption(I18N.EMAIL_DOMAIN_MAPPING_POPUP_ADD_MORE.msg());
		addMoreButton.addClickListener((clickEvent)->{addMore();});

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
			if(existingMappingWithSameValues.get(0).getId() != mapping.getId())
				showCustomError(true, customErrorSpace, I18N.NAME_ALREADY_EXIST_WITH_MAPPING_ERROR_MESSAGE.msg());
		}
	}



	protected void save() throws ValidationException
	{
		customErrorSpace.setVisible(false);

		if(valuesEnteredCorrectAndNonDuplicate()) {

			formEnabled(false); // disable form elements

			EmailDomainMapping newMapping = constructMappingForSave();
			newMapping.setId(mapping.getId());

			repository.updateEmailDomainMapping(newMapping.getMappingName(),newMapping.getMappingValue(),newMapping.isActive(),newMapping.getId());

		}
	}

	private boolean valuesEnteredCorrectAndNonDuplicate()  throws ValidationException
	{
		String enteredMappingValue, enteredMappingName;
		List<EmailDomainMapping> valuesFromDB=null;
		List<String> namesOfExistingMappingWithSameValues = new ArrayList<>();
		boolean alright = true;

		if(checkGenericValidations()) {

			// check if mapping name exits
			enteredMappingName = nameTextField.getValue().toString();
			valuesFromDB = repository.findByMappingName(enteredMappingName);
			if (valuesFromDB != null && valuesFromDB.size() > 0) {
				//trying to update a name which exists with another ID
				if (valuesFromDB.get(0).getId() != mapping.getId()) {
					throw new ValidationException(I18N.NAME_ALREADY_EXIST_WITH_MAPPING_ERROR_MESSAGE.msg());
				}
			}


			// check if existing mapping value exists
			//get a list of mappings entered by user and check if they exist in the database already; also disable text fields in process
			for (Field f : valueTextField) {
				enteredMappingValue = f.getValue().toString();

				if (enteredMappingValue != null && !enteredMappingValue.isEmpty()) {
					valuesFromDB = repository.findByMappingValueContaining(enteredMappingValue);
					if (valuesFromDB != null && valuesFromDB.size() > 0) {
						//trying to add mapping values which exist with another Id
						if (valuesFromDB.get(0).getId() != mapping.getId()) {

							for (EmailDomainMapping m : valuesFromDB) {
								namesOfExistingMappingWithSameValues.add(enteredMappingValue + " under the " + m.getMappingName() + " mapping");
							}
						}
					}
				}

			}
			//
			if (namesOfExistingMappingWithSameValues != null && namesOfExistingMappingWithSameValues.size() >= 1) {
				throw new ValidationException(I18N.MAPPING_ALREADY_EXIST_ERROR_MESSAGE.msg() + namesOfExistingMappingWithSameValues.toString());

			}
		}

		return alright;
	}

}

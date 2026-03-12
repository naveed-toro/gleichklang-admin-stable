package de.binaerebauten.gleichklang.adminweb.view.popup.systemconfig;

import com.vaadin.ui.*;
import de.binaerebauten.gleichklang.adminweb.view.popup.EmailTemplateConstants;
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
import java.util.List;

public abstract class EmailDomainMappingPopups extends Popup {

    protected final EmailDomainMapping mapping;
    protected final  EmailDomainMappingRepository repository;
    final String textWidth="200px",popupHeight="500px",popupWidth="500px";
    final VerticalLayout parentLayout, dataTextLayout, nameLayout;
    final HorizontalLayout buttonLayout ;
    final VerticalLayout customErrorSpace = new VerticalLayout();
    protected final SaveHelper saveHelper;
    protected final Field nameTextField;
    protected final ArrayList<Field> valueTextField = new ArrayList<>();;
    protected final ComponentGroup componentGroup;
    protected final Button addMoreButton;


    EmailDomainMappingPopups(EmailDomainMapping mapping, EmailDomainMappingRepository repository)
    {
        super(I18N.EMAIL_DOMAIN_MAPPING_POPUP.msg());
        this.mapping = mapping;
        this.repository = repository;
        parentLayout = new VerticalLayout();
        dataTextLayout = new VerticalLayout();
        buttonLayout = new HorizontalLayout();
        nameLayout = new VerticalLayout();
        this.addMoreButton = new Button();
        this.saveHelper = new SaveHelper(this::save);
        this.saveHelper.setShowUnsavedNotification(false);
        componentGroup = new ComponentGroup(EmailDomainMapping.class);
        this.nameTextField = componentGroup.buildAndBind(true,I18N.EMAIL_DOMAIN_MAPPING_POPUP_MAPPING_NAME.msg(),TextField.class, EmailDomainMapping_.mappingName);
        this.setWidth(popupWidth);
        this.setHeight(popupHeight);
        nameTextField.setWidth(textWidth);
        parentLayout.setMargin(true);
        parentLayout.setSpacing(true);
        addMoreButton.setCaption(I18N.EMAIL_DOMAIN_MAPPING_POPUP_ADD_MORE.msg());
    }
    protected abstract void save() throws ValidationException;


    void formEnabled(boolean enabled)
    {
        nameTextField.setEnabled(enabled);
        for(Field f: valueTextField) {
            f.setEnabled(enabled);
        }
        saveHelper.getSaveButton().setEnabled(enabled);
        addMoreButton.setEnabled(enabled);

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
    }

    boolean containsValue(Field t)
    {
        if(t != null && (t.isEmpty() || t.getValue().toString() == null) || t.getValue().toString().isEmpty())
            return false;
        else
            return true;
    }

    boolean checkGenericValidations() throws ValidationException
    {
        int mappingValueCount=0;
        if(!containsValue(nameTextField))
        {
            throw new ValidationException(I18N.NAME_REQUIRED_ERROR_MESSAGE.msg());
            //showCustomError(true,customErrorSpace,"Name is required");
            //return false;
        }
        //Check for the second mapping value as the first one is already being checked
        for(Field t: valueTextField)
        {
            if(containsValue(t))
                mappingValueCount++;
        }
        if(mappingValueCount <2) {
            //showCustomError(true,customErrorSpace,"At least 2 domain mappings are required");
            throw  new ValidationException(I18N.DOMAIN_MAPPINGS_REQUIRED_ERROR_MESSAGE.msg());

        }else if(!checkValidDataFormat())
        {
            //showCustomError(true,customErrorSpace,"Please enter valid mapping values, like gmail.com");
            //return false;
            throw new ValidationException(I18N.VALID_MAPPING_REQUIRED_ERROR_MESSAGE.msg());
        }

        return true;

    }

    boolean checkValidDataFormat()
    {
        for(Field f: valueTextField) {
            if(containsValue(f) && (f.getValue().toString().indexOf(".") == -1 || f.getValue().toString().indexOf(".") ==0)){
                return false;
            }
        }
        return true;
    }

    void addMore()
    {
        Field t = new TextField();
        t.setWidth(textWidth);
        valueTextField.add(t);
        dataTextLayout.addComponent(t);
    }




    EmailDomainMapping constructMappingForSave()
    {
        EmailDomainMapping mappingForDB = new EmailDomainMapping();
        String csvValue;
        List<String> enteredMappingValues = new ArrayList<>();
        mappingForDB.setActive(true);
        mappingForDB.setDeleted(false);
        mappingForDB.setMappingName(nameTextField.getValue().toString());

        // construct CSV for mapping value
        for(Field f: valueTextField) {
            if(!f.isEmpty() && f.getValue() != null && !f.getValue().toString().isEmpty())
                enteredMappingValues.add(f.getValue().toString());
        }
        csvValue = enteredMappingValues.toString();
        if (csvValue != null)

            csvValue = csvValue.replace("[","");
        csvValue = csvValue.replace("]","");
        csvValue = csvValue.trim();
        if(csvValue.endsWith(","))
        {
            int  i = csvValue.lastIndexOf(",");
            csvValue = csvValue.substring(0,i);

        }
        mappingForDB.setMappingValue(csvValue);
        return mappingForDB;
    }

}

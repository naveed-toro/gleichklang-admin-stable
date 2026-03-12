package de.binaerebauten.gleichklang.core.view.component;

import com.google.common.base.Strings;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Field;
import com.vaadin.ui.TextField;
import de.binaerebauten.gleichklang.core.model.locatable.*;
import de.binaerebauten.gleichklang.core.model.user.Address;
import de.binaerebauten.gleichklang.core.model.user.Address_;
import de.binaerebauten.gleichklang.core.view.component.MessageBox.MessageBoxButtons;
import de.binaerebauten.gleichklang.core.view.component.MessageBox.MessageBoxStyle;
import de.binaerebauten.gleichklang.core.service.LocatableHandler;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AddressPanel extends FormPanel
{
	public interface AddressRemoveListener
	{
		void removeAddress(Address address);
	}
	
	private final ComponentGroup<Address> componentGroup;
	private final LocatableHandler addressHandler;
	private final LocatableSelection<Continent> continentSelect;
	private final LocatableSelection<Country> countrySelect;
	private final LocatableSelection<Zip> zipSelect;
	private final List<Field<?>> fields = new ArrayList<>();
	private final Button removeButton;
	private final LocatableSelection<Region> regionSelect;
	private TextField tmpZip;
	private CheckBox zipIsNotExistsCheckbox;
	
	private AddressRemoveListener addressRemoveListener = null;
	
	public AddressPanel(Address address, LocatableHandler addressHandler, int addressNumber)
	{
		Objects.requireNonNull(address);
		Objects.requireNonNull(addressHandler);
		
		this.addressHandler = addressHandler;
		componentGroup = new ComponentGroup<>(Address.class, address);
		
		this.removeAllComponents();
		
		continentSelect = new LocatableSelection<>(Continent.class, true);
		componentGroup.bind(continentSelect, Address_.continent);

		fields.add(continentSelect);
		
		countrySelect = new LocatableSelection<>(Country.class, true);

		componentGroup.bind(countrySelect, Address_.country);
        countrySelect.setEnabled(false);
		fields.add(countrySelect);
		
		regionSelect = new LocatableSelection<>(Region.class, true);

		componentGroup.bind(regionSelect, Address_.region);
        regionSelect.setEnabled(false);
		fields.add(regionSelect);
		
		zipSelect = new LocatableSelection<>(Zip.class, false);
		componentGroup.bind(zipSelect, Address_.zip);
        zipSelect.setEnabled(false);
		fields.add(zipSelect);
		
		final Field<?> citySelect = componentGroup.buildAndBind(I18N.ADDRESS_CAPTION_CITY.msg(), Address_.city);

		continentSelect.addStyleName(continentSelect.getValue() == null ? CssStyle.EMPTY_ANSWER.getStyleName() : CssStyle.ANSWERED.getStyleName());
		regionSelect.addStyleName(regionSelect.getValue() == null ? CssStyle.EMPTY_ANSWER.getStyleName() : CssStyle.ANSWERED.getStyleName());
		countrySelect.addStyleName(countrySelect.getValue() == null ? CssStyle.EMPTY_ANSWER.getStyleName() : CssStyle.ANSWERED.getStyleName());
		zipSelect.addStyleName(zipSelect.getValue() == null ? CssStyle.EMPTY_ANSWER.getStyleName() : CssStyle.ANSWERED.getStyleName());

		citySelect.addStyleName(citySelect.getValue() == null ? CssStyle.EMPTY_ANSWER.getStyleName() : CssStyle.ANSWERED.getStyleName());

		fields.add(citySelect);
		
		final Field<?> streetWithNumber = componentGroup.buildAndBind(I18N.ADDRESS_CAPTION_STREETWITHNUMBER.msg(), Address_.streetWithNumber);

		streetWithNumber.addStyleName(streetWithNumber.getValue() == null ? CssStyle.EMPTY_ANSWER.getStyleName() : CssStyle.ANSWERED.getStyleName());

		fields.add(streetWithNumber);
		
		addFormElement(this.continentSelect);
		addFormElement(this.countrySelect);
		addFormElement(this.regionSelect);
		addFormElement(this.zipSelect);

        this.continentSelect.addValueChangeListener(event -> countrySelect.setEnabled(event.getProperty().getValue() != null));
        this.countrySelect.addValueChangeListener(event -> regionSelect.setEnabled(event.getProperty().getValue() != null));
		this.regionSelect.addValueChangeListener(event -> {
            final boolean enabled = event.getProperty().getType() != null;
            zipSelect.setEnabled(enabled);
			zipSelect.addStyleName(zipSelect.getValue() == null ? CssStyle.EMPTY_ANSWER.getStyleName() : CssStyle.ANSWERED.getStyleName());
            tmpZip.setEnabled(enabled);
        });

		citySelect.addValueChangeListener(event ->
		{
			if (event.getProperty().getValue() != null && !event.getProperty().getValue().toString().isEmpty())
			{
				citySelect.removeStyleName(CssStyle.EMPTY_ANSWER.getStyleName());
				citySelect.addStyleName(CssStyle.ANSWERED.getStyleName());
			}
			else
			{
				citySelect.removeStyleName(CssStyle.ANSWERED.getStyleName());
				citySelect.addStyleName(CssStyle.EMPTY_ANSWER.getStyleName());
			}
		});

		streetWithNumber.addValueChangeListener(event ->
		{
			if (event.getProperty().getValue() != null && !event.getProperty().getValue().toString().isEmpty())
			{
				streetWithNumber.removeStyleName(CssStyle.EMPTY_ANSWER.getStyleName());
				streetWithNumber.addStyleName(CssStyle.ANSWERED.getStyleName());
			}
			else
			{
				streetWithNumber.removeStyleName(CssStyle.ANSWERED.getStyleName());
				streetWithNumber.addStyleName(CssStyle.EMPTY_ANSWER.getStyleName());
			}

		});
		
		addZipIsNotExistsComponent();
		
		addFormElement(citySelect);
		addFormElement(streetWithNumber);
		
		removeButton = new Button();
		removeButton.setIcon(FontAwesome.TRASH_O);
		removeButton.setStyleName(CssStyle.PANEL_BUTTON.getStyleName());
		removeButton.setVisible(false);
		removeButton.addClickListener(event -> addressRemoveListener.removeAddress(address));
		
		addComponent(removeButton);
		
		fillComponent();
		
		setAddressNumber(addressNumber);
	}
	
	public void setAddressRemoveListener(AddressRemoveListener addressRemoveListener)
	{
		this.addressRemoveListener = addressRemoveListener;
		removeButton.setVisible(addressRemoveListener != null);
	}
	
	public void setAddressNumber(int addressNumber)
	{
		final String addressCaption;
		
		if (getAddress().isPayment())
		{
			addressCaption = I18N.ADRESSPANEL_CAPTION_PAYMENT_ADDRESS.msg();
		}
		else
		{
			addressCaption = I18N.ADRESSPANEL_CAPTION_ADDRESS.msg(addressNumber);
		}
		
		this.setCaption(addressCaption);
		removeButton.setCaption(I18N.ADDRESS_BTN_REMOVE.msg(addressNumber));
	}
	
	private void addZipIsNotExistsComponent()
	{
		zipIsNotExistsCheckbox = ComponentFactory.getInstance().createField(CheckBox.class, I18N.ADDRESS_ZIP_NOT_EXISTS.msg());
		
		tmpZip = componentGroup.buildAndBind(I18N.ADDRESS_TMP_ZIP.msg(), TextField.class, Address_.tmpZip);
		tmpZip.setMaxLength(10);
		tmpZip.addBlurListener(event -> tmpZip.validate());
        tmpZip.setEnabled(false);
		
		fields.add(tmpZip);
		
		addFormElement(tmpZip);
		addFormElement(zipIsNotExistsCheckbox);
		
		zipIsNotExistsCheckbox.addValueChangeListener(event -> switchZipSelection(zipIsNotExistsCheckbox.getValue()));
		switchZipSelection(!Strings.isNullOrEmpty(tmpZip.getValue()));
	}
	
	public void switchZipSelection(Boolean showZipIsNotExists)
	{
		zipIsNotExistsCheckbox.setValue(showZipIsNotExists);
		
		if (!showZipIsNotExists)
		{
			tmpZip.setValue(null);
		}
		setVisibleFormElement(tmpZip, showZipIsNotExists);
		tmpZip.setComponentError(null);
		
		if (showZipIsNotExists)
		{
			zipSelect.setValue(null);
		}
		setVisibleFormElement(zipSelect, !showZipIsNotExists);
		zipSelect.setComponentError(null);
	}
	
	private void fillComponent()
	{
		continentSelect.updateElements(addressHandler.getContinents());
		
		continentSelect.addValueChangeListener(event -> countrySelect.updateElements(
				addressHandler.getCountries((Continent) continentSelect.getValue())));
		countrySelect.addValueChangeListener(
				event -> regionSelect.updateElements(addressHandler.getRegions((Country) countrySelect.getValue())));
		
		regionSelect.addValueChangeListener(event -> zipSelect.updateElements(addressHandler.getZips((Region) regionSelect.getValue())));
		
		final Address address = componentGroup.getItemDataSource().getBean();
		
		continentSelect.select(address.getContinent());
		countrySelect.select(address.getCountry());
		regionSelect.select(address.getRegion());
		zipSelect.select(address.getZip());
	}
	
	public Address getAddress()
	{
		return componentGroup.getItemDataSource().getBean();
	}
	
	public void commit() throws CommitException
	{
		try
		{
			continentSelect.validate();
			countrySelect.validate();
			zipSelect.validate();
			regionSelect.validate();
			componentGroup.commit();
		}
 		catch (CommitException | InvalidValueException e)
		{
			MessageBox.show(I18N.ADRESSPANEL_COMPONENT_MESSAGE.msg(), MessageBoxButtons.OK, MessageBoxStyle.ATTENTION, null);
			throw new CommitException(e);
		}
	}

	public List<Field<?>> getFields()
	{
		return fields;
	}
}

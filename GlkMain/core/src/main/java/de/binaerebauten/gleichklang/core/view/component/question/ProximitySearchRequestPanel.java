package de.binaerebauten.gleichklang.core.view.component.question;

import com.vaadin.server.FontAwesome;
import com.vaadin.ui.*;
import de.binaerebauten.gleichklang.core.model.locatable.*;
import de.binaerebauten.gleichklang.core.model.locatable.ProximitySearchRequest.ProximityDistance;
import de.binaerebauten.gleichklang.core.service.LocatableHandler;
import de.binaerebauten.gleichklang.core.view.component.ComponentFactory;
import de.binaerebauten.gleichklang.core.view.component.ComponentGroup;
import de.binaerebauten.gleichklang.core.view.component.FormPanel;
import de.binaerebauten.gleichklang.core.view.component.LocatableSelection;
import de.binaerebauten.gleichklang.core.view.component.validator.SaveHelper;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ProximitySearchRequestPanel extends CustomComponent
{
	public interface SaveListener
	{
		void save(ProximitySearchRequest proximitySearchRequest);
	}
	
	private final LocatableHandler locatableHandler;
	private final ComponentGroup<ProximitySearchRequest> componentGroup;
	private final SaveHelper saveHelper;
	private final SaveListener saveListener;

	private ComboBox distanceComboBox;
	
	public ProximitySearchRequestPanel(LocatableHandler locatableHandler, SaveListener saveListener)
	{
		Objects.requireNonNull(locatableHandler);
		Objects.requireNonNull(saveListener);
		
		this.saveListener = saveListener;
		this.locatableHandler = locatableHandler;
		
		saveHelper = new SaveHelper(this::commit, this::save, true);
		saveHelper.setAutoEnabled(true);
		saveHelper.getSaveButton().setCaption(I18N.PROXIMITYSEARCHREQUEST_ACTION_ADD.msg());
		saveHelper.getSaveButton().setIcon(FontAwesome.CHECK);
		saveHelper.getSaveButton().setStyleName(CssStyle.SAVE_BUTTON.getStyleName());
		saveHelper.setShowValidationNotification(false);
		componentGroup = new ComponentGroup<>(ProximitySearchRequest.class);
		
		reset();
	}
	
	private void commit()
	{
		final ProximitySearchRequest proximitySearchRequest = componentGroup.getItemDataSource().getBean();
		final ProximityDistance proximityDistance = (ProximityDistance) distanceComboBox.getValue();
		
		proximitySearchRequest.setDistance(proximityDistance);
	}
	
	private void save()
	{
		saveListener.save(componentGroup.getItemDataSource().getBean());
		reset();
	}
	
	private void reset()
	{
		saveHelper.removeAllFields();
		componentGroup.unbindAll();
		
		componentGroup.setItemDataSource(new ProximitySearchRequest());
		
		setCompositionRoot(createLayout());
	}
	
	private Component createLayout()
	{
		final VerticalLayout layout = new VerticalLayout();

		final FormPanel formPanel = new FormPanel(I18N.PROXIMITYSEARCHREQUEST_CAPTION_TITLE.msg());
		formPanel.setDescription(I18N.PROXIMITYSEARCHREQUEST_CAPTION_DESCRIPTION.msg());

		layout.addStyleName(CssStyle.HIDDEN.getStyleName());

		final Button addPSR = new Button();
		addPSR.setIcon(FontAwesome.PLUS_CIRCLE);
		addPSR.addStyleName(CssStyle.ADD_BUTTON.getStyleName());
		addPSR.setCaption(I18N.PROXIMITYSEARCHREQUEST_BUTTON_ADD_CAPTION.msg());
		addPSR.addClickListener((event) -> {
			layout.removeStyleName(CssStyle.HIDDEN.getStyleName());
			addPSR.setEnabled(false);
		});

		formPanel.addComponent(addPSR);

		final LocatableSelection<Continent> continentSelect = new LocatableSelection<>(Continent.class, true);
		continentSelect.updateElements(locatableHandler.getContinentsWithZips());
		continentSelect.addStyleName(CssStyle.EMPTY_ANSWER.getStyleName());
		
		final LocatableSelection<Country> countrySelect = new LocatableSelection<>(Country.class, true);
		countrySelect.setEnabled(false);
		countrySelect.addStyleName(CssStyle.EMPTY_ANSWER.getStyleName());
		
		final LocatableSelection<Zip> zipSelect = new LocatableSelection<>(Zip.class, true);
		componentGroup.bind(zipSelect, ProximitySearchRequest_.center);
		zipSelect.setEnabled(false);
		zipSelect.addStyleName(CssStyle.EMPTY_ANSWER.getStyleName());
		
		distanceComboBox = createDistanceComboBox();
		
		final Field<?> countryRestrictionComponent = componentGroup.buildAndBind(I18N.PROXIMITYSEARCHREQUEST_CAPTION_COUNTRYRESTRICTION.msg(), ProximitySearchRequest_.restrictCountry);
		countryRestrictionComponent.setEnabled(false);
		
		continentSelect.addValueChangeListener(event ->
		{
			final Continent continent = (Continent) continentSelect.getValue();
			countrySelect.setEnabled(continent != null);
			countrySelect.updateElements(locatableHandler.getCountriesWithZips(continent));
			continentSelect.removeStyleName(CssStyle.EMPTY_ANSWER.getStyleName());
			continentSelect.addStyleName(CssStyle.ANSWERED.getStyleName());
		});
		
		countrySelect.addValueChangeListener(event ->
		{
			final Country country = (Country) countrySelect.getValue();
			zipSelect.updateElements(locatableHandler.getZips(country));
			zipSelect.setEnabled(country != null);
			countrySelect.removeStyleName(CssStyle.EMPTY_ANSWER.getStyleName());
			countrySelect.addStyleName(CssStyle.ANSWERED.getStyleName());

		});
		
		zipSelect.addValueChangeListener(event ->
		{
			distanceComboBox.setEnabled(zipSelect.getValue() != null);
		});
		
		distanceComboBox.addValueChangeListener(event ->
		{
			countryRestrictionComponent.setEnabled(distanceComboBox.getValue() != null);
			distanceComboBox.removeStyleName(CssStyle.EMPTY_ANSWER.getStyleName());
			distanceComboBox.addStyleName(CssStyle.ANSWERED.getStyleName());
		});
		
		final List<Field<?>> fields = Arrays.asList(continentSelect, countrySelect, zipSelect, distanceComboBox, countryRestrictionComponent);
		
		saveHelper.addFields(fields);
		formPanel.addFormElements(fields);

		layout.addComponent(formPanel);

		layout.addComponent(saveHelper.getSaveButton());
		
		return layout;
	}
	
	private ComboBox createDistanceComboBox()
	{
		final ComboBox distanceComboBox = ComponentFactory.getInstance().createField(ProximityDistance.class, ComboBox.class);
		distanceComboBox.setRequired(true);
		distanceComboBox.setEnabled(false);
		distanceComboBox.setCaption(I18N.PROXIMITYSEARCHREQUEST_CAPTION_PROXIMITY.msg());
		distanceComboBox.setInputPrompt(I18N.PROXIMITYSEARCHREQUEST_CAPTION_PROXIMITYPROMPT.msg(I18N.PROXIMITYSEARCHREQUEST_CAPTION_PROXIMITY.msg()));
		distanceComboBox.addStyleName(CssStyle.EMPTY_ANSWER.getStyleName());

		return distanceComboBox;
	}
}

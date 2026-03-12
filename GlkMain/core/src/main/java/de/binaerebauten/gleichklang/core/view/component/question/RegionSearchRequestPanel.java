package de.binaerebauten.gleichklang.core.view.component.question;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.*;
import com.vaadin.ui.AbstractSelect.ItemCaptionMode;
import de.binaerebauten.gleichklang.core.model.locatable.*;
import de.binaerebauten.gleichklang.core.service.LocatableHandler;
import de.binaerebauten.gleichklang.core.view.component.ComponentGroup;
import de.binaerebauten.gleichklang.core.view.component.ExtendablePanel;
import de.binaerebauten.gleichklang.core.view.component.FormPanel;
import de.binaerebauten.gleichklang.core.view.component.LocatableSelection;
import de.binaerebauten.gleichklang.core.view.component.validator.SaveHelper;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;

import java.util.Objects;

public class RegionSearchRequestPanel extends CustomComponent
{
	public interface SaveListener
	{
		void save(RegionSearchRequest regionSearchRequest);
	}
	
	private final LocatableHandler locatableHandler;
	private final ComponentGroup<RegionSearchRequest> componentGroup;
	private final SaveHelper saveHelper;
	private final SaveListener saveListener;

	
	public RegionSearchRequestPanel(LocatableHandler locatableHandler, SaveListener saveListener)
	{
		Objects.requireNonNull(locatableHandler);
		Objects.requireNonNull(saveListener);
		
		this.locatableHandler = locatableHandler;
		this.saveListener = saveListener;
		
		saveHelper = new SaveHelper(this::save);
		saveHelper.setAutoEnabled(true);
		saveHelper.getSaveButton().setCaption(I18N.REGIONSEARCHREQUEST_ACTION_ADD.msg());
		saveHelper.getSaveButton().setIcon(FontAwesome.CHECK);
		saveHelper.getSaveButton().setStyleName(CssStyle.SAVE_BUTTON.getStyleName());
		saveHelper.setShowValidationNotification(false);
		componentGroup = new ComponentGroup<>(RegionSearchRequest.class);
		
		reset();
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
		
		componentGroup.setItemDataSource(new RegionSearchRequest());
		
		setCompositionRoot(createLayout());
	}
	
	private Component createLayout()
	{
		final VerticalLayout layout = new VerticalLayout();

		final FormPanel formPanel = new FormPanel(I18N.REGIONSEARCHREQUEST_CAPTION_TITLE.msg());
		formPanel.setDescription(I18N.REGIONSEARCHREQUEST_CAPTION_DESCRIPTION.msg());

		layout.addStyleName(CssStyle.HIDDEN.getStyleName());

		final Button addRSR = new Button();
		addRSR.setIcon(FontAwesome.PLUS_CIRCLE);
		addRSR.setCaption(I18N.REGIONSEARCHREQUEST_BUTTON_ADD_CAPTION.msg());
		addRSR.addStyleName(CssStyle.ADD_BUTTON.getStyleName());
		addRSR.addClickListener((event) -> {
			layout.removeStyleName(CssStyle.HIDDEN.getStyleName());
			addRSR.setEnabled(false);
		});

		formPanel.addComponent(addRSR);

		final LocatableSelection<Continent> continentSelect = new LocatableSelection<>(Continent.class, true);
		continentSelect.updateElements(locatableHandler.getContinents());
		componentGroup.bind(continentSelect, RegionSearchRequest_.continent);
		
		final LocatableSelection<Country> countrySelect = new LocatableSelection<>(Country.class, false);
		componentGroup.bind(countrySelect, RegionSearchRequest_.country);
		countrySelect.setEnabled(false);
		countrySelect.addStyleName(CssStyle.EMPTY_ANSWER.getStyleName());
		
		final OptionGroup regionSelectionPanel = componentGroup.buildAndBind(OptionGroup.class, RegionSearchRequest_.restrictions);
		regionSelectionPanel.setItemCaptionMode(ItemCaptionMode.PROPERTY);
		regionSelectionPanel.setItemCaptionPropertyId(LocatableEntity.NAME);
		regionSelectionPanel.setNullSelectionAllowed(true);
		
		final ExtendablePanel extendablePanel = new ExtendablePanel(I18N.REGIONSEARCHREQUEST_CAPTION_RESTRICTREGIONS.msg(), regionSelectionPanel, true);
		extendablePanel.setEnabled(false);
		extendablePanel.addValueChangeListener(event -> regionSelectionPanel.setValue(null));
		
		continentSelect.addValueChangeListener(event ->
		{
			final Continent continent = (Continent) continentSelect.getValue();
			countrySelect.updateElements(locatableHandler.getCountries(continent));
			countrySelect.setEnabled(continent != null);

			if (continent != null)
			{
				countrySelect.removeStyleName(CssStyle.EMPTY_ANSWER.getStyleName());
				countrySelect.addStyleName(CssStyle.ANSWERED.getStyleName());
			}
		});
		
		countrySelect.addValueChangeListener(event ->
		{
			final Country country = (Country) countrySelect.getValue();
			
			regionSelectionPanel.setContainerDataSource(new BeanItemContainer<>(Region.class, locatableHandler.getRegions(country)));
			extendablePanel.setChecked(false);
			extendablePanel.setEnabled(country != null);
		});


		
		saveHelper.addFields(componentGroup);
		
		formPanel.addFormElement(continentSelect);
		formPanel.addFormElement(countrySelect);
		formPanel.addFormElement(extendablePanel);

		layout.addComponent(formPanel);

		layout.addComponent(saveHelper.getSaveButton());

		return layout;
	}
}

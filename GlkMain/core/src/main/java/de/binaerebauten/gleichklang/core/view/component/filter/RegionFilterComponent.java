package de.binaerebauten.gleichklang.core.view.component.filter;

import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import de.binaerebauten.gleichklang.core.model.filter.AbstractFilter;
import de.binaerebauten.gleichklang.core.model.filter.RegionFilter;
import de.binaerebauten.gleichklang.core.model.locatable.Continent;
import de.binaerebauten.gleichklang.core.model.locatable.Country;
import de.binaerebauten.gleichklang.core.model.locatable.Region;
import de.binaerebauten.gleichklang.core.view.component.FilterComponent;
import de.binaerebauten.gleichklang.core.view.component.LocatableSelection;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;

public class RegionFilterComponent extends CustomComponent implements FilterComponent
{
	private final RegionFilter filter;

	private final LocatableSelection<Continent> continentSelection;
	private final LocatableSelection<Country> countrySelection;
	private final LocatableSelection<Region> regionSelection;

	public RegionFilterComponent(RegionFilter filter, FilterComponentHandler handler)
	{
		this.filter = filter;

		continentSelection = new LocatableSelection<>(Continent.class, true);
		countrySelection = new LocatableSelection<>(Country.class, false);
		regionSelection = new LocatableSelection<>(Region.class, false);

		continentSelection.addStyleName(CssStyle.EMPTY_ANSWER.getStyleName());
		countrySelection.addStyleName(CssStyle.ANSWERED.getStyleName());
		regionSelection.addStyleName(CssStyle.ANSWERED.getStyleName());

		continentSelection.addValueChangeListener(event ->
		{
			final Continent continent = (Continent) continentSelection.getValue();

			countrySelection.updateElements(null);
			regionSelection.updateElements(null);

			if (continent != null)
			{
				countrySelection.updateElements(handler.getLocatableEntities(Country.class, continent));
				continentSelection.removeStyleName(CssStyle.EMPTY_ANSWER.getStyleName());
				continentSelection.addStyleName(CssStyle.ANSWERED.getStyleName());
			}
		});

		countrySelection.addValueChangeListener(event ->
		{
			final Country country = (Country) countrySelection.getValue();

			regionSelection.updateElements(null);

			if (country != null)
			{
				regionSelection.updateElements(handler.getLocatableEntities(Region.class, country));
			}
		});

		continentSelection.updateElements(handler.getLocatableEntities(Continent.class, null));

		final HorizontalLayout layout = new HorizontalLayout();
		layout.setStyleName(CssStyle.REGION_FILTER_COMPONENT.getStyleName());
		layout.setSpacing(true);

		layout.addComponents(continentSelection, countrySelection, regionSelection);

		setCompositionRoot(layout);
	}

	@Override
	public void commit() throws CommitException
	{
		final Region region = (Region) regionSelection.getValue();
		if (region != null)
		{
			filter.setLocatableEntity(region);
			return;
		}

		final Country country = (Country) countrySelection.getValue();
		if (country != null)
		{
			filter.setLocatableEntity(country);
			return;
		}

		final Continent continent = (Continent) continentSelection.getValue();
		if (continent != null)
		{
			filter.setLocatableEntity(continent);
			return;
		}

		throw new CommitException("Keine Region ausgewählt");
	}

	@Override
	public AbstractFilter getFilter()
	{
		return filter;
	}
}

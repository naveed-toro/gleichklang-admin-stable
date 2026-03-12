package de.binaerebauten.gleichklang.core.view.component.filter;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;
import de.binaerebauten.gleichklang.core.model.filter.AbstractFilter;
import de.binaerebauten.gleichklang.core.model.filter.ProximityFilter;
import de.binaerebauten.gleichklang.core.view.component.ComponentFactory;
import de.binaerebauten.gleichklang.core.view.component.FilterComponent;

public class ProximityFilterComponent extends CustomComponent implements FilterComponent
{
	private final ProximityFilter filter;

	private final TextField radius;

	public ProximityFilterComponent(ProximityFilter filter)
	{
		this.filter = filter;

		final HorizontalLayout layout = new HorizontalLayout();
		layout.setSpacing(true);

		radius = ComponentFactory.getInstance().createField(Integer.class, TextField.class);
		radius.setCaption("Radius");

		layout.addComponent(radius);

		setCompositionRoot(layout);
	}

	@Override
	public void commit()
	{
		filter.setRadius((int) radius.getConvertedValue());
	}

	@Override
	public AbstractFilter getFilter()
	{
		return filter;
	}
}
